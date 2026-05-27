package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val valuationDao: ValuationDao,
    private val comparisonDao: ComparisonDao,
    private val vendorDao: VendorDao
) {
    val allValuations: Flow<List<ValuationHistory>> = valuationDao.getAllValuations()
    val allComparisons: Flow<List<SavedComparison>> = comparisonDao.getAllComparisons()
    val allVendors: Flow<List<VendorShop>> = vendorDao.getAllVendors()

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

    suspend fun insertVendor(vendor: VendorShop): Long {
        return vendorDao.insertVendor(vendor)
    }

    suspend fun approveVendor(id: Int) {
        vendorDao.approveVendor(id)
    }

    suspend fun deleteVendor(id: Int) {
        vendorDao.deleteVendor(id)
    }

    suspend fun getVendorCount(): Int {
        return vendorDao.getVendorCount()
    }
}
