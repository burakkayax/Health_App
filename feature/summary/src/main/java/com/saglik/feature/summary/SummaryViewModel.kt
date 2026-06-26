package com.saglik.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.core.model.WeightEntry
import com.saglik.core.model.SleepQuality
import com.saglik.domain.bmi.BmiSummary
import com.saglik.domain.usecase.ObserveBmiSummaryUseCase
import com.saglik.domain.usecase.ObserveLatestWeightEntryUseCase
import com.saglik.domain.usecase.ObserveSleepSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SummaryViewModel @Inject constructor(
    observeLatestWeightEntryUseCase: ObserveLatestWeightEntryUseCase,
    observeBmiSummaryUseCase: ObserveBmiSummaryUseCase,
    observeSleepSummaryUseCase: ObserveSleepSummaryUseCase,
) : ViewModel() {
    val uiState: StateFlow<SummaryUiState> =
        combine(
            observeLatestWeightEntryUseCase(),
            observeBmiSummaryUseCase(),
            observeSleepSummaryUseCase(),
        ) { latestWeight, bmiSummary, sleepSummary ->
            SummaryUiState.loading().copy(
                weight = latestWeight.toWeightSummary(),
                bmi = BmiUiMapper.map(bmiSummary),
                sleep = sleepSummary.toSleepSummaryUiState(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SummaryUiState.loading(),
        )

    private fun WeightEntry?.toWeightSummary(): WeightSummary {
        if (this == null) {
            return WeightSummary(
                value = "Not available",
                delta = "Add weight to start",
                trend = emptyList(),
            )
        }

        return WeightSummary(
            value = String.format(Locale.US, "%.1f kg", weightKg),
            delta = "Latest entry",
            trend = listOf(weightKg - 0.2f, weightKg - 0.1f, weightKg),
        )
    }

    private fun com.saglik.domain.sleep.SleepSummary.toSleepSummaryUiState(): SleepSummaryUiState {
        val duration = latestDurationMinutes
        if (!hasData || duration == null) {
            return SleepSummaryUiState(
                duration = "No sleep yet",
                quality = "Add your first sleep entry",
                weeklyHours = weeklyDurations.map { it.value / 60f },
                hasData = false,
                isLoading = false,
            )
        }

        return SleepSummaryUiState(
            duration = duration.formatDuration(),
            quality = latestQuality.toQualityLabel(),
            weeklyHours = weeklyDurations.map { it.value / 60f },
            hasData = true,
            isLoading = false,
        )
    }

    private fun Int.formatDuration(): String {
        val hours = this / 60
        val minutes = this % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    private fun SleepQuality?.toQualityLabel(): String =
        when (this) {
            SleepQuality.POOR -> "Poor"
            SleepQuality.OKAY -> "Okay"
            SleepQuality.GOOD -> "Good"
            SleepQuality.EXCELLENT -> "Excellent"
            null -> "Sleep logged"
        }
}
