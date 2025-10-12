package com.si.swipe_test.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val repository: ProductRepository by inject()

    override suspend fun doWork(): Result {
        return try {
            val unsyncedProducts = repository.getUnsyncedProducts()
            unsyncedProducts.forEach { product ->
                val nameBody = product.productName.toRequestBody("text/plain".toMediaTypeOrNull())
                val typeBody = product.productType.toRequestBody("text/plain".toMediaTypeOrNull())
                val priceBody = product.price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val taxBody = product.tax.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // For simplicity, we are not handling image uploads in the background worker for now.
                val response = repository.addProduct(nameBody, typeBody, priceBody, taxBody)
                if (response.success) {
                    repository.setProductSynced(product.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
