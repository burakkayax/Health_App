package com.saglik.feature.summary

import com.saglik.domain.bmi.BmiCategory
import com.saglik.domain.bmi.BmiFormatter
import com.saglik.domain.bmi.BmiMissingReason
import com.saglik.domain.bmi.BmiSummary

internal object BmiUiMapper {
    fun map(summary: BmiSummary): BmiSummaryUiState {
        val result = summary.bmi
        if (summary.hasData && result != null) {
            return BmiSummaryUiState(
                bmiText = BmiFormatter.formatValue(result.value),
                categoryText = BmiFormatter.categoryLabel(result.category),
                bmiValue = result.value,
                category = result.category,
                hasData = true,
                isLoading = false,
                missingReasonText = null,
            )
        }

        val missingText = summary.missingReason.toMissingText()
        return BmiSummaryUiState(
            bmiText = "Not available",
            categoryText = missingText,
            bmiValue = null,
            category = BmiCategory.UNKNOWN,
            hasData = false,
            isLoading = false,
            missingReasonText = missingText,
        )
    }

    private fun BmiMissingReason?.toMissingText(): String =
        when (this) {
            BmiMissingReason.MISSING_PROFILE -> "Add your profile to calculate BMI"
            BmiMissingReason.MISSING_HEIGHT -> "Add height to calculate BMI"
            BmiMissingReason.MISSING_WEIGHT -> "Add weight to calculate BMI"
            BmiMissingReason.INVALID_INPUT -> "Add height and weight to calculate BMI"
            null -> "Add height and weight to calculate BMI"
        }
}
