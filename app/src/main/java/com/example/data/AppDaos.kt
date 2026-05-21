package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("UPDATE users SET status = :status WHERE id = :id")
    suspend fun updateUserStatus(id: Int, status: String)

    @Query("UPDATE users SET role = :role WHERE id = :id")
    suspend fun updateUserRole(id: Int, role: String)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int)
}

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity): Long

    @Query("SELECT * FROM shops ORDER BY id DESC")
    fun getAllShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE status = 'APPROVED' ORDER BY id DESC")
    fun getApprovedShops(): Flow<List<ShopEntity>>

    @Query("UPDATE shops SET status = :status WHERE id = :id")
    suspend fun updateShopStatus(id: Int, status: String)

    @Query("UPDATE shops SET state = :state WHERE id = :id")
    suspend fun updateShopState(id: Int, state: String)

    @Query("UPDATE shops SET city = :city WHERE id = :id")
    suspend fun updateShopCity(id: Int, city: String)

    @Query("UPDATE shops SET isVerified = :isVerified WHERE id = :id")
    suspend fun updateShopVerification(id: Int, isVerified: Boolean)

    @Query("DELETE FROM shops WHERE id = :id")
    suspend fun deleteShop(id: Int)
}

@Dao
interface VendorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Query("SELECT * FROM vendors ORDER BY id DESC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE status = 'APPROVED' ORDER BY id DESC")
    fun getApprovedVendors(): Flow<List<VendorEntity>>

    @Query("UPDATE vendors SET status = :status WHERE id = :id")
    suspend fun updateVendorStatus(id: Int, status: String)

    @Query("UPDATE vendors SET state = :state WHERE id = :id")
    suspend fun updateVendorState(id: Int, state: String)

    @Query("DELETE FROM vendors WHERE id = :id")
    suspend fun deleteVendor(id: Int)
}

@Dao
interface GadgetScanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: GadgetScanEntity): Long

    @Query("SELECT * FROM gadget_scans WHERE userId = :userId ORDER BY timestamp DESC")
    fun getScansForUser(userId: Int): Flow<List<GadgetScanEntity>>

    @Query("SELECT * FROM gadget_scans WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: Int): GadgetScanEntity?

    @Query("SELECT * FROM gadget_scans ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<GadgetScanEntity>>
}

@Dao
interface GadgetListingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: GadgetListingEntity): Long

    @Query("SELECT * FROM gadget_listings ORDER BY timestamp DESC")
    fun getAllListings(): Flow<List<GadgetListingEntity>>

    @Query("SELECT * FROM gadget_listings WHERE status = 'APPROVED' ORDER BY timestamp DESC")
    fun getApprovedListings(): Flow<List<GadgetListingEntity>>

    @Query("UPDATE gadget_listings SET status = :status WHERE id = :id")
    suspend fun updateListingStatus(id: Int, status: String)

    @Query("UPDATE gadget_listings SET isFeatured = :isFeatured WHERE id = :id")
    suspend fun updateListingFeatured(id: Int, isFeatured: Boolean)

    @Query("UPDATE gadget_listings SET isSold = :isSold WHERE id = :id")
    suspend fun updateListingSold(id: Int, isSold: Boolean)

    @Query("DELETE FROM gadget_listings WHERE id = :id")
    suspend fun deleteListing(id: Int)
}

@Dao
interface SavedGadgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaved(saved: SavedGadgetEntity): Long

    @Query("DELETE FROM saved_gadgets WHERE userId = :userId AND scanId = :scanId")
    suspend fun deleteSaved(userId: Int, scanId: Int)

    @Query("SELECT * FROM saved_gadgets WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSavedForUser(userId: Int): Flow<List<SavedGadgetEntity>>
}

@Dao
interface ReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("UPDATE reports SET status = :status WHERE id = :id")
    suspend fun updateReportStatus(id: Int, status: String)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: Int)
}

@Dao
interface SupportMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: SupportMessageEntity): Long

    @Query("SELECT * FROM support_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SupportMessageEntity>>

    @Query("UPDATE support_messages SET status = :status WHERE id = :id")
    suspend fun updateMessageStatus(id: Int, status: String)

    @Query("DELETE FROM support_messages WHERE id = :id")
    suspend fun deleteMessage(id: Int)
}

@Dao
interface AppSettingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettingEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): AppSettingEntity?

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<AppSettingEntity?>
}

@Dao
interface AdminActionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AdminActionLogEntity): Long

    @Query("SELECT * FROM admin_action_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AdminActionLogEntity>>
}
