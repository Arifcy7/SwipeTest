package com.si.swipe_test.data

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {

    @GET("api/public/get")
    suspend fun getProducts(): List<Product>

    @Multipart
    @POST("api/public/add")
    suspend fun addProduct(
        @Part("product_name") productName: RequestBody,
        @Part("product_type") productType: RequestBody,
        @Part("price") price: RequestBody,
        @Part("tax") tax: RequestBody,
        @Part image: MultipartBody.Part?
    ): AddProductResponse
}

//data class AddProductResponse(
//    val success: Boolean,
//    val message: String,
//    @com.google.gson.annotations.SerializedName("product_id")
//    val productId: Int?,
//    @com.google.gson.annotations.SerializedName("product_details")
//    val productDetails: ProductDetails
//)

data class ProductDetails(
    @com.google.gson.annotations.SerializedName("product_name")
    val productName: String,
    @com.google.gson.annotations.SerializedName("product_type")
    val productType: String,
    val price: String,
    val tax: String,
    val image: String?
)