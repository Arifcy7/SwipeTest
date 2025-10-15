package com.si.swipe_test.data

import android.content.Context
import android.util.Log
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
                Log.d("SyncWorker", "Found ${unsyncedProducts.size} unsynced products")

                for (product in unsyncedProducts) {
                    Log.d("SyncWorker", "Syncing product: ${product.productName} (localId: ${product.localId})")
                    val success = productRepository.syncUnsyncedProduct(product)

                    if (success) {
                        Log.d("SyncWorker", "Successfully synced product ${product.localId}")
                    } else {
                        Log.d("SyncWorker", "Failed to sync product ${product.localId}")
                    }
                }
                Result.success()
            } else {
                Log.d("SyncWorker", "Network not available, retrying later")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error during sync", e)
            Result.retry()
        }
    }
}