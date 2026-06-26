package com.saglik.domain.bmi

import java.util.Locale

object BmiFormatter {
    fun formatValue(value: Float): String = String.format(Locale.US, "%.1f", value)

    fun categoryLabel(category: BmiCategory): String =
        when (category) {
            BmiCategory.LOW -> "Low range"
            BmiCategory.HEALTHY -> "Healthy range"
            BmiCategory.HIGH -> "High range"
            BmiCategory.VERY_HIGH -> "Very high range"
            BmiCategory.UNKNOWN -> "Not available"
        }
}
