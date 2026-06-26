package com.saglik.domain.onboarding

import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex

data class CompleteOnboardingInput(
    val sex: Sex?,
    val age: Int?,
    val heightCm: Float?,
    val startingWeightKg: Float?,
    val goal: HealthGoal?,
)
