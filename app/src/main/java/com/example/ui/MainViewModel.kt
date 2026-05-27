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

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val analyzer = GadgetAnalyzer()

    // Screen State Navigation
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Valuation UI State
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _valuationResult = MutableStateFlow<GadgetAnalysisResult?>(null)
    val valuationResult: StateFlow<GadgetAnalysisResult?> = _valuationResult.asStateFlow()

    // Active diagnostic parameters for custom fine-tuning
    val selectedCondition = MutableStateFlow("Grade B") // Grade A, Grade B, Grade C
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
                val result = analyzer.analyzeGadget(name, bitmap)
                _valuationResult.value = result
                _uiState.value = UiState.Success(result)
                
                // Save to Room DB History
                val historyItem = ValuationHistory(
                    gadgetName = result.name,
                    category = result.category,
                    brand = result.brand,
                    model = result.model,
                    specs = result.estimatedSpecs,
                    physicalCondition = selectedCondition.value,
                    calculatedValueMin = result.valueMinGradeB,
                    calculatedValueMax = result.valueMaxGradeB,
                    grade = "Grade B",
                    comments = result.localMarketAnalysis
                )
                repository.insertValuation(historyItem)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "An unexpected error occurred during valuation.")
            }
        }
    }

    fun runDeviceComparison(deviceA: String, deviceB: String) {
        if (deviceA.isBlank() || deviceB.isBlank()) return
        
        viewModelScope.launch {
            _comparisonLoading.value = true
            try {
                val valA = analyzer.analyzeGadget(deviceA)
                val valB = analyzer.analyzeGadget(deviceB)

                val summaryText = "Comparative market study in Ikeja: ${valA.name} typically resells for ₦${valA.valueMinGradeB.toFormattedString()} - ₦${valA.valueMaxGradeB.toFormattedString()} (Grade B) with steady market liquidity. Handsets of ${valB.name} trade at around ₦${valB.valueMinGradeB.toFormattedString()} - ₦${valB.valueMaxGradeB.toFormattedString()} supporting solid swap-deals."

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

    // Direct helper extension to format big numbers in Nigerian format (e.g. 520,000)
    private fun Long.toFormattedString(): String {
        return java.text.NumberFormat.getIntegerInstance().format(this)
    }
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
