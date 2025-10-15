package com.si.swipe_test.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "products",
    indices = [Index(value = ["serverId"], unique = false)]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,

    @SerializedName("product_id")
    val serverId: Int? = null,

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("price")
    val price: Double,

    @SerializedName("product_name")
    val productName: String,

    @SerializedName("product_type")
    val productType: String,

    @SerializedName("tax")
    val tax: Double,

    val isSynced: Boolean = false,

    val imageUri: String? = null
)