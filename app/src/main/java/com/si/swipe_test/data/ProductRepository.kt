package com.si.swipe_test.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

class ProductRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getProducts(): List<Product> {
        return apiService.getProducts()
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
}
