package com.burak.healthapp.feature.onboarding

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalTime
import kotlin.math.roundToInt

fun suggestWaterTargetMl(weightKg: Float?, activityLevel: OnboardingActivityLevel): Int {
    if (weightKg == null || weightKg <= 0) return DefaultHealthGoals.WATER_TARGET_ML
    val base = weightKg * 35f
    val activityBonus = when (activityLevel) {
        OnboardingActivityLevel.LOW -> 0f
        OnboardingActivityLevel.LIGHT -> 150f
        OnboardingActivityLevel.MODERATE -> 250f
        OnboardingActivityLevel.HIGH -> 500f
    }
    val total = base + activityBonus
    return clampValue((total / 250f).roundToInt() * 250, 1500, 4000)
}

fun suggestCaffeineCutoffTime(bedtime: LocalTime, bufferHours: Int = 8): LocalTime = bedtime.minusHours(bufferHours.toLong())

fun suggestCalories(
    age: Int?,
    sex: OnboardingSex,
    heightCm: Float?,
    currentWeightKg: Float?,
    activityLevel: OnboardingActivityLevel,
    mainGoal: OnboardingMainGoal,
): Int {
    if (age == null || heightCm == null || currentWeightKg == null || sex == OnboardingSex.UNSPECIFIED) {
        return DefaultHealthGoals.DAILY_CALORIES
    }

    val bmr = if (sex == OnboardingSex.MALE) {
        10 * currentWeightKg + 6.25f * heightCm - 5 * age + 5
    } else {
        10 * currentWeightKg + 6.25f * heightCm - 5 * age - 161
    }

    val multiplier = when (activityLevel) {
        OnboardingActivityLevel.LOW -> 1.2f
        OnboardingActivityLevel.LIGHT -> 1.375f
        OnboardingActivityLevel.MODERATE -> 1.55f
        OnboardingActivityLevel.HIGH -> 1.725f
    }

    val tdee = bmr * multiplier

    val adjustment = when (mainGoal) {
        OnboardingMainGoal.MAINTAIN -> 0
        OnboardingMainGoal.SLOW_GAIN -> 250
        OnboardingMainGoal.SLOW_LOSS -> -250
    }

    val target = tdee + adjustment
    return clampValue((target / 50f).roundToInt() * 50, 1200, 4500)
}

fun suggestProteinGrams(weightKg: Float?): Int {
    if (weightKg == null || weightKg <= 0) return DefaultHealthGoals.PROTEIN_GRAMS
    return (weightKg * 1.6f / 5f).roundToInt() * 5
}

fun suggestFatGrams(calories: Int): Int = ((calories * 0.25f) / 9f / 5f).roundToInt() * 5

fun suggestCarbGrams(calories: Int, proteinGrams: Int, fatGrams: Int): Int {
    val remainingCalories = calories - (proteinGrams * 4) - (fatGrams * 9)
    val carbs = (remainingCalories / 4f / 5f).roundToInt() * 5
    return carbs.coerceAtLeast(0)
}

private fun clampValue(value: Int, min: Int, max: Int): Int = value.coerceIn(min, max)
