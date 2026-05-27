package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val valuationDao: ValuationDao,
    private val comparisonDao: ComparisonDao
) {
    val allValuations: Flow<List<ValuationHistory>> = valuationDao.getAllValuations()
    val allComparisons: Flow<List<SavedComparison>> = comparisonDao.getAllComparisons()

    suspend fun insertValuation(valuation: ValuationHistory): Long {
        return valuationDao.insertValuation(valuation)
    }

    suspend fun deleteValuationById(id: Int) {
        valuationDao.deleteValuationById(id)
    }

    suspend fun clearHistory() {
        valuationDao.clearHistory()
    }

    suspend fun insertComparison(comparison: SavedComparison): Long {
        return comparisonDao.insertComparison(comparison)
    }

    suspend fun deleteComparisonById(id: Int) {
        comparisonDao.deleteComparisonById(id)
    }
}
