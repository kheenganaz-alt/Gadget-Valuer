package com.example.pricing

import java.util.Locale

data class ValuationInput(
    val basePriceUkUsed: Double,
    val brand: String,
    val ageInYears: Int,
    val storageGb: Int, // e.g. 64, 128, 256, 512
    val condition: String, // "Pristine", "Excellent", "Good", "Fair", "Broken"
    val batteryHealth: Int, // 50 to 100
    val screenCondition: String, // "Perfect", "Minor Scratches", "Cracked Screen", "Dead Spots"
    val marketDemand: String, // "Very High", "High", "Stable", "Low"
    val repairRisk: String, // "Low", "Medium", "High"
    // Advanced condition parameters (AI identified) with backward compatible defaults
    val screenCracksScore: Double = 100.0,
    val bezelDamageScore: Double = 100.0,
    val portWearScore: Double = 100.0,
    val cosmeticScratchesScore: Double = 100.0,
    val marketTrendOffset: Double = 0.0
)

data class ValuationOutput(
    val finalEstimatedMin: Double,
    val finalEstimatedMax: Double,
    val adjustmentBreakdown: List<String>,
    val marketAdvice: String
)

object GadgetPricing {
    fun calculateValuation(input: ValuationInput): ValuationOutput {
        var multiplier = 1.0
        val breakdowns = mutableListOf<String>()

        // 1. Brand Factor
        val brandLower = input.brand.lowercase(Locale.ROOT)
        if (brandLower.contains("apple") || brandLower.contains("iphone")) {
            multiplier *= 1.05
            breakdowns.add("Apple Premium Brand status: +5%")
        } else if (brandLower.contains("samsung")) {
            multiplier *= 1.02
            breakdowns.add("Samsung Brand stability: +2%")
        } else if (brandLower.contains("xiaomi") || brandLower.contains("infinix") || brandLower.contains("tecno")) {
            multiplier *= 0.93
            breakdowns.add("Budget Android rapid-depreciation factor: -7%")
        }

        // 2. Device Age
        val agePenalty = input.ageInYears * 0.08
        if (agePenalty > 0.0) {
            multiplier *= (1.0 - agePenalty).coerceAtLeast(0.5)
            breakdowns.add("Age depreciation penalty (${input.ageInYears} yrs): -${(agePenalty * 100).toInt()}%")
        }

        // 3. Storage Level Premium
        if (input.storageGb >= 512) {
            multiplier *= 1.15
            breakdowns.add("Elite Storage capacity (512GB+): +15%")
        } else if (input.storageGb >= 256) {
            multiplier *= 1.08
            breakdowns.add("High Storage selection (256GB): +8%")
        } else if (input.storageGb <= 64 && input.storageGb > 0) {
            multiplier *= 0.90
            breakdowns.add("Legacy Storage capacity (64GB or less): -10%")
        }

        // 4. Condition Selection
        when (input.condition) {
            "Pristine" -> {
                multiplier *= 1.12
                breakdowns.add("Pristine cosmetical shape: +12%")
            }
            "Excellent" -> {
                multiplier *= 1.05
                breakdowns.add("Excellent exterior care: +5%")
            }
            "Good" -> {
                // Baseline
                breakdowns.add("Standard Good state: 0% change")
            }
            "Fair" -> {
                multiplier *= 0.82
                breakdowns.add("Fair body usage signs: -18%")
            }
            "Broken" -> {
                multiplier *= 0.50
                breakdowns.add("Heavy structural body damage: -50%")
            }
        }

        // 5. Battery Health (Critical in Nigeria!)
        if (input.batteryHealth in 90..100) {
            multiplier *= 1.05
            breakdowns.add("Strong Battery Lifecycle (>90%): +5%")
        } else if (input.batteryHealth in 80..89) {
            // Normal
            breakdowns.add("Average Battery Lifespan: 0% alteration")
        } else if (input.batteryHealth in 70..79) {
            multiplier *= 0.88
            breakdowns.add("Battery health degradation (70%-79%): -12% price knock")
        } else {
            multiplier *= 0.75
            breakdowns.add("Critical battery replacement risk (<70%): -25% price drop")
        }

        // 6. Screen Condition
        when (input.screenCondition) {
            "Perfect" -> {
                multiplier *= 1.03
                breakdowns.add("Flawless original panel: +3%")
            }
            "Minor Scratches" -> {
                multiplier *= 0.95
                breakdowns.add("Minor hairline display scratches: -5%")
            }
            "Cracked Screen" -> {
                multiplier *= 0.65
                breakdowns.add("Cracked screen assembly (heavy panel swap cost): -35%")
            }
            "Dead Spots" -> {
                multiplier *= 0.45
                breakdowns.add("Dead pixels visual bleed damage: -55%")
            }
        }

        // 7. Market Demand
        when (input.marketDemand) {
            "Very High" -> {
                multiplier *= 1.07
                breakdowns.add("Sky-high local demand surge: +7%")
            }
            "High" -> {
                multiplier *= 1.03
                breakdowns.add("Strong continuous interest: +3%")
            }
            "Stable" -> {}
            "Low" -> {
                multiplier *= 0.90
                breakdowns.add("Low target market liquidity: -10%")
            }
        }

        // 8. Repair Risk
        when (input.repairRisk) {
            "Low" -> {}
            "Medium" -> {
                multiplier *= 0.96
                breakdowns.add("Board repair accessibility limits: -4%")
            }
            "High" -> {
                multiplier *= 0.88
                breakdowns.add("Import-only spare parts replacement risk: -12%")
            }
        }

        // 9. Advanced Physical Condition Scores (AI Analysis Dynamic Fine-Tuning)
        if (input.screenCracksScore < 100.0) {
            val panelPenalty = (100.0 - input.screenCracksScore) * 0.0035
            multiplier *= (1.0 - panelPenalty).coerceAtLeast(0.4)
            breakdowns.add("AI Screen Crack Assessment (${input.screenCracksScore.toInt()}% quality): -${(panelPenalty * 100).toInt()}%")
        }
        if (input.bezelDamageScore < 100.0) {
            val bezelPenalty = (100.0 - input.bezelDamageScore) * 0.0012
            multiplier *= (1.0 - bezelPenalty).coerceAtLeast(0.7)
            breakdowns.add("AI Bezel & Frame status (${input.bezelDamageScore.toInt()}% quality): -${(bezelPenalty * 100).toInt()}%")
        }
        if (input.portWearScore < 100.0) {
            val portPenalty = (100.0 - input.portWearScore) * 0.0008
            multiplier *= (1.0 - portPenalty).coerceAtLeast(0.8)
            breakdowns.add("AI Connection Port health (${input.portWearScore.toInt()}% quality): -${(portPenalty * 100).toInt()}%")
        }
        if (input.cosmeticScratchesScore < 100.0) {
            val scratchPenalty = (100.0 - input.cosmeticScratchesScore) * 0.001
            multiplier *= (1.0 - scratchPenalty).coerceAtLeast(0.75)
            breakdowns.add("AI Exterior scratches status (${input.cosmeticScratchesScore.toInt()}% quality): -${(scratchPenalty * 100).toInt()}%")
        }

        // 10. Real-time Market Demand & Trend Fluctuations
        if (input.marketTrendOffset != 0.0) {
            multiplier *= (1.0 + input.marketTrendOffset)
            val percentage = (input.marketTrendOffset * 100).toInt()
            val sign = if (percentage >= 0) "+" else ""
            breakdowns.add("Real-time local demand index: $sign$percentage%")
        }

        // Compute outputs
        val baseMin = input.basePriceUkUsed * multiplier
        val finalEstimatedMin = (baseMin * 0.95).coerceAtLeast(15000.0)
        val finalEstimatedMax = (baseMin * 1.05).coerceAtLeast(20000.0)

        // Generate customized advice
        val advice = when {
            multiplier >= 1.10 -> "This device is in stellar condition with maximum demand. Do not settle for low offers. Sell through premium verified listings."
            multiplier in 0.90..1.09 -> "Good reliable condition. Holds stable value. Post on the Gadget Valuer NG marketplace targeting urban buyers."
            else -> "Heavy depreciation noticed due to condition or components. It's recommended to repair the battery or screen to unlock an extra 45% valuation boost."
        }

        return ValuationOutput(
            finalEstimatedMin = Math.round(finalEstimatedMin / 500.0) * 500.0, // round to nearest 500 Naira
            finalEstimatedMax = Math.round(finalEstimatedMax / 500.0) * 500.0,
            adjustmentBreakdown = breakdowns,
            marketAdvice = advice
        )
    }
}
