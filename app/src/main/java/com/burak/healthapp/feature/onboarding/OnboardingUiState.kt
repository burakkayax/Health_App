package com.burak.healthapp.feature.onboarding

import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.DashboardCardType

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val selectedTrackingAreas: Set<DashboardCardType> = defaultOnboardingTrackingAreas(),
    val name: String = "",
    val age: String = "",
    val sex: OnboardingSex = OnboardingSex.UNSPECIFIED,
    val heightCm: String = "",
    val currentWeightKg: String = "",
    val targetWeightKg: String = "",
    val activityLevel: OnboardingActivityLevel = OnboardingActivityLevel.LIGHT,
    val mainGoal: OnboardingMainGoal = OnboardingMainGoal.MAINTAIN,
    val waterTargetMl: String = "",
    val dailyStepTarget: String = "",
    val sleepBedtime: String = "",
    val sleepWakeTime: String = "",
    val dailyCaffeineLimitMg: String = "",
    val caffeineCutoffTime: String = "",
    val dailyCaloriesTarget: String = "",
    val proteinTargetGrams: String = "",
    val carbTargetGrams: String = "",
    val fatTargetGrams: String = "",
    val exerciseDaysPerWeek: String = "",
    val exerciseDurationMinutes: String = "",
    val smokeDailyLimit: String = "",
    val waterReminderEnabled: Boolean = false,
    val waterReminderStartTime: String = "",
    val waterReminderEndTime: String = "",
    val waterReminderIntervalMinutes: String = "",
    val stepTrackingPreferred: Boolean = false,
    val validationErrors: Map<String, UiText> = emptyMap(),
    val isSaving: Boolean = false,
    val saveError: UiText? = null,
)

fun defaultOnboardingTrackingAreas(): Set<DashboardCardType> = setOf(
    DashboardCardType.HYDRATION,
    DashboardCardType.SLEEP,
    DashboardCardType.NUTRITION,
    DashboardCardType.STEPS,
    DashboardCardType.WEIGHT,
)
