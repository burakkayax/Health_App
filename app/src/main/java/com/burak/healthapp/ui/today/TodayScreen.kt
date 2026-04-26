package com.burak.healthapp.ui.today

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
import com.burak.healthapp.ui.components.HealthPillTextField
import com.burak.healthapp.ui.components.HealthCard
import com.burak.healthapp.ui.components.CardFooterLinkRow
import com.burak.healthapp.ui.components.CardHeaderActionButton
import com.burak.healthapp.ui.components.CircularProgressRing
import com.burak.healthapp.ui.components.RoundedPillButton
import com.burak.healthapp.ui.components.SegmentedControl
import com.burak.healthapp.ui.components.SectionTitle
import com.burak.healthapp.ui.model.MealDraftFoodState
import com.burak.healthapp.ui.model.MealEditorUiState
import com.burak.healthapp.ui.model.SmokingStatus
import com.burak.healthapp.ui.model.SupplementItemState
import com.burak.healthapp.ui.model.TodayUiState
import com.burak.healthapp.ui.theme.HealthCarbs
import com.burak.healthapp.ui.theme.HealthFat
import com.burak.healthapp.ui.theme.HealthPrimary
import com.burak.healthapp.ui.theme.HealthProtein
import com.burak.healthapp.ui.theme.HealthSuccess
import com.burak.healthapp.ui.theme.HealthSleep
import com.burak.healthapp.ui.theme.HealthSpacing
import com.burak.healthapp.ui.theme.HealthWater
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private sealed interface TodaySheet {
    data object Meal : TodaySheet
    data object Exercise : TodaySheet
    data object Hydration : TodaySheet
    data object Sleep : TodaySheet
    data object Weight : TodaySheet
    data object Smoking : TodaySheet
    data object SupplementDose : TodaySheet
}

@Composable
fun TodayRoute(
    selectedDate: LocalDate,
    onOpenMealHistory: () -> Unit,
    onOpenWeightDetail: () -> Unit,
    onOpenSleepDetail: () -> Unit,
    onOpenStepDetail: () -> Unit,
) {
    val viewModel: TodayViewModel = viewModel(factory = TodayViewModel.Factory)
    val mealEditorViewModel: MealEditorViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mealEditorState by mealEditorViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    TodayContent(
        state = uiState,
        onAddMeal = viewModel::addMeal,
        onAddHydration = viewModel::addHydration,
        onSaveSleep = viewModel::saveSleep,
        onSaveWeight = viewModel::saveWeight,
        onSaveExercise = viewModel::saveExercise,
        onSaveSmokingCount = viewModel::saveSmokingCount,
        onIncrementSmoking = viewModel::incrementSmoking,
        onSaveSupplementDoses = viewModel::saveSupplementDoses,
        onDeleteHydration = viewModel::deleteHydrationEntry,
        onDeleteSleep = viewModel::deleteSleep,
        onDeleteExercise = viewModel::deleteExercise,
        onDeleteSmoking = viewModel::deleteSmoking,
        onDeleteSupplementDose = viewModel::deleteSupplementDose,
        onOpenMealHistory = onOpenMealHistory,
        onOpenWeightDetail = onOpenWeightDetail,
        onOpenSleepDetail = onOpenSleepDetail,
        onOpenStepDetail = onOpenStepDetail,
        mealEditorState = mealEditorState,
        onMealTypeChange = mealEditorViewModel::setMealType,
        onAddMealDraft = mealEditorViewModel::addDraftFood,
        onRemoveMealDraft = mealEditorViewModel::removeDraftFood,
        onMealDraftNameChange = mealEditorViewModel::updateDraftName,
        onMealDraftCaloriesChange = mealEditorViewModel::updateDraftCalories,
        onMealDraftProteinChange = mealEditorViewModel::updateDraftProtein,
        onMealDraftCarbsChange = mealEditorViewModel::updateDraftCarbs,
        onMealDraftFatChange = mealEditorViewModel::updateDraftFat,
        onResetMealEditor = mealEditorViewModel::reset,
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

@Composable
private fun NutritionCard(
    state: TodayUiState,
    onAddMeal: () -> Unit,
    onOpenMealHistory: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nutrition_card"),
        footer = {
            CardFooterLinkRow(
                label = "Bugünün Öğünleri",
                modifier = Modifier.testTag("nutrition_history_link"),
                onClick = onOpenMealHistory,
            )
        },
    ) {
        SectionTitle(
            title = "Beslenme ve Makrolar",
            trailing = {
                CardHeaderActionButton(
                    modifier = Modifier.testTag("nutrition_add_button"),
                    onClick = onAddMeal,
                )
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressRing(
                progress = state.nutrition.progress,
                color = HealthPrimary,
                modifier = Modifier.width(220.dp).height(220.dp),
                strokeWidth = 12.dp,
                trackColor = HealthPrimary.copy(alpha = 0.15f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = HealthSpacing.md),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${state.nutrition.currentCalories}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "/ ${state.nutrition.targetCalories} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm, bottom = HealthSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            state.nutrition.macros.forEach { macro ->
                val color = when {
                    macro.label == "Karb" -> HealthCarbs
                    macro.label.startsWith("Ya") -> HealthFat
                    else -> HealthProtein
                }
                val trackColor = when {
                    macro.label == "Karb" -> HealthCarbs.copy(alpha = 0.15f)
                    macro.label.startsWith("Ya") -> HealthFat.copy(alpha = 0.15f)
                    else -> HealthProtein.copy(alpha = 0.15f)
                }
                CircularProgressRing(
                    progress = macro.progress,
                    color = color,
                    modifier = Modifier.width(if (macro.isEmphasized) 100.dp else 92.dp).height(if (macro.isEmphasized) 100.dp else 92.dp),
                    strokeWidth = if (macro.isEmphasized) 11.dp else 10.dp,
                    trackColor = trackColor,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = HealthSpacing.xs),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${macro.current}g",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = macro.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactRingMetricLayout(
    progress: Float,
    color: Color,
    headline: String,
    supportingLabel: String,
    helperLabel: String,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.14f),
    bottomContent: (@Composable () -> Unit)? = null,
    ringContent: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HealthSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressRing(
            progress = progress,
            color = color,
            modifier = Modifier.size(104.dp),
            strokeWidth = 10.dp,
            trackColor = trackColor,
        ) {
            ringContent()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = supportingLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (helperLabel.isNotBlank()) {
                Text(
                    text = helperLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            bottomContent?.invoke()
        }
    }
}

@Composable
private fun WeightCard(
    state: TodayUiState,
    onAddWeight: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    val locale = remember { Locale.forLanguageTag("tr") }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_card")
            .clickable(onClick = onOpenDetails),
    ) {
        SectionTitle(
            title = "Kilo",
            trailing = {
                CardHeaderActionButton(
                    modifier = Modifier.testTag("weight_add_button"),
                    onClick = onAddWeight,
                )
            },
        )
        CompactRingMetricLayout(
            progress = state.weight.progress,
            color = HealthPrimary,
            headline = state.weight.headline,
            supportingLabel = state.weight.supportingLabel,
            helperLabel = state.weight.helperLabel,
        ) {
            Text(
                text = state.weight.currentWeightKg?.let { String.format(locale, "%.1f", it) } ?: "--",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    state: TodayUiState,
    onAddExercise: () -> Unit,
    onDeleteExercise: () -> Unit,
) {
    val ringColor = if (state.exercise.type == null) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        HealthPrimary
    }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exercise_card"),
    ) {
        SectionTitle(
            title = "Egzersiz",
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    if (state.exercise.type != null) {
                        DeleteIconButton(
                            testTag = "exercise_delete_button",
                            contentDescription = "Egzersiz kaydını sil",
                            onClick = onDeleteExercise,
                        )
                    }
                    CardHeaderActionButton(
                        modifier = Modifier.testTag("exercise_add_button"),
                        onClick = onAddExercise,
                    )
                }
            },
        )
        CompactRingMetricLayout(
            progress = state.exercise.progress,
            color = ringColor,
            headline = state.exercise.title,
            supportingLabel = state.exercise.durationLabel,
            helperLabel = "${state.exercise.intensityLabel} • ${state.exercise.helperLabel}",
        ) {
            Icon(
                imageVector = state.exercise.type.toExerciseIcon(),
                contentDescription = null,
                tint = ringColor,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun StepCard(
    state: TodayUiState,
    onOpenDetails: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("steps_card")
            .clickable(onClick = onOpenDetails),
    ) {
        SectionTitle(title = "Adım Sayar")
        CompactRingMetricLayout(
            progress = state.steps.progress,
            color = HealthPrimary,
            headline = state.steps.headline,
            supportingLabel = state.steps.supportingLabel,
            helperLabel = state.steps.helperLabel,
            trackColor = HealthPrimary.copy(alpha = 0.14f),
        ) {
            Icon(
                imageVector = Icons.Rounded.DirectionsWalk,
                contentDescription = null,
                tint = HealthPrimary,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun HydrationCard(
    state: TodayUiState,
    onQuickAdd: (Int) -> Unit,
    onMore: () -> Unit,
    onDeleteHydration: (Long) -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hydration_card"),
    ) {
        SectionTitle(
            title = "Su Tüketimi",
            trailing = {
                CardHeaderActionButton(
                    modifier = Modifier.testTag("hydration_add_button"),
                    onClick = onMore,
                )
            },
        )
        Column(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                Text(
                    text = "${state.hydration.currentMl} ml",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "/ ${state.hydration.targetMl} ml",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(999.dp),
                    ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(state.hydration.progress.coerceIn(0f, 1f))
                        .height(12.dp)
                        .background(
                            color = HealthWater,
                            shape = RoundedCornerShape(999.dp),
                        ),
                )
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                RoundedPillButton(
                    label = "+200 ml",
                    onClick = { onQuickAdd(200) },
                )
                RoundedPillButton(
                    label = "+500 ml",
                    onClick = { onQuickAdd(500) },
                )
            }
            if (state.hydration.entries.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    state.hydration.entries.forEachIndexed { index, entry ->
                        if (index > 0) {
                            HorizontalDivider()
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("hydration_entry_${entry.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "${entry.amountMl} ml",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            DeleteIconButton(
                                testTag = "hydration_delete_${entry.id}",
                                contentDescription = "Su kaydını sil",
                                onClick = { onDeleteHydration(entry.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepCard(
    state: TodayUiState,
    onEdit: () -> Unit,
    onOpenDetails: () -> Unit,
    onDeleteSleep: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sleep_card")
            .clickable(onClick = onOpenDetails),
    ) {
        SectionTitle(
            title = "Uyku Takibi",
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    if (state.sleep.progress > 0f) {
                        DeleteIconButton(
                            testTag = "sleep_delete_button",
                            contentDescription = "Uyku kaydını sil",
                            onClick = onDeleteSleep,
                        )
                    }
                    CardHeaderActionButton(
                        modifier = Modifier.testTag("sleep_add_button"),
                        onClick = onEdit,
                    )
                }
            },
        )
        CompactRingMetricLayout(
            progress = state.sleep.progress,
            color = HealthSleep,
            headline = state.sleep.durationLabel,
            supportingLabel = state.sleep.timeRangeLabel,
            helperLabel = "Hedef ${state.sleep.targetLabel}",
            trackColor = HealthSleep.copy(alpha = 0.14f),
        ) {
            Text(
                text = if (state.sleep.progress > 0f) state.sleep.durationLabel else "--",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun SmokingCard(
    state: TodayUiState,
    onAddSmoking: () -> Unit,
    onQuickIncrement: () -> Unit,
    onDeleteSmoking: () -> Unit,
) {
    val ringColor = when (state.smoking.status) {
        SmokingStatus.PASSIVE -> MaterialTheme.colorScheme.onSurfaceVariant
        SmokingStatus.SAFE -> HealthSuccess
        SmokingStatus.WARNING -> HealthCarbs
        SmokingStatus.DANGER -> MaterialTheme.colorScheme.error
    }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smoking_card"),
    ) {
        SectionTitle(
            title = "Sigara",
            trailing = {
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    if (state.smoking.count > 0) {
                        DeleteIconButton(
                            testTag = "smoking_delete_button",
                            contentDescription = "Sigara kaydını sil",
                            onClick = onDeleteSmoking,
                        )
                    }
                    CardHeaderActionButton(
                        modifier = Modifier.testTag("smoking_add_button"),
                        onClick = onAddSmoking,
                    )
                }
            },
        )
        CompactRingMetricLayout(
            progress = state.smoking.progress,
            color = ringColor,
            headline = state.smoking.headline,
            supportingLabel = state.smoking.supportingLabel,
            helperLabel = state.smoking.helperLabel,
            trackColor = ringColor.copy(alpha = 0.14f),
            bottomContent = {
                RoundedPillButton(
                    label = "+1",
                    modifier = Modifier.testTag("smoking_quick_add_button"),
                    containerColor = ringColor.copy(alpha = 0.14f),
                    contentColor = ringColor,
                    onClick = onQuickIncrement,
                )
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = ringColor,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun SupplementsCard(
    items: List<SupplementItemState>,
    onAdd: () -> Unit,
    onDeleteDose: (Long) -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplements_card"),
    ) {
        SectionTitle(
            title = "Takviyeler",
            trailing = {
                CardHeaderActionButton(
                    modifier = Modifier.testTag("supplements_add_button"),
                    onClick = onAdd,
                )
            },
        )
        if (items.isEmpty()) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.sm),
                text = "Aktif takviye bulunmuyor. Profil ekranından ekleyebilirsin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = HealthSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                items(items, key = { it.id }) { item ->
                    SupplementRingItem(
                        item = item,
                        onDeleteDose = onDeleteDose,
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplementRingItem(
    item: SupplementItemState,
    onDeleteDose: (Long) -> Unit,
) {
    Column(
        modifier = Modifier.width(112.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        CircularProgressRing(
            progress = item.progress,
            color = HealthPrimary,
            modifier = Modifier.size(88.dp),
            strokeWidth = 7.dp,
            trackColor = HealthPrimary.copy(alpha = 0.14f),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = formatFloat(item.currentAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.unitLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${formatFloat(item.currentAmount)}/${formatFloat(item.targetAmount)}${item.unitLabel}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (item.currentAmount > 0f) {
            DeleteIconButton(
                testTag = "supplement_dose_delete_${item.id}",
                contentDescription = "Takviye dozunu sil",
                onClick = { onDeleteDose(item.id) },
            )
        }
    }
}

@Composable
private fun DeleteIconButton(
    testTag: String,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.testTag(testTag),
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MealEditorSheet(
    state: MealEditorUiState,
    onMealTypeChange: (MealType) -> Unit,
    onAddDraft: () -> Unit,
    onRemoveDraft: (Long) -> Unit,
    onDraftNameChange: (Long, String) -> Unit,
    onDraftCaloriesChange: (Long, String) -> Unit,
    onDraftProteinChange: (Long, String) -> Unit,
    onDraftCarbsChange: (Long, String) -> Unit,
    onDraftFatChange: (Long, String) -> Unit,
    onSaveMeal: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        contentPadding = PaddingValues(bottom = HealthSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            Text(
                text = "Öğün Ekle",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                MealType.entries.forEach { mealType ->
                    FilterChip(
                        selected = state.mealType == mealType,
                        onClick = { onMealTypeChange(mealType) },
                        label = { Text(mealType.label) },
                    )
                }
            }
        }
        items(
            items = state.draftFoods,
            key = MealDraftFoodState::draftId,
        ) { draft ->
            HealthCard(modifier = Modifier.testTag("meal_draft_${draft.draftId}")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Besin",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.draftFoods.size > 1) {
                        RoundedPillButton(
                            label = "Sil",
                            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                            contentColor = MaterialTheme.colorScheme.error,
                            onClick = { onRemoveDraft(draft.draftId) },
                        )
                    }
                }
                HealthPillTextField(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    value = draft.name,
                    onValueChange = { onDraftNameChange(draft.draftId, it) },
                    label = "Öğe Adı",
                )
                NumberFieldRow(
                    leftLabel = "Kalori",
                    leftValue = draft.calories,
                    rightLabel = "Protein",
                    rightValue = draft.protein,
                    onLeftChange = { onDraftCaloriesChange(draft.draftId, it) },
                    onRightChange = { onDraftProteinChange(draft.draftId, it) },
                )
                NumberFieldRow(
                    leftLabel = "Karb",
                    leftValue = draft.carbs,
                    rightLabel = "Yağ",
                    rightValue = draft.fat,
                    onLeftChange = { onDraftCarbsChange(draft.draftId, it) },
                    onRightChange = { onDraftFatChange(draft.draftId, it) },
                )
            }
        }
        item {
            RoundedPillButton(
                label = "Besin Ekle",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meal_sheet_add_food_button"),
                containerColor = HealthPrimary.copy(alpha = 0.12f),
                contentColor = HealthPrimary,
                onClick = onAddDraft,
            )
        }
        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("meal_sheet_save_button"),
                enabled = state.canSave,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                ),
                onClick = onSaveMeal,
                contentPadding = PaddingValues(vertical = HealthSpacing.sm),
            ) {
                Text(text = "Öğünü Kaydet")
            }
        }
    }
}

@Composable
private fun ExerciseEditorSheet(
    currentType: ExerciseType?,
    currentDuration: Int,
    currentIntensity: ExerciseIntensity?,
    onSave: (ExerciseType, Int, ExerciseIntensity) -> Unit,
) {
    var selectedType by rememberSaveable { mutableStateOf(currentType ?: ExerciseType.WEIGHTS) }
    var selectedDuration by rememberSaveable { mutableStateOf(currentDuration.takeIf { it > 0 } ?: 45) }
    var useCustomDuration by rememberSaveable { mutableStateOf(currentDuration !in listOf(30, 45, 60) && currentDuration > 0) }
    var customDuration by rememberSaveable { mutableStateOf(if (useCustomDuration) currentDuration.toString() else "") }
    var selectedIntensity by rememberSaveable { mutableStateOf(currentIntensity ?: ExerciseIntensity.MEDIUM) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = "Egzersiz Ekle",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
            items(ExerciseType.entries) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = type.toExerciseIcon(),
                                contentDescription = null,
                            )
                            Text(type.label)
                        }
                    },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            listOf(30, 45, 60).forEach { duration ->
                FilterChip(
                    selected = !useCustomDuration && selectedDuration == duration,
                    onClick = {
                        useCustomDuration = false
                        selectedDuration = duration
                    },
                    label = { Text("$duration dk") },
                )
            }
            FilterChip(
                selected = useCustomDuration,
                onClick = { useCustomDuration = true },
                label = { Text("Özel") },
            )
        }
        if (useCustomDuration) {
            HealthPillTextField(
                value = customDuration,
                onValueChange = { customDuration = it },
                label = "Özel Süre (dk)",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }
        SegmentedControl(
            modifier = Modifier.fillMaxWidth(),
            options = ExerciseIntensity.entries.map { it.label },
            selectedIndex = ExerciseIntensity.entries.indexOf(selectedIntensity),
            onSelectionChange = { index ->
                selectedIntensity = ExerciseIntensity.entries[index]
            },
        )
        RoundedPillButton(
            label = "Kaydet",
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = {
                val duration = if (useCustomDuration) {
                    customDuration.toIntOrDefault(selectedDuration)
                } else {
                    selectedDuration
                }
                onSave(
                    selectedType,
                    duration,
                    selectedIntensity,
                )
            },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}

@Composable
private fun SmokingEditorSheet(
    initialCount: Int,
    onSave: (Int) -> Unit,
) {
    var count by rememberSaveable { mutableStateOf(initialCount.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = "Sigara Girişi",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        HealthPillTextField(
            value = count,
            onValueChange = { count = it },
            label = "Günlük Toplam",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        RoundedPillButton(
            label = "Kaydet",
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = { onSave(count.toIntOrDefault(0)) },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}

private fun ExerciseType?.toExerciseIcon(): ImageVector {
    return when (this) {
        ExerciseType.WEIGHTS -> Icons.Rounded.FitnessCenter
        ExerciseType.RUN -> Icons.Rounded.DirectionsRun
        ExerciseType.WALK -> Icons.Rounded.DirectionsWalk
        ExerciseType.BIKE -> Icons.Rounded.DirectionsBike
        ExerciseType.YOGA -> Icons.Rounded.SelfImprovement
        null -> Icons.Rounded.FitnessCenter
    }
}

@Composable
private fun ExerciseTypeChip(
    type: ExerciseType,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = type.toExerciseIcon(),
                    contentDescription = null,
                )
                Text(type.label)
            }
        }
    )
}

@Composable
private fun MealDraftFields(
    draft: MealDraftFoodState,
    onDraftNameChange: (Long, String) -> Unit,
    onDraftCaloriesChange: (Long, String) -> Unit,
    onDraftProteinChange: (Long, String) -> Unit,
    onDraftCarbsChange: (Long, String) -> Unit,
    onDraftFatChange: (Long, String) -> Unit,
) {
    HealthPillTextField(
        value = draft.name,
        onValueChange = { onDraftNameChange(draft.draftId, it) },
        label = "Öğe Adı",
    )
    NumberFieldRow(
        leftLabel = "Kalori",
        leftValue = draft.calories,
        rightLabel = "Protein",
        rightValue = draft.protein,
        onLeftChange = { onDraftCaloriesChange(draft.draftId, it) },
        onRightChange = { onDraftProteinChange(draft.draftId, it) },
    )
    NumberFieldRow(
        leftLabel = "Karb",
        leftValue = draft.carbs,
        rightLabel = "Yağ",
        rightValue = draft.fat,
        onLeftChange = { onDraftCarbsChange(draft.draftId, it) },
        onRightChange = { onDraftFatChange(draft.draftId, it) },
    )
}

@Composable
private fun MealDraftCard(
    draft: MealDraftFoodState,
    state: MealEditorUiState,
    onRemoveDraft: (Long) -> Unit,
    onDraftNameChange: (Long, String) -> Unit,
    onDraftCaloriesChange: (Long, String) -> Unit,
    onDraftProteinChange: (Long, String) -> Unit,
    onDraftCarbsChange: (Long, String) -> Unit,
    onDraftFatChange: (Long, String) -> Unit,
) {
    HealthCard(modifier = Modifier.testTag("meal_draft_${draft.draftId}")) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Besin",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (state.draftFoods.size > 1) {
                RoundedPillButton(
                    label = "Sil",
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = { onRemoveDraft(draft.draftId) },
                )
            }
        }
        Column(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            MealDraftFields(
                draft = draft,
                onDraftNameChange = onDraftNameChange,
                onDraftCaloriesChange = onDraftCaloriesChange,
                onDraftProteinChange = onDraftProteinChange,
                onDraftCarbsChange = onDraftCarbsChange,
                onDraftFatChange = onDraftFatChange,
            )
        }
    }
}

@Composable
private fun HydrationSheet(
    onSave: (Int) -> Unit,
) {
    var amount by rememberSaveable { mutableStateOf("750") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = "Su Ekle",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        HealthPillTextField(
            value = amount,
            onValueChange = { amount = it },
            label = "Miktar (ml)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        RoundedPillButton(
            label = "Kaydet",
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = { onSave(amount.toIntOrDefault(0)) },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}

@Composable
private fun SleepEditorSheet(
    timeRangeLabel: String,
    onSave: (LocalTime, LocalTime) -> Unit,
) {
    var start by rememberSaveable { mutableStateOf(timeRangeLabel.substringBefore(" - ").ifBlank { "23:30" }) }
    var end by rememberSaveable { mutableStateOf(timeRangeLabel.substringAfter(" - ", "07:00")) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = "Uyku Düzenle",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Saatleri 24 saat formatında HH:mm olarak gir.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NumberFieldRow(
            leftLabel = "Yatış",
            leftValue = start,
            rightLabel = "Kalkış",
            rightValue = end,
            onLeftChange = { start = it },
            onRightChange = { end = it },
            numeric = false,
        )
        RoundedPillButton(
            label = "Kaydet",
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = {
                val startTime = start.toLocalTimeOrNull() ?: LocalTime.of(23, 30)
                val endTime = end.toLocalTimeOrNull() ?: LocalTime.of(7, 0)
                onSave(startTime, endTime)
            },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}

@Composable
private fun WeightEditorSheet(
    initialWeight: Float,
    onSave: (Float) -> Unit,
) {
    var weight by rememberSaveable { mutableStateOf(if (initialWeight > 0f) formatFloat(initialWeight) else "") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = "Kilo Ekle",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        HealthPillTextField(
            value = weight,
            onValueChange = { weight = it },
            label = "Kilo (kg)",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        RoundedPillButton(
            label = "Kaydet",
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = { onSave(weight.toFloatOrDefault(0f)) },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}

@Composable
private fun SupplementDoseSheet(
    items: List<SupplementItemState>,
    onSave: (List<SupplementDoseEntry>) -> Unit,
) {
    val initialDrafts = remember(items) {
        items.map { item ->
            SupplementDoseDraft(
                templateId = item.id,
                name = item.name,
                unitLabel = item.unitLabel,
                amountText = if (item.currentAmount > 0f) formatFloat(item.currentAmount) else "",
            )
        }
    }
    var editableDrafts by remember(items) { mutableStateOf(initialDrafts) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = "Takviye Dozu Ekle",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        editableDrafts.forEachIndexed { index, item ->
            HealthPillTextField(
                value = item.amountText,
                onValueChange = { newValue ->
                    editableDrafts = editableDrafts.toMutableList().also { list ->
                        list[index] = list[index].copy(amountText = newValue)
                    }
                },
                label = item.name,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = {
                    Text(
                        text = item.unitLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
        }
        RoundedPillButton(
            label = "Kaydet",
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = {
                onSave(
                    editableDrafts.mapNotNull { item ->
                        val amount = item.amountText.toFloatOrNull()
                        if (amount == null || amount <= 0f) {
                            null
                        } else {
                            SupplementDoseEntry(
                                templateId = item.templateId,
                                date = LocalDate.now(),
                                amount = amount,
                            )
                        }
                    },
                )
            },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}

@Composable
private fun NumberFieldRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    onLeftChange: (String) -> Unit,
    onRightChange: (String) -> Unit,
    numeric: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        HealthPillTextField(
            modifier = Modifier.weight(1f),
            value = leftValue,
            onValueChange = onLeftChange,
            label = leftLabel,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
            ),
        )
        if (rightLabel.isNotBlank()) {
            HealthPillTextField(
                modifier = Modifier.weight(1f),
                value = rightValue,
                onValueChange = onRightChange,
                label = rightLabel,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (numeric) KeyboardType.Number else KeyboardType.Text,
                ),
            )
        }
    }
}

private fun formatFloat(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private data class SupplementDoseDraft(
    val templateId: Long,
    val name: String,
    val unitLabel: String,
    val amountText: String,
)

private fun String.toIntOrDefault(default: Int): Int = toIntOrNull() ?: default
private fun String.toFloatOrDefault(default: Float): Float = toFloatOrNull() ?: default

private fun String.toLocalTimeOrNull(): LocalTime? {
    return runCatching {
        LocalTime.parse(this, DateTimeFormatter.ofPattern("H:mm"))
    }.getOrNull()
}
