package com.si.swipe_test.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Product::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}