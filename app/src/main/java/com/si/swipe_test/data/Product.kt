package com.si.swipe_test.data

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("image")
    val image: String?,
    @SerializedName("price")
    val price: Double,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("product_type")
    val productType: String,
    @SerializedName("tax")
    val tax: Double
)
