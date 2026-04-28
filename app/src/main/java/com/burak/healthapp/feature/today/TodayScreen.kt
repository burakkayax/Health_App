package com.burak.healthapp.feature.today

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    var localCards by remember(cards) { mutableStateOf(cards) }
    val listState = rememberLazyListState()
    var draggingType by remember { mutableStateOf<DashboardCardType?>(null) }
    var draggingStartIndex by remember { mutableIntStateOf(-1) }
    var draggingIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.sm)
            .testTag("dashboard_customization_sheet"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.dashboard_customize_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f, fill = false)
                .testTag("dashboard_customization_sheet_list"),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            itemsIndexed(localCards, key = { _, config -> config.type.name }) { index, config ->
                val isDragging = draggingIndex == index
                val scale by animateFloatAsState(
                    targetValue = if (isDragging) 1.04f else 1f,
                    label = "drag_scale",
                )
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 6.dp else 0.dp,
                    label = "drag_elevation",
                )

                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            shadowElevation = elevation.toPx()
                            translationY = if (isDragging) dragOffset else 0f
                        },
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DragHandle(
                            type = config.type,
                            index = index,
                            onDragStarted = { type ->
                                val startIndex = localCards.indexOfFirst { it.type == type }
                                draggingType = type
                                draggingStartIndex = startIndex
                                draggingIndex = startIndex
                                dragOffset = 0f
                            },
                            onDrag = { type, dragAmount ->
                                if (draggingType != type) return@DragHandle
                                dragOffset += dragAmount
                                val currentIndex = localCards.indexOfFirst { it.type == type }
                                if (currentIndex == -1) return@DragHandle

                                val itemInfo = listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.key == type.name }
                                    ?: return@DragHandle
                                val targetIndex = findLiveDashboardReorderTargetIndex(
                                    visibleItems = listState.layoutInfo.visibleItemsInfo,
                                    draggingKey = type.name,
                                    currentIndex = currentIndex,
                                    dragOffset = dragOffset,
                                )
                                if (targetIndex != currentIndex) {
                                    val targetInfo = listState.layoutInfo.visibleItemsInfo
                                        .firstOrNull { it.index == targetIndex }
                                    localCards = reorderDashboardCards(localCards, currentIndex, targetIndex)
                                    draggingIndex = targetIndex
                                    if (targetInfo != null) {
                                        dragOffset += itemInfo.offset - targetInfo.offset
                                    }
                                }
                            },
                            onDragEnded = { type ->
                                val finalIndex = localCards.indexOfFirst { it.type == type }
                                val startIndex = draggingStartIndex
                                draggingType = null
                                draggingStartIndex = -1
                                draggingIndex = -1
                                dragOffset = 0f
                                if (startIndex >= 0 && finalIndex >= 0 && startIndex != finalIndex) {
                                    onMove(type, finalIndex)
                                }
                            },
                            onDragCancelled = {
                                localCards = cards
                                draggingType = null
                                draggingStartIndex = -1
                                draggingIndex = -1
                                dragOffset = 0f
                            },
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = config.type.label(),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        Switch(
                            modifier = Modifier.testTag("dashboard_card_switch_${config.type.name}"),
                            checked = config.isVisible,
                            onCheckedChange = { checked -> onVisibilityChange(config.type, checked) },
                        )
                    }
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
private fun DragHandle(
    type: DashboardCardType,
    index: Int,
    onDragStarted: (DashboardCardType) -> Unit,
    onDrag: (DashboardCardType, Float) -> Unit,
    onDragEnded: (DashboardCardType) -> Unit,
    onDragCancelled: (DashboardCardType) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val handleDescription = stringResource(R.string.dashboard_drag_handle)

    Column(
        modifier = Modifier
            .size(width = 24.dp, height = 24.dp)
            .testTag("dashboard_drag_handle_$index")
            .semantics {
                contentDescription = handleDescription
            }
            .pointerInput(type) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        onDragStarted(type)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragEnd = {
                        onDragEnded(type)
                    },
                    onDragCancel = {
                        onDragCancelled(type)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(type, dragAmount.y)
                    },
                )
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .width(20.dp)
                .height(2.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(1.dp),
                ),
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(20.dp)
                .height(2.dp)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(1.dp),
                ),
        )
    }
}

private fun findLiveDashboardReorderTargetIndex(
    visibleItems: List<LazyListItemInfo>,
    draggingKey: String,
    currentIndex: Int,
    dragOffset: Float,
): Int {
    val draggingInfo = visibleItems.firstOrNull { it.key == draggingKey } ?: return currentIndex
    val draggingCenter = draggingInfo.offset + draggingInfo.size / 2f + dragOffset
    val crossedItem = if (dragOffset > 0f) {
        visibleItems
            .asSequence()
            .filter { it.key != draggingKey && it.index > currentIndex }
            .filter { draggingCenter > it.offset + it.size / 2f }
            .maxByOrNull(LazyListItemInfo::index)
    } else {
        visibleItems
            .asSequence()
            .filter { it.key != draggingKey && it.index < currentIndex }
            .filter { draggingCenter < it.offset + it.size / 2f }
            .minByOrNull(LazyListItemInfo::index)
    }
    return crossedItem?.index ?: currentIndex
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
