package com.si.swipe_test.data

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.si.swipe_test.utils.ConnectivityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val productRepository: ProductRepository,
    private val connectivityManager: ConnectivityManager,
    private val productDao: ProductDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (connectivityManager.isNetworkAvailable()) {
                val unsyncedProducts = productDao.getUnsyncedProducts()
                for (product in unsyncedProducts) {
                    val productFormData = ProductFormData(
                        productName = product.productName,
                        productType = product.productType,
                        price = product.price.toString(),
                        tax = product.tax.toString(),
                        imageUri = product.imageUri?.let { Uri.parse(it) }
                    )
                    productRepository.addProduct(productFormData)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
