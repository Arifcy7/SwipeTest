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

    @Query("SELECT * FROM products WHERE isSynced = 0 AND productName = :productName AND productType = :productType AND price = :price AND tax = :tax LIMIT 1")
    suspend fun getUnsyncedProductByDetails(productName: String, productType: String, price: Double, tax: Double): Product?

    @Query("DELETE FROM products WHERE localId = :localId")
    suspend fun deleteProductByLocalId(localId: Int)

    @Query("SELECT * FROM products WHERE productName = :productName AND productType = :productType AND price = :price AND tax = :tax LIMIT 1")
    suspend fun getProductByDetails(productName: String, productType: String, price: Double, tax: Double): Product?

    @Transaction
    suspend fun upsertProducts(products: List<Product>) {
        for (product in products) {
            val existingProduct = if (product.serverId != null) {
                getProductByServerId(product.serverId)
            } else {
                getProductByDetails(product.productName, product.productType, product.price, product.tax)
            }

            if (existingProduct == null) {
                // New product from server
                insertProduct(product.copy(isSynced = true))
            } else if (!existingProduct.isSynced) {
                // Product exists locally but not synced - update it with server data
                updateProduct(existingProduct.copy(
                    serverId = product.serverId,
                    image = product.image,
                    isSynced = true,
                    imageUri = null
                ))
            }
        }
    }
}