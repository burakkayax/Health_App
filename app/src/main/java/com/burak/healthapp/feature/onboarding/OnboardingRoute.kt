package com.burak.healthapp.feature.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingRoute() {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                is OnboardingAction.NextClicked -> viewModel.goToNextStep()
                is OnboardingAction.BackClicked -> viewModel.goToPreviousStep()
                is OnboardingAction.SkipWithDefaults -> viewModel.skipWithDefaults()
                is OnboardingAction.ToggleTrackingArea -> viewModel.onTrackingAreaToggled(action.area)
                is OnboardingAction.UpdateName -> viewModel.updateName(action.value)
                is OnboardingAction.UpdateAge -> viewModel.updateAge(action.value)
                is OnboardingAction.UpdateSex -> viewModel.updateSex(action.value)
                is OnboardingAction.UpdateHeight -> viewModel.updateHeightCm(action.value)
                is OnboardingAction.UpdateCurrentWeight -> viewModel.updateCurrentWeightKg(action.value)
                is OnboardingAction.UpdateTargetWeight -> viewModel.updateTargetWeightKg(action.value)
                is OnboardingAction.UpdateActivityLevel -> viewModel.updateActivityLevel(action.value)
                is OnboardingAction.UpdateMainGoal -> viewModel.updateMainGoal(action.value)
                is OnboardingAction.UpdateWaterTarget -> viewModel.updateWaterTargetMl(action.value)
                is OnboardingAction.UpdateSleepBedtime -> viewModel.updateSleepBedtime(action.value)
                is OnboardingAction.UpdateSleepWakeTime -> viewModel.updateSleepWakeTime(action.value)
                is OnboardingAction.UpdateCaffeineLimit -> viewModel.updateDailyCaffeineLimitMg(action.value)
                is OnboardingAction.UpdateCaffeineCutoff -> viewModel.updateCaffeineCutoffTime(action.value)
                is OnboardingAction.UpdateCalories -> viewModel.updateDailyCaloriesTarget(action.value)
                is OnboardingAction.UpdateProtein -> viewModel.updateProteinTargetGrams(action.value)
                is OnboardingAction.UpdateCarbs -> viewModel.updateCarbTargetGrams(action.value)
                is OnboardingAction.UpdateFat -> viewModel.updateFatTargetGrams(action.value)
                is OnboardingAction.UpdateExerciseDays -> viewModel.updateExerciseDaysPerWeek(action.value)
                is OnboardingAction.UpdateExerciseDuration -> viewModel.updateExerciseDurationMinutes(action.value)
                is OnboardingAction.UpdateSmokeLimit -> viewModel.updateSmokeDailyLimit(action.value)
                is OnboardingAction.UpdateDailySteps -> viewModel.updateDailyStepTarget(action.value)
                is OnboardingAction.UpdateWaterReminderEnabled -> viewModel.updateWaterReminderEnabled(action.value)
                is OnboardingAction.UpdateWaterReminderStart -> viewModel.updateWaterReminderStartTime(action.value)
                is OnboardingAction.UpdateWaterReminderEnd -> viewModel.updateWaterReminderEndTime(action.value)
                is OnboardingAction.UpdateWaterReminderInterval -> viewModel.updateWaterReminderIntervalMinutes(action.value)
                is OnboardingAction.UpdateStepTrackingPreferred -> viewModel.updateStepTrackingPreferred(action.value)
            }
        },
    )
}
