package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String, // Keep lowercase during insertion
    val phoneNumber: String,
    val password: String,
    val state: String,
    val city: String,
    val role: String, // USER, SHOP, VENDOR, ADMIN
    val status: String = "ACTIVE" // ACTIVE, SUSPENDED
)

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val whatsApp: String,
    val email: String,
    val state: String,
    val city: String,
    val address: String,
    val categoriesSold: String, // Semicolon-separated, e.g. "Phones;Laptops;Tablets"
    val shopLogo: String,
    val verificationDocument: String,
    val status: String, // PENDING, APPROVED, REJECTED, SUSPENDED
    val isVerified: Boolean = false
)

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vendorName: String,
    val ownerName: String,
    val phone: String,
    val whatsApp: String,
    val email: String,
    val state: String,
    val city: String,
    val address: String,
    val categoriesSold: String,
    val vendorLogo: String,
    val verificationDocument: String,
    val status: String // PENDING, APPROVED, REJECTED, SUSPENDED
)

@Entity(tableName = "gadget_scans")
data class GadgetScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val imageUri: String?,
    val gadgetName: String,
    val brand: String,
    val model: String,
    val category: String,
    val storage: String,
    val color: String,
    val estimatedCondition: String,
    val confidenceScore: Double,
    val ukUsedMin: Double,
    val ukUsedMax: Double,
    val fairlyUsedMin: Double,
    val fairlyUsedMax: Double,
    val brandNewMin: Double,
    val brandNewMax: Double,
    val marketTrend: String,
    val commonIssues: String, // Semicolon-separated
    val conditionFactors: String,
    val bestResaleAdvice: String,
    val timestamp: Long = System.currentTimeMillis(),
    val screenCracksScore: Double = 95.0,
    val bezelDamageScore: Double = 90.0,
    val portWearScore: Double = 95.0,
    val cosmeticScratchesScore: Double = 92.0,
    val conditionConfidenceScore: Double = 93.0
)

@Entity(tableName = "gadget_listings")
data class GadgetListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val posterName: String,
    val gadgetName: String,
    val brand: String,
    val model: String,
    val category: String,
    val condition: String,
    val price: Double,
    val state: String,
    val city: String,
    val imageUri: String?,
    val description: String,
    val contactPhone: String,
    val contactWhatsApp: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, SUSPENDED
    val isFeatured: Boolean = false,
    val isSold: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_gadgets")
data class SavedGadgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val scanId: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val listingId: Int = 0, // Backward status fallback field
    val reporterName: String = "Anonymous",
    val reporterEmail: String = "",
    val targetType: String = "LISTING", // LISTING, SHOP, VENDOR, PRICE, IDENTIFICATION
    val targetId: Int = 0,
    val targetName: String = "General Listing",
    val reason: String, // Wrong gadget price, Fake shop, Fake listing, Scam suspicion, Wrong gadget identification, Abusive content
    val details: String = "",
    val status: String = "PENDING", // PENDING, REVIEWED, RESOLVED, DISMISSED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderEmail: String,
    val subject: String,
    val message: String,
    val status: String = "OPEN", // OPEN, IN_PROGRESS, RESOLVED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val id: Int = 1,
    val priceDisclaimer: String,
    val supportedStates: String, // Semicolon-separated
    val supportedCategories: String, // Semicolon-separated
    val marketplaceRules: String,
    val shopApprovalMessage: String,
    val vendorApprovalMessage: String,
    val contactEmail: String,
    val whatsAppNumber: String
)

@Entity(tableName = "admin_action_logs")
data class AdminActionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val adminUserId: Int,
    val adminEmail: String,
    val actionType: String, // APPROVE, REJECT, SUSPEND, REACTIVATE, DELETE, VERIFY, UNVERIFY, FEATURE, UNFEATURE, EDIT, MARK_SOLD, CHANGE_STATUS
    val targetType: String, // USER, SHOP, VENDOR, LISTING, REPORT, SUPPORT, SETTINGS
    val targetId: Int,
    val oldValue: String = "",
    val newValue: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
