package com.burak.healthapp.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.CardFooterLinkRow
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.CircularProgressRing
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.feature.today.meal.MealDraftFoodState
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.TodayUiState
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthFat
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthProtein
import com.burak.healthapp.core.ui.theme.HealthSuccess
import com.burak.healthapp.core.ui.theme.HealthSleep
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthWater
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.burak.healthapp.feature.today.components.ExerciseCard
import com.burak.healthapp.feature.today.components.HydrationCard
import com.burak.healthapp.feature.today.components.NutritionCard
import com.burak.healthapp.feature.today.components.SleepCard
import com.burak.healthapp.feature.today.components.SmokingCard
import com.burak.healthapp.feature.today.components.StepCard
import com.burak.healthapp.feature.today.components.SupplementsCard
import com.burak.healthapp.feature.today.components.WeightCard
import com.burak.healthapp.feature.today.sheet.ExerciseEditorSheet
import com.burak.healthapp.feature.today.sheet.HydrationSheet
import com.burak.healthapp.feature.today.sheet.MealEditorSheet
import com.burak.healthapp.feature.today.sheet.SleepEditorSheet
import com.burak.healthapp.feature.today.sheet.SmokingEditorSheet
import com.burak.healthapp.feature.today.sheet.SupplementDoseSheet
import com.burak.healthapp.feature.today.sheet.WeightEditorSheet
private sealed interface TodaySheet {
    data object Meal : TodaySheet
    data object Exercise : TodaySheet
    data object Hydration : TodaySheet
    data object Sleep : TodaySheet
    data object Weight : TodaySheet
    data object Smoking : TodaySheet
    data object SupplementDose : TodaySheet
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
        onDeleteHydration = actions.onDeleteHydration,
        onDeleteSleep = actions.onDeleteSleep,
        onDeleteExercise = actions.onDeleteExercise,
        onDeleteSmoking = actions.onDeleteSmoking,
        onDeleteSupplementDose = actions.onDeleteSupplementDose,
        onOpenMealHistory = actions.onOpenMealHistory,
        onOpenWeightDetail = actions.onOpenWeightDetail,
        onOpenSleepDetail = actions.onOpenSleepDetail,
        onOpenStepDetail = actions.onOpenStepDetail,
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
    onDeleteHydration: (Long) -> Unit = {},
    onDeleteSleep: () -> Unit = {},
    onDeleteExercise: () -> Unit = {},
    onDeleteSmoking: () -> Unit = {},
    onDeleteSupplementDose: (Long) -> Unit = {},
    onOpenMealHistory: () -> Unit,
    onOpenWeightDetail: () -> Unit,
    onOpenSleepDetail: () -> Unit,
    onOpenStepDetail: () -> Unit = {},
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
) {
    var activeSheet by remember { mutableStateOf<TodaySheet?>(null) }

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
            item {
                NutritionCard(
                    state = state,
                    onAddMeal = { activeSheet = TodaySheet.Meal },
                    onOpenMealHistory = onOpenMealHistory,
                )
            }
            item {
                WeightCard(
                    state = state,
                    onAddWeight = { activeSheet = TodaySheet.Weight },
                    onOpenDetails = onOpenWeightDetail,
                )
            }
            item {
                ExerciseCard(
                    state = state,
                    onAddExercise = { activeSheet = TodaySheet.Exercise },
                    onDeleteExercise = onDeleteExercise,
                )
            }
            item {
                StepCard(
                    state = state,
                    onOpenDetails = onOpenStepDetail,
                )
            }
            item {
                HydrationCard(
                    state = state,
                    onQuickAdd = onAddHydration,
                    onMore = { activeSheet = TodaySheet.Hydration },
                    onDeleteHydration = onDeleteHydration,
                )
            }
            item {
                SleepCard(
                    state = state,
                    onEdit = { activeSheet = TodaySheet.Sleep },
                    onOpenDetails = onOpenSleepDetail,
                    onDeleteSleep = onDeleteSleep,
                )
            }
            item {
                SmokingCard(
                    state = state,
                    onAddSmoking = { activeSheet = TodaySheet.Smoking },
                    onQuickIncrement = onIncrementSmoking,
                    onDeleteSmoking = onDeleteSmoking,
                )
            }
            item {
                SupplementsCard(
                    items = state.supplements.items,
                    onAdd = { activeSheet = TodaySheet.SupplementDose },
                    onDeleteDose = onDeleteSupplementDose,
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

        null -> Unit
    }
}
