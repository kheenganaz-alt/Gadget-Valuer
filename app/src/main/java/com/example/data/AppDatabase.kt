package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ValuationHistory::class, SavedComparison::class, VendorShop::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun valuationDao(): ValuationDao
    abstract fun comparisonDao(): ComparisonDao
    abstract fun vendorDao(): VendorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gadget_valuer_db"
                )
                .fallbackToDestructiveMigration() // safe destructible migrations
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
