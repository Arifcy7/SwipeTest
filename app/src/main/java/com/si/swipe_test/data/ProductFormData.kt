package com.si.swipe_test.data

import android.net.Uri

data class ProductFormData(
    val productName: String = "",
    val productType: String = "",
    val price: String = "",
    val tax: String = "",
    val imageUri: Uri? = null
)
