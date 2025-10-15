package com.si.swipe_test.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertProducts(products: List<Product>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM products ORDER BY localId DESC")
    fun getProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<Product>

    @Query("UPDATE products SET isSynced = 1 WHERE localId = :productId")
    suspend fun setProductSynced(productId: Int)

    @Query("DELETE FROM products WHERE serverId = :serverId AND isSynced = 0")
    suspend fun deleteUnsyncedDuplicates(serverId: Int)

    @Query("SELECT * FROM products WHERE serverId = :serverId LIMIT 1")
    suspend fun getProductByServerId(serverId: Int): Product?

    @Query("SELECT * FROM products WHERE productName = :productName AND productType = :productType AND price = :price AND tax = :tax LIMIT 1")
    suspend fun getProductByDetails(productName: String, productType: String, price: Double, tax: Double): Product?

    @Query("DELETE FROM products WHERE localId = :localId")
    suspend fun deleteProductByLocalId(localId: Int)

    @Transaction
    suspend fun upsertProducts(products: List<Product>) {
        for (product in products) {
            val existing = if (product.serverId != null) {
                getProductByServerId(product.serverId)
            } else {
                getProductByDetails(product.productName, product.productType, product.price, product.tax)
            }

            if (existing != null) {
                val updatedProduct = existing.copy(
                    productName = product.productName,
                    productType = product.productType,
                    price = product.price,
                    tax = product.tax,
                    image = product.image,
                    isSynced = true
                )
                updateProduct(updatedProduct)
            } else {
                insertProduct(product.copy(isSynced = true))
            }
        }
    }
}