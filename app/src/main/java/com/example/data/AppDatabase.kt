package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        ShopEntity::class,
        VendorEntity::class,
        GadgetScanEntity::class,
        GadgetListingEntity::class,
        SavedGadgetEntity::class,
        ReportEntity::class,
        SupportMessageEntity::class,
        AppSettingEntity::class,
        AdminActionLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun shopDao(): ShopDao
    abstract fun vendorDao(): VendorDao
    abstract fun gadgetScanDao(): GadgetScanDao
    abstract fun gadgetListingDao(): GadgetListingDao
    abstract fun savedGadgetDao(): SavedGadgetDao
    abstract fun reportDao(): ReportDao
    abstract fun supportMessageDao(): SupportMessageDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun adminActionLogDao(): AdminActionLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gadget_valuer_ng_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
