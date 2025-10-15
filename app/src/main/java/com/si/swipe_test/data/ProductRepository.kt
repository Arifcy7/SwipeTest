package com.si.swipe_test.data

import android.content.Context
import android.util.Log
import android.webkit.MimeTypeMap
import com.si.swipe_test.utils.ConnectivityManager
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ProductRepository constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val connectivityManager: ConnectivityManager,
    private val context: Context
) {

    fun getProducts(): Flow<List<Product>> = productDao.getProducts()

    suspend fun refreshProducts() {
        if (connectivityManager.isNetworkAvailable()) {
            try {
                val networkProducts = apiService.getProducts()
                productDao.upsertProducts(networkProducts)
                Log.d("ProductRepository", "Refreshed ${networkProducts.size} products from server")
            } catch (e: Exception) {
                Log.e("ProductRepository", "Error refreshing products", e)
            }
        }
    }

    suspend fun addProduct(productFormData: ProductFormData): AddProductResponse? {
        // First insert locally with unsynced status
        val localProduct = Product(
            productName = productFormData.productName,
            productType = productFormData.productType,
            price = productFormData.price.toDoubleOrNull() ?: 0.0,
            tax = productFormData.tax.toDoubleOrNull() ?: 0.0,
            image = null,
            imageUri = productFormData.imageUri?.toString(),
            isSynced = false,
            serverId = null
        )

        val localId = productDao.insertProduct(localProduct)
        val insertedProduct = localProduct.copy(localId = localId.toInt())

        Log.d("ProductRepository", "Inserted local product with ID: $localId")

        // Try to sync immediately
        return syncProduct(insertedProduct, productFormData)
    }

    suspend fun syncUnsyncedProduct(product: Product): Boolean {
        if (!connectivityManager.isNetworkAvailable()) {
            return false
        }

        val productFormData = ProductFormData(
            productName = product.productName,
            productType = product.productType,
            price = product.price.toString(),
            tax = product.tax.toString(),
            imageUri = product.imageUri?.let { android.net.Uri.parse(it) }
        )

        val response = syncProduct(product, productFormData)
        return response?.success == true
    }

    private suspend fun syncProduct(product: Product, productFormData: ProductFormData): AddProductResponse? {
        if (!connectivityManager.isNetworkAvailable()) {
            Log.d("ProductRepository", "Network unavailable, product will sync later")
            return null
        }

        try {
            var imagePart: MultipartBody.Part? = null
            if (productFormData.imageUri != null) {
                val file = productFormData.imageUri.let { uri ->
                    context.contentResolver.openInputStream(uri)?.let { inputStream ->
                        val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
                        file.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        file
                    }
                }
                if (file != null) {
                    val extension = MimeTypeMap.getFileExtensionFromUrl(productFormData.imageUri.toString())
                    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "image/*"
                    val requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
                    imagePart = MultipartBody.Part.createFormData("files[]", file.name, requestBody)
                }
            }

            val response = apiService.addProduct(
                productName = productFormData.productName.toRequestBody("text/plain".toMediaTypeOrNull()),
                productType = productFormData.productType.toRequestBody("text/plain".toMediaTypeOrNull()),
                price = productFormData.price.toRequestBody("text/plain".toMediaTypeOrNull()),
                tax = productFormData.tax.toRequestBody("text/plain".toMediaTypeOrNull()),
                image = imagePart
            )

            Log.d("ProductRepository", "API Response: success=${response.success}, productId=${response.productId}")

            if (response.success && response.productId != null) {
                // Update the existing product with server data and mark as synced
                val syncedProduct = product.copy(
                    serverId = response.productId,
                    productName = response.productDetails.productName,
                    productType = response.productDetails.productType,
                    price = response.productDetails.price.toDoubleOrNull() ?: product.price,
                    tax = response.productDetails.tax.toDoubleOrNull() ?: product.tax,
                    image = response.productDetails.image,
                    isSynced = true,
                    imageUri = null // Clear local URI as image is now on server
                )

                // Update the product in the database
                productDao.updateProduct(syncedProduct)
                Log.d("ProductRepository", "Updated product ${product.localId} with server data (serverId: ${response.productId})")
            }

            return response
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error syncing product", e)
            return null
        }
    }
}