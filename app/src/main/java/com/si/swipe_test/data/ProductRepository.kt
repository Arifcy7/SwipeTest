package com.si.swipe_test.data

import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProductRepository constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao
) {

    fun getProducts(): Flow<List<Product>> = productDao.getProducts()

    suspend fun refreshProducts() {
        val networkProducts = apiService.getProducts()
        productDao.insertProducts(networkProducts.map { it.copy(isSynced = true) })
    }

    suspend fun addProduct(
        productName: RequestBody,
        productType: RequestBody,
        price: RequestBody,
        tax: RequestBody,
        image: MultipartBody.Part? = null
    ): AddProductResponse {
        return apiService.addProduct(productName, productType, price, tax, image)
    }

    suspend fun saveProductLocally(product: Product) {
        productDao.insertProduct(product.copy(isSynced = false))
    }

    suspend fun getUnsyncedProducts(): List<Product> {
        return productDao.getUnsyncedProducts()
    }

    suspend fun setProductSynced(productId: Int) {
        productDao.setProductSynced(productId)
    }
}
