package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ValuationDao {
    @Query("SELECT * FROM valuation_history ORDER BY timestamp DESC")
    fun getAllValuations(): Flow<List<ValuationHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertValuation(valuation: ValuationHistory): Long

    @Query("DELETE FROM valuation_history WHERE id = :id")
    suspend fun deleteValuationById(id: Int)

    @Query("DELETE FROM valuation_history")
    suspend fun clearHistory()
}

@Dao
interface ComparisonDao {
    @Query("SELECT * FROM saved_comparisons ORDER BY timestamp DESC")
    fun getAllComparisons(): Flow<List<SavedComparison>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComparison(comparison: SavedComparison): Long

    @Query("DELETE FROM saved_comparisons WHERE id = :id")
    suspend fun deleteComparisonById(id: Int)
}
