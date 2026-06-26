package com.saglik.feature.weight

import androidx.compose.runtime.Immutable
import com.saglik.domain.bmi.BmiCategory

@Immutable
data class WeightDetailUiState(
    val latestWeightText: String,
    val latestEntryText: String,
    val trend: List<Float>,
    val highestWeightText: String,
    val lowestWeightText: String,
    val bmi: WeightBmiUiState,
    val addWeightValue: String,
    val isSaving: Boolean,
    val canSave: Boolean,
    val errorMessage: String?,
    val history: List<WeightHistoryUiState>,
) {
    companion object {
        fun loading(): WeightDetailUiState =
            WeightDetailUiState(
                latestWeightText = "Not available",
                latestEntryText = "No weight entries yet",
                trend = emptyList(),
                highestWeightText = "Not available",
                lowestWeightText = "Not available",
                bmi = WeightBmiUiState.loading(),
                addWeightValue = "",
                isSaving = false,
                canSave = false,
                errorMessage = null,
                history = emptyList(),
            )
    }
}

@Immutable
data class WeightHistoryUiState(
    val id: String,
    val dateText: String,
    val weightText: String,
)

@Immutable
data class WeightBmiUiState(
    val valueText: String,
    val detailText: String,
    val bmiValue: Float?,
    val category: BmiCategory?,
    val hasData: Boolean,
) {
    companion object {
        fun loading(): WeightBmiUiState =
            WeightBmiUiState(
                valueText = "Loading",
                detailText = "Calculating BMI",
                bmiValue = null,
                category = null,
                hasData = false,
            )
    }
}
