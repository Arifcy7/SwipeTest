package com.si.swipe_test.data

import com.google.gson.annotations.SerializedName

data class AddProductResponse(
    val message: String,
    @SerializedName("product_details")
    val productDetails: Product,
    @SerializedName("product_id")
    val productId: Int,
    val success: Boolean
)
