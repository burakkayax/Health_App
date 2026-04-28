package com.burak.healthapp.feature.today

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.defaultDashboardCardConfig

data class MacroRingState(
    val label: String,
    val current: Int,
    val target: Int,
    val progress: Float,
    val isEmphasized: Boolean = false,
)

data class NutritionCardState(
    val currentCalories: Int,
    val targetCalories: Int,
    val progress: Float,
    val macros: List<MacroRingState>,
    val entries: List<MealEntry>,
)

data class WeightCardState(
    val currentWeightKg: Float?,
    val targetWeightKg: Float,
    val progress: Float,
    val hasMeasurement: Boolean,
    val headline: String,
    val supportingLabel: String,
    val helperLabel: String,
)

data class HydrationCardState(
    val currentMl: Int,
    val targetMl: Int,
    val progress: Float,
    val entries: List<HydrationEntry> = emptyList(),
)

data class SleepCardState(
    val durationLabel: String,
    val timeRangeLabel: String,
    val targetLabel: String,
    val progress: Float,
)

data class ExerciseCardState(
    val type: ExerciseType?,
    val durationMinutes: Int,
    val intensity: ExerciseIntensity?,
    val progress: Float,
    val title: String,
    val durationLabel: String,
    val intensityLabel: String,
    val helperLabel: String,
)

data class SmokingCardState(
    val count: Int,
    val limit: Int,
    val progress: Float,
    val headline: String,
    val supportingLabel: String,
    val helperLabel: String,
    val status: SmokingStatus,
)

data class StepCardState(
    val currentSteps: Int,
    val targetSteps: Int,
    val progress: Float,
    val headline: String,
    val supportingLabel: String,
    val helperLabel: String,
)

enum class SmokingStatus {
    PASSIVE,
    SAFE,
    WARNING,
    DANGER,
}

data class SupplementItemState(
    val id: Long,
    val name: String,
    val currentAmount: Float,
    val targetAmount: Float,
    val unitLabel: String,
    val progress: Float,
)

data class SupplementCardState(
    val items: List<SupplementItemState>,
)

data class TodayUiState(
    val userName: String,
    val avatarInitials: String,
    val goalSettings: GoalSettings,
    val latestMeasurement: BodyMeasurementEntry?,
    val nutrition: NutritionCardState,
    val weight: WeightCardState,
    val exercise: ExerciseCardState,
    val hydration: HydrationCardState,
    val sleep: SleepCardState,
    val smoking: SmokingCardState,
    val steps: StepCardState,
    val supplements: SupplementCardState,
    val dashboardCards: List<DashboardCardConfig> = defaultDashboardCardConfig(),
)
