package com.saglik.feature.onboarding

import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex

sealed interface OnboardingEvent {
    data object NextClicked : OnboardingEvent
    data object BackClicked : OnboardingEvent
    data class SexSelected(val sex: Sex) : OnboardingEvent
    data class AgeChanged(val value: String) : OnboardingEvent
    data class HeightChanged(val value: String) : OnboardingEvent
    data class StartingWeightChanged(val value: String) : OnboardingEvent
    data class GoalSelected(val goal: HealthGoal) : OnboardingEvent
    data object CompleteClicked : OnboardingEvent
}
