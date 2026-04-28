package com.burak.healthapp.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.TodayUiState
import com.burak.healthapp.feature.today.components.ExerciseCard
import com.burak.healthapp.feature.today.components.HydrationCard
import com.burak.healthapp.feature.today.components.NutritionCard
import com.burak.healthapp.feature.today.components.SleepCard
import com.burak.healthapp.feature.today.components.SmokingCard
import com.burak.healthapp.feature.today.components.StepCard
import com.burak.healthapp.feature.today.components.SupplementsCard
import com.burak.healthapp.feature.today.components.WeightCard
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import com.burak.healthapp.feature.today.sheet.ExerciseEditorSheet
import com.burak.healthapp.feature.today.sheet.HydrationSheet
import com.burak.healthapp.feature.today.sheet.MealEditorSheet
import com.burak.healthapp.feature.today.sheet.SleepEditorSheet
import com.burak.healthapp.feature.today.sheet.SmokingEditorSheet
import com.burak.healthapp.feature.today.sheet.SupplementDoseSheet
import com.burak.healthapp.feature.today.sheet.WeightEditorSheet
import java.time.LocalTime
private sealed interface TodaySheet {
    data object Meal : TodaySheet
    data object Exercise : TodaySheet
    data object Hydration : TodaySheet
    data object Sleep : TodaySheet
    data object Weight : TodaySheet
    data object Smoking : TodaySheet
    data object SupplementDose : TodaySheet
    data object CustomizeDashboard : TodaySheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayContent(
    state: TodayUiState,
    actions: TodayActions,
    mealEditorState: MealEditorUiState,
) {
    TodayContent(
        state = state,
        onAddMeal = actions.onAddMeal,
        onAddHydration = actions.onAddHydration,
        onSaveSleep = actions.onSaveSleep,
        onSaveWeight = actions.onSaveWeight,
        onSaveExercise = actions.onSaveExercise,
        onSaveSmokingCount = actions.onSaveSmokingCount,
        onIncrementSmoking = actions.onIncrementSmoking,
        onSaveSupplementDoses = actions.onSaveSupplementDoses,
        onDeleteSleep = actions.onDeleteSleep,
        onDeleteExercise = actions.onDeleteExercise,
        onDeleteSmoking = actions.onDeleteSmoking,
        onDeleteSupplementDose = actions.onDeleteSupplementDose,
        onOpenMealHistory = actions.onOpenMealHistory,
        onOpenWeightDetail = actions.onOpenWeightDetail,
        onOpenSleepDetail = actions.onOpenSleepDetail,
        onOpenStepDetail = actions.onOpenStepDetail,
        onOpenHydrationDetail = actions.onOpenHydrationDetail,
        mealEditorState = mealEditorState,
        onMealTypeChange = actions.onMealTypeChange,
        onAddMealDraft = actions.onAddMealDraft,
        onRemoveMealDraft = actions.onRemoveMealDraft,
        onMealDraftNameChange = actions.onMealDraftNameChange,
        onMealDraftCaloriesChange = actions.onMealDraftCaloriesChange,
        onMealDraftProteinChange = actions.onMealDraftProteinChange,
        onMealDraftCarbsChange = actions.onMealDraftCarbsChange,
        onMealDraftFatChange = actions.onMealDraftFatChange,
        onResetMealEditor = actions.onResetMealEditor,
        onDashboardCardVisibilityChange = actions.onDashboardCardVisibilityChange,
        onMoveDashboardCard = actions.onMoveDashboardCard,
        onResetDashboardCards = actions.onResetDashboardCards,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayContent(
    state: TodayUiState,
    onAddMeal: (MealType, String, Int, Int, Int, Int) -> Unit,
    onAddHydration: (Int) -> Unit,
    onSaveSleep: (LocalTime, LocalTime) -> Unit,
    onSaveWeight: (Float) -> Unit,
    onSaveExercise: (ExerciseType, Int, ExerciseIntensity) -> Unit,
    onSaveSmokingCount: (Int) -> Unit,
    onIncrementSmoking: () -> Unit,
    onSaveSupplementDoses: (List<SupplementDoseEntry>) -> Unit,
    onDeleteSleep: () -> Unit = {},
    onDeleteExercise: () -> Unit = {},
    onDeleteSmoking: () -> Unit = {},
    onDeleteSupplementDose: (Long) -> Unit = {},
    onOpenMealHistory: () -> Unit,
    onOpenWeightDetail: () -> Unit,
    onOpenSleepDetail: () -> Unit,
    onOpenStepDetail: () -> Unit = {},
    onOpenHydrationDetail: () -> Unit = {},
    mealEditorState: MealEditorUiState,
    onMealTypeChange: (MealType) -> Unit,
    onAddMealDraft: () -> Unit,
    onRemoveMealDraft: (Long) -> Unit,
    onMealDraftNameChange: (Long, String) -> Unit,
    onMealDraftCaloriesChange: (Long, String) -> Unit,
    onMealDraftProteinChange: (Long, String) -> Unit,
    onMealDraftCarbsChange: (Long, String) -> Unit,
    onMealDraftFatChange: (Long, String) -> Unit,
    onResetMealEditor: () -> Unit,
    onDashboardCardVisibilityChange: (DashboardCardType, Boolean) -> Unit = { _, _ -> },
    onMoveDashboardCard: (DashboardCardType, Int) -> Unit = { _, _ -> },
    onResetDashboardCards: () -> Unit = {},
) {
    var activeSheet by remember { mutableStateOf<TodaySheet?>(null) }
    val orderedCards = state.dashboardCards.sortedBy(DashboardCardConfig::sortOrder)
    val visibleCards = orderedCards.filter(DashboardCardConfig::isVisible)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("today_list"),
            contentPadding = PaddingValues(
                start = HealthSpacing.sm,
                end = HealthSpacing.sm,
                top = HealthSpacing.xs,
                bottom = HealthSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            if (visibleCards.isEmpty()) {
                item(key = "empty_dashboard") {
                    EmptyDashboardCard(onCustomize = { activeSheet = TodaySheet.CustomizeDashboard })
                }
            } else {
                items(
                    items = visibleCards,
                    key = { config -> config.type.name },
                ) { config ->
                    when (config.type) {
                        DashboardCardType.NUTRITION -> NutritionCard(
                            state = state,
                            onAddMeal = { activeSheet = TodaySheet.Meal },
                            onOpenMealHistory = onOpenMealHistory,
                        )
                        DashboardCardType.WEIGHT -> WeightCard(
                            state = state,
                            onAddWeight = { activeSheet = TodaySheet.Weight },
                            onOpenDetails = onOpenWeightDetail,
                        )
                        DashboardCardType.HYDRATION -> HydrationCard(
                            state = state,
                            onQuickAdd = onAddHydration,
                            onMore = { activeSheet = TodaySheet.Hydration },
                            onOpenDetails = onOpenHydrationDetail,
                        )
                        DashboardCardType.SLEEP -> SleepCard(
                            state = state,
                            onEdit = { activeSheet = TodaySheet.Sleep },
                            onOpenDetails = onOpenSleepDetail,
                            onDeleteSleep = onDeleteSleep,
                        )
                        DashboardCardType.EXERCISE -> ExerciseCard(
                            state = state,
                            onAddExercise = { activeSheet = TodaySheet.Exercise },
                            onDeleteExercise = onDeleteExercise,
                        )
                        DashboardCardType.SMOKING -> SmokingCard(
                            state = state,
                            onAddSmoking = { activeSheet = TodaySheet.Smoking },
                            onQuickIncrement = onIncrementSmoking,
                            onDeleteSmoking = onDeleteSmoking,
                        )
                        DashboardCardType.SUPPLEMENTS -> SupplementsCard(
                            items = state.supplements.items,
                            onAdd = { activeSheet = TodaySheet.SupplementDose },
                            onDeleteDose = onDeleteSupplementDose,
                        )
                        DashboardCardType.STEPS -> StepCard(
                            state = state,
                            onOpenDetails = onOpenStepDetail,
                        )
                    }
                }
            }
            item(key = "customize_dashboard") {
                RoundedPillButton(
                    label = stringResource(R.string.today_customize_dashboard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("today_customize_dashboard_button"),
                    containerColor = HealthPrimary.copy(alpha = 0.12f),
                    contentColor = HealthPrimary,
                    onClick = { activeSheet = TodaySheet.CustomizeDashboard },
                )
            }
        }
    }

    when (activeSheet) {
        TodaySheet.Meal -> ModalBottomSheet(
            onDismissRequest = {
                onResetMealEditor()
                activeSheet = null
            },
        ) {
            MealEditorSheet(
                state = mealEditorState,
                onMealTypeChange = onMealTypeChange,
                onAddDraft = onAddMealDraft,
                onRemoveDraft = onRemoveMealDraft,
                onDraftNameChange = onMealDraftNameChange,
                onDraftCaloriesChange = onMealDraftCaloriesChange,
                onDraftProteinChange = onMealDraftProteinChange,
                onDraftCarbsChange = onMealDraftCarbsChange,
                onDraftFatChange = onMealDraftFatChange,
                onSaveMeal = {
                    mealEditorState.draftFoods
                        .filter { draft -> draft.name.isNotBlank() }
                        .forEach { draft ->
                            onAddMeal(
                                mealEditorState.mealType,
                                draft.name,
                                draft.calories.toIntOrDefault(0),
                                draft.carbs.toIntOrDefault(0),
                                draft.fat.toIntOrDefault(0),
                                draft.protein.toIntOrDefault(0),
                            )
                        }
                    onResetMealEditor()
                    activeSheet = null
                },
            )
        }

        TodaySheet.Exercise -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            ExerciseEditorSheet(
                currentType = state.exercise.type,
                currentDuration = state.exercise.durationMinutes,
                currentIntensity = state.exercise.intensity,
                onSave = { type, duration, intensity ->
                    onSaveExercise(type, duration, intensity)
                    activeSheet = null
                },
            )
        }

        TodaySheet.Hydration -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            HydrationSheet(
                onSave = {
                    onAddHydration(it)
                    activeSheet = null
                },
            )
        }

        TodaySheet.Sleep -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            SleepEditorSheet(
                timeRangeLabel = state.sleep.timeRangeLabel,
                onSave = { start, end ->
                    onSaveSleep(start, end)
                    activeSheet = null
                },
            )
        }

        TodaySheet.Weight -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            WeightEditorSheet(
                initialWeight = state.weight.currentWeightKg ?: 0f,
                onSave = {
                    onSaveWeight(it)
                    activeSheet = null
                },
            )
        }

        TodaySheet.Smoking -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            SmokingEditorSheet(
                initialCount = state.smoking.count,
                onSave = { count ->
                    onSaveSmokingCount(count)
                    activeSheet = null
                },
            )
        }

        TodaySheet.SupplementDose -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            SupplementDoseSheet(
                items = state.supplements.items,
                onSave = { doses ->
                    onSaveSupplementDoses(doses)
                    activeSheet = null
                },
            )
        }

        TodaySheet.CustomizeDashboard -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            DashboardCustomizationSheet(
                cards = orderedCards,
                onVisibilityChange = onDashboardCardVisibilityChange,
                onMove = onMoveDashboardCard,
                onReset = onResetDashboardCards,
            )
        }

        null -> Unit
    }
}

@Composable
private fun EmptyDashboardCard(onCustomize: () -> Unit) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_empty_state"),
    ) {
        Text(
            text = stringResource(R.string.dashboard_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.dashboard_empty_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        RoundedPillButton(
            label = stringResource(R.string.today_customize_dashboard),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            containerColor = HealthPrimary,
            contentColor = androidx.compose.ui.graphics.Color.White,
            onClick = onCustomize,
        )
    }
}

@Composable
private fun DashboardCustomizationSheet(
    cards: List<DashboardCardConfig>,
    onVisibilityChange: (DashboardCardType, Boolean) -> Unit,
    onMove: (DashboardCardType, Int) -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.sm)
            .testTag("dashboard_customization_sheet"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.dashboard_customize_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        cards.forEachIndexed { index, config ->
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = config.type.label(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    TextButton(
                        enabled = index > 0,
                        modifier = Modifier.testTag("dashboard_card_up_${config.type.name}"),
                        onClick = { onMove(config.type, index - 1) },
                    ) {
                        Text(text = stringResource(R.string.dashboard_move_up))
                    }
                    TextButton(
                        enabled = index < cards.lastIndex,
                        modifier = Modifier.testTag("dashboard_card_down_${config.type.name}"),
                        onClick = { onMove(config.type, index + 1) },
                    ) {
                        Text(text = stringResource(R.string.dashboard_move_down))
                    }
                    Switch(
                        modifier = Modifier.testTag("dashboard_card_switch_${config.type.name}"),
                        checked = config.isVisible,
                        onCheckedChange = { checked -> onVisibilityChange(config.type, checked) },
                    )
                }
            }
        }
        RoundedPillButton(
            label = stringResource(R.string.dashboard_customize_reset),
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            onClick = onReset,
        )
    }
}

@Composable
private fun DashboardCardType.label(): String = when (this) {
    DashboardCardType.NUTRITION -> stringResource(R.string.dashboard_card_nutrition)
    DashboardCardType.WEIGHT -> stringResource(R.string.dashboard_card_weight)
    DashboardCardType.HYDRATION -> stringResource(R.string.dashboard_card_hydration)
    DashboardCardType.SLEEP -> stringResource(R.string.dashboard_card_sleep)
    DashboardCardType.EXERCISE -> stringResource(R.string.dashboard_card_exercise)
    DashboardCardType.SMOKING -> stringResource(R.string.dashboard_card_smoking)
    DashboardCardType.SUPPLEMENTS -> stringResource(R.string.dashboard_card_supplements)
    DashboardCardType.STEPS -> stringResource(R.string.dashboard_card_steps)
}
