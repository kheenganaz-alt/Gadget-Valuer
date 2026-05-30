package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "valuation_history")
data class ValuationHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gadgetName: String,
    val category: String, // e.g., Phone, Laptop, Tablet, Smartwatch
    val brand: String,
    val model: String,
    val specs: String, // e.g., "128GB, 8GB RAM, Midnight Black"
    val physicalCondition: String, // e.g., Like New, Good, Fair, Broken
    val calculatedValueMin: Long, // Value in NGN
    val calculatedValueMax: Long, // Value in NGN
    val grade: String, // e.g., Grade A, Grade B, Grade C
    val comments: String, // AI analysis feedback
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null // Reference snapshot base64
)

@Serializable
@Entity(tableName = "saved_comparisons")
data class SavedComparison(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val gadgetAName: String,
    val gadgetAValueMin: Long,
    val gadgetAValueMax: Long,
    val gadgetBName: String,
    val gadgetBValueMin: Long,
    val gadgetBValueMax: Long,
    val specComparison: String, // Dynamic summary text
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
@Entity(tableName = "vendor_shops")
data class VendorShop(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val center: String = "", // backwards compatible
    val details: String = "", // backwards compatible
    val ownerName: String = "",
    val phone: String = "",
    val whatsapp: String = "",
    val email: String = "",
    val state: String = "Lagos", // default operating state
    val city: String = "",
    val address: String = "",
    val categories: String = "Phones, Laptops, Accessories",
    val logoUrl: String? = null,
    val verificationDocUrl: String? = null,
    val isVerified: Boolean = false,
    val ratings: Double = 4.5,
    val specialization: String = "",
    val telephone: String = "", // backwards compatible support for legacy tests
    val status: String = "pending", // "pending", "approved", "suspended", "rejected"
    val timestamp: Long = System.currentTimeMillis()
)
