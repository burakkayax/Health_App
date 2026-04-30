package com.burak.healthapp.feature.today

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.TodayUiState
import com.burak.healthapp.feature.today.meal.MealEditorViewModel
import java.time.LocalDate
@Composable
fun TodayRoute(
    selectedDate: LocalDate,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
    onOpenMealHistory: () -> Unit,
    onOpenWeightDetail: () -> Unit,
    onOpenSleepDetail: () -> Unit,
    onOpenStepDetail: () -> Unit,
    onOpenHydrationDetail: () -> Unit,
    onOpenCaffeineDetail: () -> Unit,
    onOpenSmokingDetail: () -> Unit,
    onOpenExerciseDetail: () -> Unit,
) {
    val viewModel: TodayViewModel = hiltViewModel()
    val mealEditorViewModel: MealEditorViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mealEditorState by mealEditorViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    TodayContent(
        state = uiState,
        mealEditorState = mealEditorState,
        windowSizeClass = windowSizeClass,
        actions = TodayActions(
            onAddMeal = viewModel::addMeal,
            onAddHydration = viewModel::addHydration,
            onAddCaffeine = viewModel::addCaffeine,
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
            onOpenHydrationDetail = onOpenHydrationDetail,
            onOpenCaffeineDetail = onOpenCaffeineDetail,
            onOpenSmokingDetail = onOpenSmokingDetail,
            onOpenExerciseDetail = onOpenExerciseDetail,
            onMealTypeChange = mealEditorViewModel::setMealType,
            onAddMealDraft = mealEditorViewModel::addDraftFood,
            onRemoveMealDraft = mealEditorViewModel::removeDraftFood,
            onMealDraftNameChange = mealEditorViewModel::updateDraftName,
            onMealDraftCaloriesChange = mealEditorViewModel::updateDraftCalories,
            onMealDraftProteinChange = mealEditorViewModel::updateDraftProtein,
            onMealDraftCarbsChange = mealEditorViewModel::updateDraftCarbs,
            onMealDraftFatChange = mealEditorViewModel::updateDraftFat,
            onResetMealEditor = mealEditorViewModel::reset,
            onDashboardCardVisibilityChange = viewModel::updateDashboardCardVisibility,
            onMoveDashboardCard = viewModel::moveDashboardCard,
            onResetDashboardCards = viewModel::resetDashboardCards,
        ),
    )
}
