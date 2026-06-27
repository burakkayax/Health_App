package com.saglik.feature.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.core.model.ExerciseSummary
import com.saglik.core.model.ExerciseType
import com.saglik.core.model.SleepQuality
import com.saglik.core.model.WeightEntry
import com.saglik.domain.steps.StepsSummary
import com.saglik.domain.usecase.ObserveBmiSummaryUseCase
import com.saglik.domain.usecase.ObserveExerciseSummaryUseCase
import com.saglik.domain.usecase.ObserveLatestWeightEntryUseCase
import com.saglik.domain.usecase.ObserveSleepSummaryUseCase
import com.saglik.domain.usecase.ObserveStepsSummaryUseCase
import com.saglik.domain.usecase.water.ObserveWaterSummaryUseCase
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
    observeStepsSummaryUseCase: ObserveStepsSummaryUseCase,
    observeExerciseSummaryUseCase: ObserveExerciseSummaryUseCase,
    observeWaterSummaryUseCase: ObserveWaterSummaryUseCase,
) : ViewModel() {
    val uiState: StateFlow<SummaryUiState> =
        combine(
            combine(
                observeLatestWeightEntryUseCase(),
                observeBmiSummaryUseCase(),
                observeSleepSummaryUseCase(),
                ::Triple
            ),
            combine(
                observeStepsSummaryUseCase(),
                observeExerciseSummaryUseCase(),
                observeWaterSummaryUseCase(),
                ::Triple
            )
        ) { (latestWeight, bmiSummary, sleepSummary), (stepsSummary, exerciseSummary, waterSummary) ->
            SummaryUiState.loading().copy(
                weight = latestWeight.toWeightSummary(),
                bmi = BmiUiMapper.map(bmiSummary),
                sleep = sleepSummary.toSleepSummaryUiState(),
                steps = stepsSummary.toStepsSummaryUiState(),
                exercise = exerciseSummary.toExerciseSummaryUiState(),
                water = waterSummary.toWaterSummaryUiState(),
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

    private fun StepsSummary.toStepsSummaryUiState(): StepsSummaryUiState {
        if (!hasData) {
            return StepsSummaryUiState(
                primaryText = "No steps yet",
                secondaryText = "Sync Health Connect to import steps",
                weeklyText = "Last 7 days unavailable",
                hasData = false,
                isLoading = false,
            )
        }

        return StepsSummaryUiState(
            primaryText = String.format(Locale.US, "%,d steps", totalStepsToday),
            secondaryText = "Today",
            weeklyText = String.format(Locale.US, "%,d in 7 days", totalStepsLast7Days),
            hasData = true,
            isLoading = false,
        )
    }

    private fun ExerciseSummary.toExerciseSummaryUiState(): ExerciseSummaryUiState {
        val latestSession = mostRecentSession
        if (sessionCount == 0 || latestSession == null) {
            return ExerciseSummaryUiState(
                primaryText = "No exercise yet",
                secondaryText = "Sync Health Connect or add a session later",
                latestText = "No sessions logged",
                hasData = false,
                isLoading = false,
            )
        }

        return ExerciseSummaryUiState(
            primaryText = sessionCount.formatSessionCount(),
            secondaryText = String.format(Locale.US, "%d min total", totalDurationMinutes),
            latestText = latestSession.title?.trim()?.takeIf { it.isNotEmpty() }
                ?: latestSession.exerciseType.toDisplayText(),
            hasData = true,
            isLoading = false,
        )
    }

    private fun Int.formatSessionCount(): String =
        if (this == 1) "1 session" else "$this sessions"

    private fun ExerciseType.toDisplayText(): String =
        when (this) {
            ExerciseType.WALKING -> "Walking"
            ExerciseType.RUNNING -> "Running"
            ExerciseType.CYCLING -> "Cycling"
            ExerciseType.SWIMMING -> "Swimming"
            ExerciseType.STRENGTH_TRAINING -> "Strength training"
            ExerciseType.YOGA -> "Yoga"
            ExerciseType.HIIT -> "HIIT"
            ExerciseType.OTHER -> "Latest session logged"
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

    private fun com.saglik.core.model.WaterSummary.toWaterSummaryUiState(): WaterSummaryUiState {
        if (!hasData) {
            return WaterSummaryUiState(
                primaryText = "No water yet",
                secondaryText = "Add your first water entry",
                weeklyText = "Last 7 days unavailable",
                hasData = false,
                isLoading = false,
            )
        }

        return WaterSummaryUiState(
            primaryText = String.format(Locale.US, "%,d ml", totalTodayMl),
            secondaryText = "Today",
            weeklyText = String.format(Locale.US, "%,d ml in 7 days", totalLast7DaysMl),
            hasData = true,
            isLoading = false,
        )
    }
}
