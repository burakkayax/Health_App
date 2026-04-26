package com.burak.healthapp.ui.model

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.WaterReminderSettings
import java.time.LocalTime

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
)

data class InsightCardState(
    val title: String,
    val value: String,
    val subtitle: String,
)

data class WeeklyCalorieBarState(
    val label: String,
    val calories: Int,
    val progress: Float,
)

data class WeeklyCaloriesCardState(
    val averageCaloriesLabel: String,
    val subtitle: String,
    val bars: List<WeeklyCalorieBarState>,
)

data class TrendChartState(
    val title: String,
    val subtitle: String,
    val points: List<TrendPoint>,
)

data class StepBarState(
    val label: String,
    val steps: Int,
    val progress: Float,
)

data class StepDetailUiState(
    val selectedPeriod: TrendsPeriod,
    val bars: List<StepBarState>,
    val totalStepsLabel: String,
    val averageStepsLabel: String,
    val targetLabel: String,
    val hasData: Boolean,
)

data class TrendsUiState(
    val avatarInitials: String,
    val selectedPeriod: TrendsPeriod,
    val insights: List<InsightCardState>,
    val weeklyCaloriesCard: WeeklyCaloriesCardState?,
    val charts: List<TrendChartState>,
)

data class MealHistoryEntryState(
    val id: Long,
    val mealType: MealType,
    val name: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
)

data class MealHistorySectionState(
    val title: String,
    val entries: List<MealHistoryEntryState>,
)

data class MealHistoryUiState(
    val sections: List<MealHistorySectionState>,
)

data class ProfileGoalSummaryState(
    val title: String,
    val value: String,
)

data class ProfileSupplementTemplateState(
    val id: Long,
    val name: String,
    val targetAmount: Float,
    val unitLabel: String,
)

data class EditableSupplementTemplateState(
    val draftId: Long,
    val id: Long = 0,
    val name: String = "",
    val targetAmount: String = "",
    val unitLabel: String = "",
    val nameError: String? = null,
    val targetAmountError: String? = null,
    val unitLabelError: String? = null,
)

data class SupplementEditorUiState(
    val isVisible: Boolean = false,
    val drafts: List<EditableSupplementTemplateState> = emptyList(),
    val canSave: Boolean = true,
    val isSaving: Boolean = false,
    val validationMessage: String? = null,
    val saveErrorMessage: String? = null,
)

data class ProfileUiState(
    val userName: String,
    val avatarInitials: String,
    val themeMode: ThemeMode,
    val goalSummaries: List<ProfileGoalSummaryState>,
    val supplementTemplates: List<ProfileSupplementTemplateState>,
    val supplementEditor: SupplementEditorUiState = SupplementEditorUiState(),
)

data class ProfileGoalsUiState(
    val userName: String,
    val avatarInitials: String,
    val goalSettings: GoalSettings,
    val latestMeasurement: BodyMeasurementEntry?,
    val heightCm: Float? = null,
    val waterReminderSettings: WaterReminderSettings = WaterReminderSettings(),
)

data class WeightHistoryItemState(
    val id: Long,
    val dateLabel: String,
    val weightLabel: String,
)

data class BmiGaugeState(
    val indicatorFraction: Float? = null,
    val valueLabel: String? = null,
    val helperMessage: String? = null,
)

data class WeightDetailUiState(
    val chartPoints: List<TrendPoint>,
    val historyItems: List<WeightHistoryItemState>,
    val bmiGauge: BmiGaugeState,
)

data class MealDraftFoodState(
    val draftId: Long,
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
)

data class MealEditorUiState(
    val mealType: MealType = MealType.BREAKFAST,
    val draftFoods: List<MealDraftFoodState> = emptyList(),
    val canSave: Boolean = false,
)

data class SleepBarState(
    val label: String,
    val progress: Float,
    val durationLabel: String,
)

data class SleepCalendarDayState(
    val dayLabel: String,
    val progress: Float,
    val hasData: Boolean,
    val isInCurrentMonth: Boolean,
    val isTargetMet: Boolean,
)

data class SleepCalendarWeekState(
    val days: List<SleepCalendarDayState>,
)

data class SleepRegularityState(
    val title: String,
    val subtitle: String,
    val helperLabel: String,
    val progress: Float?,
    val status: SleepRegularityStatus,
    val isEmpty: Boolean = false,
)

enum class SleepRegularityStatus {
    EMPTY,
    REGULAR,
    VARIABLE,
    IRREGULAR,
}

data class SleepDetailUiState(
    val selectedPeriod: TrendsPeriod,
    val bars: List<SleepBarState>,
    val regularity: SleepRegularityState,
    val hasData: Boolean,
    val targetLabel: String,
    val bedtimeLabel: String,
    val wakeLabel: String,
    val calendarWeeks: List<SleepCalendarWeekState> = emptyList(),
)

fun EditableSupplementTemplateState.toDomainTemplate(sortOrder: Int): SupplementTemplate {
    return SupplementTemplate(
        id = id,
        name = name.trim(),
        targetAmount = targetAmount.toFloatOrNull() ?: 0f,
        unitLabel = unitLabel.trim(),
        sortOrder = sortOrder,
    )
}
