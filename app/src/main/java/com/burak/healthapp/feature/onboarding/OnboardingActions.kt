package com.burak.healthapp.feature.onboarding

import com.burak.healthapp.domain.model.DashboardCardType

sealed interface OnboardingAction {
    data object NextClicked : OnboardingAction
    data object BackClicked : OnboardingAction
    data object SkipWithDefaults : OnboardingAction
    data class ToggleTrackingArea(val area: DashboardCardType) : OnboardingAction
    data class UpdateName(val value: String) : OnboardingAction
    data class UpdateAge(val value: String) : OnboardingAction
    data class UpdateSex(val value: OnboardingSex) : OnboardingAction
    data class UpdateHeight(val value: String) : OnboardingAction
    data class UpdateCurrentWeight(val value: String) : OnboardingAction
    data class UpdateTargetWeight(val value: String) : OnboardingAction
    data class UpdateActivityLevel(val value: OnboardingActivityLevel) : OnboardingAction
    data class UpdateMainGoal(val value: OnboardingMainGoal) : OnboardingAction
    data class UpdateWaterTarget(val value: String) : OnboardingAction
    data class UpdateDailySteps(val value: String) : OnboardingAction
    data class UpdateSleepBedtime(val value: String) : OnboardingAction
    data class UpdateSleepWakeTime(val value: String) : OnboardingAction
    data class UpdateCaffeineLimit(val value: String) : OnboardingAction
    data class UpdateCaffeineCutoff(val value: String) : OnboardingAction
    data class UpdateCalories(val value: String) : OnboardingAction
    data class UpdateProtein(val value: String) : OnboardingAction
    data class UpdateCarbs(val value: String) : OnboardingAction
    data class UpdateFat(val value: String) : OnboardingAction
    data class UpdateExerciseDays(val value: String) : OnboardingAction
    data class UpdateExerciseDuration(val value: String) : OnboardingAction
    data class UpdateSmokeLimit(val value: String) : OnboardingAction
    data class UpdateWaterReminderEnabled(val value: Boolean) : OnboardingAction
    data class UpdateWaterReminderStart(val value: String) : OnboardingAction
    data class UpdateWaterReminderEnd(val value: String) : OnboardingAction
    data class UpdateWaterReminderInterval(val value: String) : OnboardingAction
    data class UpdateStepTrackingPreferred(val value: Boolean) : OnboardingAction
}
