@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.feature.weight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.core.model.WeightEntry
import com.saglik.domain.bmi.BmiCategory
import com.saglik.domain.bmi.BmiFormatter
import com.saglik.domain.bmi.BmiMissingReason
import com.saglik.domain.bmi.BmiSummary
import com.saglik.domain.usecase.AddWeightEntryUseCase
import com.saglik.domain.usecase.ObserveBmiSummaryUseCase
import com.saglik.domain.usecase.ObserveWeightTrendSummaryUseCase
import com.saglik.domain.usecase.WeightTrendSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WeightDetailViewModel @Inject constructor(
    observeWeightTrendSummaryUseCase: ObserveWeightTrendSummaryUseCase,
    observeBmiSummaryUseCase: ObserveBmiSummaryUseCase,
    private val addWeightEntryUseCase: AddWeightEntryUseCase,
) : ViewModel() {
    private val formState = MutableStateFlow(WeightFormState())

    val uiState: StateFlow<WeightDetailUiState> =
        combine(
            observeWeightTrendSummaryUseCase(),
            observeBmiSummaryUseCase(),
            formState,
        ) { trendSummary, bmiSummary, form ->
            WeightDetailUiState.loading().copy(
                latestWeightText = trendSummary.latestEntry.formatWeight(),
                latestEntryText = if (trendSummary.latestEntry == null) {
                    "No weight entries yet"
                } else {
                    "Latest entry"
                },
                trend = trendSummary.chartPoints.map { it.weightKg },
                highestWeightText = trendSummary.highestKg.formatWeightValue(),
                lowestWeightText = trendSummary.lowestKg.formatWeightValue(),
                bmi = bmiSummary.toWeightBmiUiState(),
                addWeightValue = form.weightText,
                isSaving = form.isSaving,
                canSave = form.weightText.toFloatOrNull().isValidWeight() && !form.isSaving,
                errorMessage = form.errorMessage,
                history = trendSummary.toHistoryRows(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeightDetailUiState.loading(),
        )

    fun onWeightInputChanged(value: String) {
        formState.update {
            it.copy(
                weightText = value.cleanDecimal(maxLength = 5),
                errorMessage = null,
            )
        }
    }

    fun addWeight() {
        val weightKg = formState.value.weightText.toFloatOrNull()
        if (!weightKg.isValidWeight()) {
            formState.update { it.copy(errorMessage = WeightFormState.RangeErrorMessage) }
            return
        }
        val validWeightKg = weightKg ?: return

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }
            val saved = runCatching { addWeightEntryUseCase(validWeightKg) }.getOrDefault(false)
            formState.update {
                if (saved) {
                    WeightFormState()
                } else {
                    it.copy(
                        isSaving = false,
                        errorMessage = WeightFormState.RangeErrorMessage,
                    )
                }
            }
        }
    }

    private fun WeightEntry?.formatWeight(): String =
        if (this == null) {
            "Not available"
        } else {
            String.format(Locale.US, "%.1f kg", weightKg)
        }

    private fun Float?.formatWeightValue(): String =
        if (this == null) "Not available" else String.format(Locale.US, "%.1f kg", this)

    private fun BmiSummary.toWeightBmiUiState(): WeightBmiUiState {
        val result = bmi
        if (hasData && result != null) {
            return WeightBmiUiState(
                valueText = BmiFormatter.formatValue(result.value),
                detailText = BmiFormatter.categoryLabel(result.category),
                bmiValue = result.value,
                category = result.category,
                hasData = true,
            )
        }

        return WeightBmiUiState(
            valueText = "Not available",
            detailText = missingReason.toMissingText(),
            bmiValue = null,
            category = BmiCategory.UNKNOWN,
            hasData = false,
        )
        }

    private fun BmiMissingReason?.toMissingText(): String =
        when (this) {
            BmiMissingReason.MISSING_PROFILE -> "Height and weight are required to calculate BMI."
            BmiMissingReason.MISSING_HEIGHT -> "Height and weight are required to calculate BMI."
            BmiMissingReason.MISSING_WEIGHT -> "Height and weight are required to calculate BMI."
            BmiMissingReason.INVALID_INPUT -> "Height and weight are required to calculate BMI."
            null -> "Height and weight are required to calculate BMI."
        }

    private fun WeightTrendSummary.toHistoryRows(): List<WeightHistoryUiState> =
        history.map { entry ->
            WeightHistoryUiState(
                id = entry.id,
                dateText = entry.toHistoryDateText(),
                weightText = entry.formatWeight(),
            )
        }

    private fun WeightEntry.toHistoryDateText(): String {
        val zone = ZoneId.systemDefault()
        val dateTime = java.time.Instant.ofEpochMilli(recordedAt.toEpochMilliseconds()).atZone(zone)
        val date = dateTime.toLocalDate()
        val today = LocalDate.now(zone)
        return when (date) {
            today -> "Today, ${dateTime.format(HistoryTimeFormatter)}"
            today.minusDays(1) -> "Yesterday"
            else -> date.format(HistoryDateFormatter)
        }
    }

    private fun Float?.isValidWeight(): Boolean =
        this != null &&
            isFinite() &&
            this in AddWeightEntryUseCase.MIN_WEIGHT_KG..AddWeightEntryUseCase.MAX_WEIGHT_KG

    private fun String.cleanDecimal(maxLength: Int): String {
        val builder = StringBuilder()
        var hasSeparator = false
        for (char in this) {
            when {
                char.isDigit() -> builder.append(char)
                (char == '.' || char == ',') && !hasSeparator -> {
                    builder.append('.')
                    hasSeparator = true
                }
            }
            if (builder.length >= maxLength) break
        }
        return builder.toString()
    }

    companion object {
        private val HistoryTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
        private val HistoryDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    }
}

private data class WeightFormState(
    val weightText: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    companion object {
        const val RangeErrorMessage = "Enter a weight between 30 and 300 kg."
    }
}
