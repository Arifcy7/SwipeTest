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

    @Transaction
    suspend fun upsertProducts(products: List<Product>) {
        for (product in products) {
            // The getProducts() endpoint doesn't return a serverId, so we can't check for it.
            // We must rely on matching product details.

            // First, see if an unsynced product with these exact details exists.
            val unsyncedMatch = getUnsyncedProductByDetails(product.productName, product.productType, product.price, product.tax)

            if (unsyncedMatch != null) {
                // This is an offline-created product that has now appeared in the server list.
                // However, the server list doesn't have the serverId, so we can't update it here.
                // We simply mark the local version as synced to prevent it from being uploaded again.
                // The SyncWorker, which DOES get the serverId, will be responsible for adding it.
                // Or, if the SyncWorker has already run, this item will soon be updated with a serverId.
                // For now, we just ensure it's marked as synced.
                if (!unsyncedMatch.isSynced) {
                    updateProduct(unsyncedMatch.copy(isSynced = true))
                }
            } else {
                // No unsynced product matches. This means it's either a brand new product from the server,
                // or it's a product that is already synced (including our offline one if the SyncWorker was fast).
                // We use REPLACE strategy to insert new products or update existing ones.
                // This relies on a unique constraint on serverId, which we should add.
                // For now, since serverId is not available, we use product details to find existing items.
                val existingSynced = getProductByDetails(product.productName, product.productType, product.price, product.tax)
                if (existingSynced == null) {
                    insertProduct(product.copy(isSynced = true))
                }
            }
        }
    }

    @Query("SELECT * FROM products WHERE productName = :productName AND productType = :productType AND price = :price AND tax = :tax LIMIT 1")
    suspend fun getProductByDetails(productName: String, productType: String, price: Double, tax: Double): Product?
}
