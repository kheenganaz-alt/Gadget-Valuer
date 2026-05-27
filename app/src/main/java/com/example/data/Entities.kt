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
