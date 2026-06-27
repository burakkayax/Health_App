package com.saglik.feature.summary

import androidx.compose.runtime.Immutable
import com.saglik.domain.bmi.BmiCategory

@Immutable
data class SummaryUiState(
    val selectedTabRoute: String,
    val weight: WeightSummary,
    val bmi: BmiSummaryUiState,
    val sleep: SleepSummaryUiState,
    val steps: StepsSummaryUiState,
    val exercise: ExerciseSummaryUiState,
    val water: WaterSummaryUiState,
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
            steps = StepsSummaryUiState.loading(),
            exercise = ExerciseSummaryUiState.loading(),
            water = WaterSummaryUiState.loading(),
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
data class StepsSummaryUiState(
    val primaryText: String,
    val secondaryText: String,
    val weeklyText: String,
    val hasData: Boolean,
    val isLoading: Boolean,
) {
    companion object {
        fun loading(): StepsSummaryUiState =
            StepsSummaryUiState(
                primaryText = "Loading",
                secondaryText = "Reading steps",
                weeklyText = "Last 7 days unavailable",
                hasData = false,
                isLoading = true,
            )
    }
}

@Immutable
data class ExerciseSummaryUiState(
    val primaryText: String,
    val secondaryText: String,
    val latestText: String,
    val hasData: Boolean,
    val isLoading: Boolean,
) {
    companion object {
        fun loading(): ExerciseSummaryUiState =
            ExerciseSummaryUiState(
                primaryText = "Loading",
                secondaryText = "Reading sessions",
                latestText = "No sessions logged",
                hasData = false,
                isLoading = true,
            )
    }
}

@Immutable
data class MoodSummary(
    val title: String,
    val tags: String,
)

@Immutable
data class WaterSummaryUiState(
    val primaryText: String,
    val secondaryText: String,
    val weeklyText: String,
    val hasData: Boolean,
    val isLoading: Boolean,
) {
    companion object {
        fun loading(): WaterSummaryUiState =
            WaterSummaryUiState(
                primaryText = "Loading",
                secondaryText = "Reading water logs",
                weeklyText = "Last 7 days unavailable",
                hasData = false,
                isLoading = true,
            )
    }
}
