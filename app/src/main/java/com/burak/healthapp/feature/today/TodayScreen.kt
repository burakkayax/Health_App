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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.adaptive.AdaptiveDashboardGrid
import com.burak.healthapp.core.ui.adaptive.ConstrainedLargeScreenContainer
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SkeletonCard
import com.burak.healthapp.core.ui.components.SkeletonMetricCard
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.feature.today.components.CaffeineCard
import com.burak.healthapp.feature.today.components.ExerciseCard
import com.burak.healthapp.feature.today.components.HydrationCard
import com.burak.healthapp.feature.today.components.NutritionCard
import com.burak.healthapp.feature.today.components.SleepCard
import com.burak.healthapp.feature.today.components.SmokingCard
import com.burak.healthapp.feature.today.components.StepCard
import com.burak.healthapp.feature.today.components.SupplementsCard
import com.burak.healthapp.feature.today.components.WeightCard
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import com.burak.healthapp.feature.today.sheet.CaffeineSheet
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
    data object Caffeine : TodaySheet
    data object CustomizeDashboard : TodaySheet
}

private sealed interface MealSheetMode {
    data object Editor : MealSheetMode
    data class FoodSearch(val draftId: Long) : MealSheetMode
    data class CustomFoodAdd(val draftId: Long) : MealSheetMode
    data class CustomFoodEdit(val draftId: Long, val foodId: Long) : MealSheetMode
}

internal sealed interface TodayDashboardItem {
    data class Card(val config: DashboardCardConfig) : TodayDashboardItem
    data object Empty : TodayDashboardItem
    data object Customize : TodayDashboardItem
}

internal fun buildDashboardItems(cards: List<DashboardCardConfig>): List<TodayDashboardItem> = buildDashboardItemsFromOrderedCards(cards.sortedBy(DashboardCardConfig::sortOrder))

private fun buildDashboardItemsFromOrderedCards(
    orderedCards: List<DashboardCardConfig>,
): List<TodayDashboardItem> {
    val visibleCards = orderedCards.filter(DashboardCardConfig::isVisible)
    return if (visibleCards.isEmpty()) {
        listOf(TodayDashboardItem.Empty)
    } else {
        visibleCards.map(TodayDashboardItem::Card) + TodayDashboardItem.Customize
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayContent(
    state: TodayUiState,
    actions: TodayActions,
    mealEditorState: MealEditorUiState,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    TodayContent(
        state = state,
        onAddMeal = actions.onAddMeal,
        onAddHydration = actions.onAddHydration,
        onAddCaffeine = actions.onAddCaffeine,
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
        onOpenCaffeineDetail = actions.onOpenCaffeineDetail,
        onOpenSmokingDetail = actions.onOpenSmokingDetail,
        onOpenExerciseDetail = actions.onOpenExerciseDetail,
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
        windowSizeClass = windowSizeClass,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayContent(
    state: TodayUiState,
    onAddMeal: (MealType, String, Int, Int, Int, Int) -> Unit,
    onAddHydration: (Int) -> Unit,
    onAddCaffeine: (
        com.burak.healthapp.domain.model.CaffeineDrinkType,
        com.burak.healthapp.domain.model.CaffeineDrinkSize,
        Int,
        String?,
    ) -> Unit = { _, _, _, _ -> },
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
    onOpenCaffeineDetail: () -> Unit = {},
    onOpenSmokingDetail: () -> Unit = {},
    onOpenExerciseDetail: () -> Unit = {},
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
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    var activeSheet by remember { mutableStateOf<TodaySheet?>(null) }
    var mealSheetMode by remember { mutableStateOf<MealSheetMode>(MealSheetMode.Editor) }
    if (state.isLoading) {
        TodaySkeletonContent(windowSizeClass = windowSizeClass)
        return
    }
    val orderedCards = remember(state.dashboardCards) {
        state.dashboardCards.sortedBy(DashboardCardConfig::sortOrder)
    }
    val dashboardItems = remember(orderedCards) {
        buildDashboardItemsFromOrderedCards(orderedCards)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AdaptiveDashboardGrid(
            items = dashboardItems,
            key = { item ->
                when (item) {
                    is TodayDashboardItem.Card -> item.config.type.name
                    TodayDashboardItem.Empty -> "empty_dashboard"
                    TodayDashboardItem.Customize -> "customize_dashboard"
                }
            },
            windowSizeClass = windowSizeClass,
            modifier = Modifier
                .fillMaxSize()
                .testTag("today_list"),
            contentPadding = PaddingValues(
                start = HealthSpacing.sm,
                end = HealthSpacing.sm,
                top = HealthSpacing.xs,
                bottom = HealthSpacing.md,
            ),
            fullSpan = { item ->
                item !is TodayDashboardItem.Card ||
                    item.config.type == DashboardCardType.NUTRITION
            },
        ) { item ->
            when (item) {
                TodayDashboardItem.Empty -> EmptyDashboardCard(onCustomize = { activeSheet = TodaySheet.CustomizeDashboard })
                TodayDashboardItem.Customize -> DashboardCustomizeButton(
                    onClick = { activeSheet = TodaySheet.CustomizeDashboard },
                )
                is TodayDashboardItem.Card -> when (item.config.type) {
                    DashboardCardType.NUTRITION -> NutritionCard(
                        state = state.nutrition,
                        onAddMeal = { activeSheet = TodaySheet.Meal },
                        onOpenMealHistory = onOpenMealHistory,
                    )
                    DashboardCardType.WEIGHT -> WeightCard(
                        state = state.weight,
                        onAddWeight = { activeSheet = TodaySheet.Weight },
                        onOpenDetails = onOpenWeightDetail,
                    )
                    DashboardCardType.HYDRATION -> HydrationCard(
                        state = state.hydration,
                        onQuickAdd = onAddHydration,
                        onMore = { activeSheet = TodaySheet.Hydration },
                        onOpenDetails = onOpenHydrationDetail,
                    )
                    DashboardCardType.SLEEP -> SleepCard(
                        state = state.sleep,
                        onEdit = { activeSheet = TodaySheet.Sleep },
                        onOpenDetails = onOpenSleepDetail,
                        onDeleteSleep = onDeleteSleep,
                    )
                    DashboardCardType.EXERCISE -> ExerciseCard(
                        state = state.exercise,
                        onAddExercise = { activeSheet = TodaySheet.Exercise },
                        onDeleteExercise = onDeleteExercise,
                        onOpenDetails = onOpenExerciseDetail,
                    )
                    DashboardCardType.CAFFEINE -> CaffeineCard(
                        state = state.caffeine,
                        onAdd = { activeSheet = TodaySheet.Caffeine },
                        onOpenDetails = onOpenCaffeineDetail,
                    )
                    DashboardCardType.SMOKING -> SmokingCard(
                        state = state.smoking,
                        onAddSmoking = { activeSheet = TodaySheet.Smoking },
                        onQuickIncrement = onIncrementSmoking,
                        onDeleteSmoking = onDeleteSmoking,
                        onOpenDetails = onOpenSmokingDetail,
                    )
                    DashboardCardType.SUPPLEMENTS -> SupplementsCard(
                        items = state.supplements.items,
                        onAdd = { activeSheet = TodaySheet.SupplementDose },
                        onDeleteDose = onDeleteSupplementDose,
                    )
                    DashboardCardType.STEPS -> StepCard(
                        state = state.steps,
                        onOpenDetails = onOpenStepDetail,
                    )
                }
            }
        }
    }

    when (activeSheet) {
        TodaySheet.Meal -> ModalBottomSheet(
            onDismissRequest = {
                onResetMealEditor()
                mealSheetMode = MealSheetMode.Editor
                activeSheet = null
            },
        ) {
            when (val mode = mealSheetMode) {
                MealSheetMode.Editor -> {
                    MealEditorSheet(
                        state = mealEditorState,
                        onMealTypeChange = onMealTypeChange,
                        onAddDraft = onAddMealDraft,
                        onRemoveDraft = onRemoveMealDraft,
                        onSearchFood = { draftId ->
                            mealSheetMode = MealSheetMode.FoodSearch(draftId)
                        },
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
                            mealSheetMode = MealSheetMode.Editor
                            activeSheet = null
                        },
                    )
                }

                is MealSheetMode.FoodSearch -> {
                    com.burak.healthapp.feature.today.meal.MealFoodSearchRoute(
                        onBack = { mealSheetMode = MealSheetMode.Editor },
                        onSelectPreset = { preset ->
                            val draft = preset.toMealDraftFoodState(mode.draftId)
                            onMealDraftNameChange(mode.draftId, draft.name)
                            onMealDraftCaloriesChange(mode.draftId, draft.calories)
                            onMealDraftProteinChange(mode.draftId, draft.protein)
                            onMealDraftCarbsChange(mode.draftId, draft.carbs)
                            onMealDraftFatChange(mode.draftId, draft.fat)
                            mealSheetMode = MealSheetMode.Editor
                        },
                        onSelectCustomFood = { food ->
                            onMealDraftNameChange(mode.draftId, food.name)
                            onMealDraftCaloriesChange(mode.draftId, food.calories.toString())
                            onMealDraftProteinChange(mode.draftId, food.proteinGrams.toString())
                            onMealDraftCarbsChange(mode.draftId, food.carbsGrams.toString())
                            onMealDraftFatChange(mode.draftId, food.fatGrams.toString())
                            mealSheetMode = MealSheetMode.Editor
                        },
                        onAddCustomFood = {
                            mealSheetMode = MealSheetMode.CustomFoodAdd(mode.draftId)
                        },
                        onEditCustomFood = { foodId ->
                            mealSheetMode = MealSheetMode.CustomFoodEdit(mode.draftId, foodId)
                        },
                    )
                }

                is MealSheetMode.CustomFoodAdd -> {
                    val vm: com.burak.healthapp.feature.today.meal.CustomFoodEditorViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
                    val editorState by vm.state.collectAsStateWithLifecycle()
                    com.burak.healthapp.feature.today.meal.CustomFoodEditorContent(
                        state = editorState,
                        onNameChange = vm::onNameChange,
                        onBrandChange = vm::onBrandChange,
                        onServingNameChange = vm::onServingNameChange,
                        onServingGramsChange = vm::onServingGramsChange,
                        onCaloriesChange = vm::onCaloriesChange,
                        onProteinChange = vm::onProteinChange,
                        onCarbsChange = vm::onCarbsChange,
                        onFatChange = vm::onFatChange,
                        onSave = { vm.save { mealSheetMode = MealSheetMode.FoodSearch(mode.draftId) } },
                        onDelete = null,
                        onBack = { mealSheetMode = MealSheetMode.FoodSearch(mode.draftId) },
                    )
                }

                is MealSheetMode.CustomFoodEdit -> {
                    val vm: com.burak.healthapp.feature.today.meal.CustomFoodEditorViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel(
                        key = "edit_${mode.foodId}",
                    )
                    androidx.compose.runtime.LaunchedEffect(mode.foodId) {
                        vm.loadFood(mode.foodId)
                    }
                    val editorState by vm.state.collectAsStateWithLifecycle()
                    com.burak.healthapp.feature.today.meal.CustomFoodEditorContent(
                        state = editorState,
                        onNameChange = vm::onNameChange,
                        onBrandChange = vm::onBrandChange,
                        onServingNameChange = vm::onServingNameChange,
                        onServingGramsChange = vm::onServingGramsChange,
                        onCaloriesChange = vm::onCaloriesChange,
                        onProteinChange = vm::onProteinChange,
                        onCarbsChange = vm::onCarbsChange,
                        onFatChange = vm::onFatChange,
                        onSave = { vm.save { mealSheetMode = MealSheetMode.FoodSearch(mode.draftId) } },
                        onDelete = { vm.delete { mealSheetMode = MealSheetMode.FoodSearch(mode.draftId) } },
                        onBack = { mealSheetMode = MealSheetMode.FoodSearch(mode.draftId) },
                    )
                }
            }
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

        TodaySheet.Caffeine -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            CaffeineSheet(
                onSave = { type, size, estimatedMg, customName ->
                    onAddCaffeine(type, size, estimatedMg, customName)
                    activeSheet = null
                },
            )
        }

        TodaySheet.CustomizeDashboard -> ModalBottomSheet(onDismissRequest = { activeSheet = null }) {
            ConstrainedLargeScreenContainer(
                windowSizeClass = windowSizeClass,
                maxWidth = 640.dp,
            ) {
                DashboardCustomizationSheet(
                    cards = orderedCards,
                    onVisibilityChange = onDashboardCardVisibilityChange,
                    onMove = onMoveDashboardCard,
                    onReset = onResetDashboardCards,
                )
            }
        }

        null -> Unit
    }
}

@Composable
private fun TodaySkeletonContent(
    windowSizeClass: HealthWindowSizeClass,
) {
    AdaptiveDashboardGrid(
        items = listOf("header", "nutrition", "hydration", "sleep", "steps", "caffeine"),
        key = { item -> "today_skeleton_$item" },
        windowSizeClass = windowSizeClass,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("today_skeleton"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        fullSpan = { item -> item == "header" || item == "nutrition" },
    ) { item ->
        if (item == "header" || item == "nutrition") {
            SkeletonCard(lines = if (item == "header") 2 else 4)
        } else {
            SkeletonMetricCard()
        }
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
private fun DashboardCustomizeButton(onClick: () -> Unit) {
    RoundedPillButton(
        label = stringResource(R.string.today_customize_dashboard),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_customize_dashboard_button"),
        containerColor = HealthPrimary.copy(alpha = 0.12f),
        contentColor = HealthPrimary,
        onClick = onClick,
    )
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
    DashboardCardType.CAFFEINE -> stringResource(R.string.dashboard_card_caffeine)
    DashboardCardType.SMOKING -> stringResource(R.string.dashboard_card_smoking)
    DashboardCardType.SUPPLEMENTS -> stringResource(R.string.dashboard_card_supplements)
    DashboardCardType.STEPS -> stringResource(R.string.dashboard_card_steps)
}
