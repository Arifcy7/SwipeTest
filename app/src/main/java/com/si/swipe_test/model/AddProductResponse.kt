package com.si.swipe_test.model

import com.google.gson.annotations.SerializedName

data class AddProductResponse(
    val message: String,
    @SerializedName("product_details")
    val productDetails: ProductDetailsResponse,
    @SerializedName("product_id")
    val productId: Int,
    val success: Boolean
)

data class ProductDetailsResponse(
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_type")
    val productType: String,
    val price: String,
    val tax: String,
    val image: String?
)