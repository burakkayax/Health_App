package com.saglik.domain.onboarding

data class OnboardingValidationErrors(
    val sex: String? = null,
    val age: String? = null,
    val heightCm: String? = null,
    val startingWeightKg: String? = null,
    val goal: String? = null,
) {
    val isValid: Boolean
        get() = sex == null &&
            age == null &&
            heightCm == null &&
            startingWeightKg == null &&
            goal == null

    val firstMessage: String?
        get() = listOf(sex, age, heightCm, startingWeightKg, goal).firstOrNull { it != null }
}
