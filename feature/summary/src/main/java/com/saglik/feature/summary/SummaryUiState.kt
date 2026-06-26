package com.saglik.feature.summary

import androidx.compose.runtime.Immutable
import com.saglik.domain.bmi.BmiCategory

@Immutable
data class SummaryUiState(
    val selectedTabRoute: String,
    val weight: WeightSummary,
    val bmi: BmiSummaryUiState,
    val sleep: SleepSummaryUiState,
    val activity: ActivitySummary,
    val mood: MoodSummary,
) {
    companion object {
        fun loading(): SummaryUiState = SummaryUiState(
            selectedTabRoute = SummaryRoute.route,
            weight = WeightSummary(
                value = "Not available",
                delta = "Add weight to start",
                trend = emptyList(),
            ),
            bmi = BmiSummaryUiState.loading(),
            sleep = SleepSummaryUiState.loading(),
            activity = ActivitySummary(
                move = "265 cal",
                exercise = "11 min",
                stand = "3 hr",
            ),
            mood = MoodSummary(
                title = "A Slightly Pleasant Moment",
                tags = "Happy - Health - Fitness",
            ),
        )
    }
}

@Immutable
data class WeightSummary(
    val value: String,
    val delta: String,
    val trend: List<Float>,
)

@Immutable
data class BmiSummaryUiState(
    val bmiText: String,
    val categoryText: String,
    val bmiValue: Float?,
    val category: BmiCategory?,
    val hasData: Boolean,
    val isLoading: Boolean,
    val missingReasonText: String?,
) {
    companion object {
        fun loading(): BmiSummaryUiState =
            BmiSummaryUiState(
                bmiText = "Loading",
                categoryText = "Calculating BMI",
                bmiValue = null,
                category = null,
                hasData = false,
                isLoading = true,
                missingReasonText = null,
            )
    }
}

@Immutable
data class SleepSummaryUiState(
    val duration: String,
    val quality: String?,
    val weeklyHours: List<Float>,
    val hasData: Boolean,
    val isLoading: Boolean,
) {
    companion object {
        fun loading(): SleepSummaryUiState =
            SleepSummaryUiState(
                duration = "Loading",
                quality = "Loading sleep",
                weeklyHours = emptyList(),
                hasData = false,
                isLoading = true,
            )
    }
}

@Immutable
data class ActivitySummary(
    val move: String,
    val exercise: String,
    val stand: String,
)

@Immutable
data class MoodSummary(
    val title: String,
    val tags: String,
)
