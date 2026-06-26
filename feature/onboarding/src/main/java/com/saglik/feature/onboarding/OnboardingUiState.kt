package com.saglik.feature.onboarding

import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex

data class OnboardingUiState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val sex: Sex? = null,
    val age: String = "",
    val heightCm: String = "",
    val startingWeightKg: String = "",
    val goal: HealthGoal? = null,
    val isNextEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)
