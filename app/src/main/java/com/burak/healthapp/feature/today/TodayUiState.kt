package com.burak.healthapp.feature.today

import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.CaffeineEntry
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
    val title: UiText,
    val durationLabel: UiText,
    val intensityLabel: UiText,
    val helperLabel: UiText,
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
    val weeklySteps: Int = 0,
)

data class CaffeineCardState(
    val dailyTotalMg: Int,
    val limitMg: Int,
    val progress: Float,
    val lastCaffeineTimeLabel: String,
    val overDailyLimit: Boolean,
    val afterCutoff: Boolean,
    val withinSleepBuffer: Boolean,
    val entries: List<CaffeineEntry>,
)

enum class SmokingStatus {
    PASSIVE,
    SAFE,
    NEUTRAL,
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
    val isLoading: Boolean = false,
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
    val caffeine: CaffeineCardState,
    val supplements: SupplementCardState,
    val dashboardCards: List<DashboardCardConfig> = defaultDashboardCardConfig(),
)
