package com.example.ui

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GadgetAnalysisResult
import com.example.ai.GadgetAnalyzer
import com.example.data.AppRepository
import com.example.data.SavedComparison
import com.example.data.ValuationHistory
import com.example.data.VendorShop
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState {
    object Idle : UiState
    object Loading : UiState
    data class Success(val result: GadgetAnalysisResult) : UiState
    data class Error(val message: String) : UiState
}

class MainViewModel(
    private val repository: AppRepository,
    private val context: android.content.Context
) : ViewModel() {

    private val analyzer = GadgetAnalyzer()
    
    private val prefs = context.getSharedPreferences("gadget_valuer_prefs", android.content.Context.MODE_PRIVATE)

    private val _customGeminiApiKey = MutableStateFlow(prefs.getString("custom_gemini_api_key", "") ?: "")
    val customGeminiApiKey = _customGeminiApiKey.asStateFlow()

    fun updateCustomGeminiApiKey(key: String) {
        _customGeminiApiKey.value = key.trim()
        prefs.edit().putString("custom_gemini_api_key", key.trim()).apply()
    }

    // Login and Profile Management
    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _loggedInUserEmail = MutableStateFlow("")
    val loggedInUserEmail: StateFlow<String> = _loggedInUserEmail.asStateFlow()

    private val _loggedInUserName = MutableStateFlow("")
    val loggedInUserName: StateFlow<String> = _loggedInUserName.asStateFlow()

    private val _isUserAdmin = MutableStateFlow(false)
    val isUserAdmin: StateFlow<Boolean> = _isUserAdmin.asStateFlow()

    // Screen State Navigation
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Valuation UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _valuationResult = MutableStateFlow<GadgetAnalysisResult?>(null)
    val valuationResult: StateFlow<GadgetAnalysisResult?> = _valuationResult.asStateFlow()

    // Active diagnostic parameters for custom fine-tuning
    val selectedCondition = MutableStateFlow("UK Used (Good)") // UK Used (Mint), UK Used (Good), Locally Used (Fair)
    val hasOriginalCharger = MutableStateFlow(true)
    val hasReceipt = MutableStateFlow(false)
    val screenIntact = MutableStateFlow(true)
    val batteryNormal = MutableStateFlow(true)

    // Camera Scanning State
    val capturedImage = MutableStateFlow<Bitmap?>(null)
    val analyzerQueryText = MutableStateFlow("")

    // Comparison States
    private val _comparisonLoading = MutableStateFlow(false)
    val comparisonLoading: StateFlow<Boolean> = _comparisonLoading.asStateFlow()

    private val _activeComparison = MutableStateFlow<SavedComparison?>(null)
    val activeComparison: StateFlow<SavedComparison?> = _activeComparison.asStateFlow()

    // Database flow streams
    val valuationsHistory: StateFlow<List<ValuationHistory>> = repository.allValuations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val comparisonsHistory: StateFlow<List<SavedComparison>> = repository.allComparisons
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val vendorsList: StateFlow<List<VendorShop>> = repository.allVendors
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            try {
                if (repository.getVendorCount() == 0) {
                    repository.insertVendor(VendorShop(
                        name = "Slot Systems Hub",
                        ownerName = "Nnamdi Slot",
                        phone = "+2348030000000",
                        whatsapp = "+2348030000000",
                        email = "info@slot.ng",
                        state = "Lagos",
                        city = "Ikeja Computer Village",
                        address = "Slot Plaza, 12 Ola Ayeni St, Ikeja",
                        categories = "Phones, Laptops, Accessories, Repairs, UK Used, Fairly Used, Brand new",
                        isVerified = true,
                        ratings = 4.8,
                        status = "approved"
                    ))
                    repository.insertVendor(VendorShop(
                        name = "MicroStation Refurbs",
                        ownerName = "Uche Station",
                        phone = "+2348020000001",
                        whatsapp = "+2348020000001",
                        email = "sales@microstation.ng",
                        state = "Abuja (FCT)",
                        city = "Wuse II",
                        address = "Central Tech Galleria, Suite B10, Wuse 2",
                        categories = "Laptops, Accessories, UK Used, Brand new",
                        isVerified = true,
                        ratings = 4.5,
                        status = "approved"
                    ))
                    repository.insertVendor(VendorShop(
                        name = "Capital Gadget Palace",
                        ownerName = "Alhaji Musa Gadgets",
                        phone = "+2348090000002",
                        whatsapp = "+2348090000002",
                        email = "musa@capitalgadgets.ng",
                        state = "Rivers",
                        city = "Port Harcourt",
                        address = "Downtown Digital Plaza, Shop 4, Garrison, PH",
                        categories = "Phones, Tablets, Accessories, UK Used, Fairly Used",
                        isVerified = true,
                        ratings = 4.7,
                        status = "approved"
                    ))
                    repository.insertVendor(VendorShop(
                        name = "Secured Trade Hub Minna",
                        ownerName = "Kola Trusted Deals",
                        phone = "+2348060000003",
                        whatsapp = "+2348060000003",
                        email = "minna@securedtrade.ng",
                        state = "Niger",
                        city = "Minna",
                        address = "14 Bosso Road, Minna, opposite Mobil Station",
                        categories = "Phones, Laptops, Repairs, Fairly Used",
                        isVerified = false,
                        ratings = 4.4,
                        status = "approved"
                    ))
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error seeding vendors", e)
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
        // Reset analysis when moving back to search
        if (screen == "dashboard") {
            _uiState.value = UiState.Idle
            capturedImage.value = null
            analyzerQueryText.value = ""
        }
    }

    fun setCapturedBitmap(bitmap: Bitmap) {
        capturedImage.value = bitmap
        _currentScreen.value = "scanner_confirm" // show captured photo preview
    }

    fun submitValuation(name: String, bitmap: Bitmap?) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            _currentScreen.value = "valuation"
            try {
                val customKey = _customGeminiApiKey.value.takeIf { it.isNotBlank() }
                val result = analyzer.analyzeGadget(name, bitmap, customKey)
                _valuationResult.value = result
                _uiState.value = UiState.Success(result)
            } catch (e: Throwable) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred during valuation.")
            }
        }
    }

    fun saveCustomizedAppraisal(
        result: GadgetAnalysisResult,
        condition: String,
        hasCharger: Boolean,
        hasReceipt: Boolean,
        screenPerfect: Boolean,
        batteryPerfect: Boolean,
        finalMin: Long,
        finalMax: Long
    ) {
        viewModelScope.launch {
            val specsStr = buildString {
                append(result.estimatedSpecs)
                val extras = mutableListOf<String>()
                if (hasCharger) extras.add("Charger") else extras.add("No Charger")
                if (hasReceipt) extras.add("Receipt") else extras.add("No Receipt")
                if (!screenPerfect) extras.add("Screen Scratched")
                if (!batteryPerfect) extras.add("Battery Cycle")
                if (extras.isNotEmpty()) {
                    append(" | ")
                    append(extras.joinToString(", "))
                }
            }
            val historyItem = ValuationHistory(
                gadgetName = result.name,
                category = result.category,
                brand = result.brand,
                model = result.model,
                specs = specsStr,
                physicalCondition = condition,
                calculatedValueMin = finalMin,
                calculatedValueMax = finalMax,
                grade = condition,
                comments = "Adjusted condition & physical inclusions inside safe transactional history."
            )
            repository.insertValuation(historyItem)
        }
    }

    fun runDeviceComparison(deviceA: String, deviceB: String) {
        if (deviceA.isBlank() || deviceB.isBlank()) return
        
        viewModelScope.launch {
            _comparisonLoading.value = true
            try {
                val valA = analyzer.analyzeGadget(deviceA)
                val valB = analyzer.analyzeGadget(deviceB)

                val summaryText = "Comparative secondary-market appraisal: ${valA.name} typically resells for ₦${valA.valueMinGradeB.toFormattedString()} - ₦${valA.valueMaxGradeB.toFormattedString()} (UK Used Good) with steady market liquidity. Handsets of ${valB.name} trade at around ₦${valB.valueMinGradeB.toFormattedString()} - ₦${valB.valueMaxGradeB.toFormattedString()} retaining strong trading value."

                val comp = SavedComparison(
                    gadgetAName = valA.name,
                    gadgetAValueMin = valA.valueMinGradeB,
                    gadgetAValueMax = valA.valueMaxGradeB,
                    gadgetBName = valB.name,
                    gadgetBValueMin = valB.valueMinGradeB,
                    gadgetBValueMax = valB.valueMaxGradeB,
                    specComparison = summaryText
                )

                repository.insertComparison(comp)
                _activeComparison.value = comp
            } catch (e: Exception) {
                Log.e("MainViewModel", "Comparison failed", e)
            } finally {
                _comparisonLoading.value = false
            }
        }
    }

    fun deleteValuation(historyId: Int) {
        viewModelScope.launch {
            repository.deleteValuationById(historyId)
        }
    }

    fun clearAllValuations() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun deleteComparison(comparisonId: Int) {
        viewModelScope.launch {
            repository.deleteComparisonById(comparisonId)
        }
    }

    fun loginUser(email: String, name: String) {
        if (email.isBlank() || name.isBlank()) return
        _isUserLoggedIn.value = true
        _loggedInUserEmail.value = email.trim()
        _loggedInUserName.value = name.trim()
        _isUserAdmin.value = email.trim().equals("Kheenganaz@gmail.com", ignoreCase = true)
    }

    fun logoutUser() {
        _isUserLoggedIn.value = false
        _loggedInUserEmail.value = ""
        _loggedInUserName.value = ""
        _isUserAdmin.value = false
    }

    fun submitVendorApplication(
        name: String,
        ownerName: String,
        phone: String,
        whatsapp: String,
        email: String,
        state: String,
        city: String,
        address: String,
        categories: String,
        logoUrl: String? = null,
        verificationDocUrl: String? = null
    ) {
        viewModelScope.launch {
            val app = VendorShop(
                name = name,
                ownerName = ownerName,
                phone = phone,
                whatsapp = whatsapp,
                email = email,
                state = state,
                city = city,
                address = address,
                categories = categories,
                logoUrl = logoUrl,
                verificationDocUrl = verificationDocUrl,
                status = "pending",
                isVerified = false,
                ratings = 4.2,
                telephone = phone, // legacy fallback standard
                center = "$city, $state", // legacy fallback standard
                details = "Owner: $ownerName. Address: $address. Specialties: $categories", // legacy fallback standard
                specialization = categories
            )
            repository.insertVendor(app)
        }
    }

    // Overload signature for compatibility with other legacy areas
    fun submitVendorApplication(name: String, center: String, details: String, specialization: String, telephone: String) {
        viewModelScope.launch {
            val app = VendorShop(
                name = name,
                ownerName = "Unknown",
                phone = telephone,
                whatsapp = telephone,
                email = "",
                state = "Lagos",
                city = "Unknown Center",
                address = center,
                categories = specialization,
                logoUrl = null,
                verificationDocUrl = null,
                status = "pending",
                isVerified = false,
                ratings = 4.5,
                telephone = telephone,
                center = center,
                details = details,
                specialization = specialization
            )
            repository.insertVendor(app)
        }
    }

    fun updateVendorStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateVendorStatus(id, status)
        }
    }

    fun updateVendorState(id: Int, state: String) {
        viewModelScope.launch {
            repository.updateVendorState(id, state)
        }
    }

    fun approveVendor(id: Int) {
        viewModelScope.launch {
            repository.approveVendor(id)
        }
    }

    fun deleteVendor(id: Int) {
        viewModelScope.launch {
            repository.deleteVendor(id)
        }
    }

    // Direct helper extension to format big numbers in Nigerian format (e.g. 520,000)
    private fun Long.toFormattedString(): String {
        return java.text.NumberFormat.getIntegerInstance().format(this)
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
