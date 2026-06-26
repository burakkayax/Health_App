package com.saglik.domain.onboarding

import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex

object OnboardingValidator {
    fun validateSex(sex: Sex?): String? =
        if (sex == null) "Choose an option to continue." else null

    fun validateAge(age: Int?): String? = when {
        age == null -> "Enter your age to continue."
        age !in MIN_AGE..MAX_AGE -> "Age must be between $MIN_AGE and $MAX_AGE."
        else -> null
    }

    fun validateHeightCm(heightCm: Float?): String? = when {
        heightCm == null -> "Enter your height to continue."
        heightCm < MIN_HEIGHT_CM || heightCm > MAX_HEIGHT_CM -> {
            "Height must be between ${MIN_HEIGHT_CM.toInt()} and ${MAX_HEIGHT_CM.toInt()} cm."
        }
        else -> null
    }

    fun validateStartingWeightKg(weightKg: Float?): String? = when {
        weightKg == null -> "Enter your starting weight to continue."
        weightKg < MIN_WEIGHT_KG || weightKg > MAX_WEIGHT_KG -> {
            "Weight must be between ${MIN_WEIGHT_KG.toInt()} and ${MAX_WEIGHT_KG.toInt()} kg."
        }
        else -> null
    }

    fun validateGoal(goal: HealthGoal?): String? =
        if (goal == null) "Choose a health goal to continue." else null

    fun validateForCompletion(input: CompleteOnboardingInput): OnboardingValidationErrors =
        OnboardingValidationErrors(
            sex = validateSex(input.sex),
            age = validateAge(input.age),
            heightCm = validateHeightCm(input.heightCm),
            startingWeightKg = validateStartingWeightKg(input.startingWeightKg),
            goal = validateGoal(input.goal),
        )

    private const val MIN_AGE = 13
    private const val MAX_AGE = 100
    private const val MIN_HEIGHT_CM = 100f
    private const val MAX_HEIGHT_CM = 250f
    private const val MIN_WEIGHT_KG = 30f
    private const val MAX_WEIGHT_KG = 300f
}
