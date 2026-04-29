package com.burak.healthapp.domain.validation

import com.burak.healthapp.domain.model.GoalSettings
import java.time.LocalTime

sealed interface ValidationResult<out T> {
    data class Valid<T>(val value: T) : ValidationResult<T>
    data class Invalid(val errors: List<HealthInputError>) : ValidationResult<Nothing>
}

enum class HealthInputError {
    REQUIRED,
    MUST_BE_NUMBER,
    MUST_BE_INTEGER,
    MUST_BE_POSITIVE,
    MUST_NOT_BE_NEGATIVE,
    TOO_HIGH,
    TOO_LOW,
    INVALID_TIME,
    DURATION_TOO_LONG,
}

data class ValidatedMealInput(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
)

object MealInputValidator {
    private const val MAX_CALORIES = 5_000
    private const val MAX_MACRO_GRAMS = 1_000

    fun validate(
        name: String,
        calories: String,
        protein: String,
        carbs: String,
        fat: String,
    ): ValidationResult<ValidatedMealInput> {
        val errors = mutableListOf<HealthInputError>()
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) errors += HealthInputError.REQUIRED
        val caloriesValue = parseNonNegativeInt(calories, errors, MAX_CALORIES)
        val proteinValue = parseNonNegativeInt(protein, errors, MAX_MACRO_GRAMS)
        val carbsValue = parseNonNegativeInt(carbs, errors, MAX_MACRO_GRAMS)
        val fatValue = parseNonNegativeInt(fat, errors, MAX_MACRO_GRAMS)

        return if (errors.isEmpty()) {
            ValidationResult.Valid(
                ValidatedMealInput(
                    name = trimmedName,
                    calories = caloriesValue ?: 0,
                    protein = proteinValue ?: 0,
                    carbs = carbsValue ?: 0,
                    fat = fatValue ?: 0,
                ),
            )
        } else {
            ValidationResult.Invalid(errors.distinct())
        }
    }
}

object HydrationInputValidator {
    private const val MAX_SINGLE_AMOUNT_ML = 5_000

    fun validate(amount: String): ValidationResult<Int> {
        val trimmed = amount.trim()
        if (trimmed.isBlank()) return invalid(HealthInputError.REQUIRED)
        val value = trimmed.toIntOrNull() ?: return invalid(HealthInputError.MUST_BE_INTEGER)
        return when {
            value <= 0 -> invalid(HealthInputError.MUST_BE_POSITIVE)
            value > MAX_SINGLE_AMOUNT_ML -> invalid(HealthInputError.TOO_HIGH)
            else -> ValidationResult.Valid(value)
        }
    }
}

object SleepInputValidator {
    private const val MAX_SLEEP_MINUTES = 16 * 60

    fun validate(start: String, end: String): ValidationResult<Pair<LocalTime, LocalTime>> {
        val startTime = start.trim().takeIf(String::isNotBlank)?.parseOrNull()
            ?: return invalid(HealthInputError.INVALID_TIME)
        val endTime = end.trim().takeIf(String::isNotBlank)?.parseOrNull()
            ?: return invalid(HealthInputError.INVALID_TIME)
        val duration = sleepDurationMinutes(startTime, endTime)
        return when {
            duration <= 0 -> invalid(HealthInputError.MUST_BE_POSITIVE)
            duration > MAX_SLEEP_MINUTES -> invalid(HealthInputError.DURATION_TOO_LONG)
            else -> ValidationResult.Valid(startTime to endTime)
        }
    }
}

object WeightInputValidator {
    fun validateWeight(weight: String): ValidationResult<Float> {
        val trimmed = weight.trim()
        if (trimmed.isBlank()) return invalid(HealthInputError.REQUIRED)
        val value = trimmed.toFloatOrNull() ?: return invalid(HealthInputError.MUST_BE_NUMBER)
        return when {
            value <= 0f -> invalid(HealthInputError.MUST_BE_POSITIVE)
            value !in 20f..400f -> invalid(if (value < 20f) HealthInputError.TOO_LOW else HealthInputError.TOO_HIGH)
            else -> ValidationResult.Valid(value)
        }
    }
}

object SmokingInputValidator {
    fun validateCount(count: String): ValidationResult<Int> {
        val trimmed = count.trim()
        if (trimmed.isBlank()) return invalid(HealthInputError.REQUIRED)
        val value = trimmed.toIntOrNull() ?: return invalid(HealthInputError.MUST_BE_INTEGER)
        return if (value < 0) invalid(HealthInputError.MUST_NOT_BE_NEGATIVE) else ValidationResult.Valid(value)
    }
}

object ExerciseInputValidator {
    private const val MAX_DURATION_MINUTES = 300

    fun validateDuration(duration: String): ValidationResult<Int> {
        val trimmed = duration.trim()
        if (trimmed.isBlank()) return invalid(HealthInputError.REQUIRED)
        val value = trimmed.toIntOrNull() ?: return invalid(HealthInputError.MUST_BE_INTEGER)
        return when {
            value <= 0 -> invalid(HealthInputError.MUST_BE_POSITIVE)
            value > MAX_DURATION_MINUTES -> invalid(HealthInputError.TOO_HIGH)
            else -> ValidationResult.Valid(value)
        }
    }
}

object SupplementDoseValidator {
    fun validateAmount(amount: String): ValidationResult<Float> {
        val trimmed = amount.trim()
        if (trimmed.isBlank()) return invalid(HealthInputError.REQUIRED)
        val value = trimmed.toFloatOrNull() ?: return invalid(HealthInputError.MUST_BE_NUMBER)
        return if (value <= 0f) invalid(HealthInputError.MUST_BE_POSITIVE) else ValidationResult.Valid(value)
    }
}

object GoalSettingsValidator {
    private const val MAX_STEP_TARGET = 100_000
    private const val MAX_CAFFEINE_LIMIT_MG = 1_000
    private const val MAX_CAFFEINE_SLEEP_BUFFER_HOURS = 24

    fun validate(goals: GoalSettings): ValidationResult<GoalSettings> {
        val errors = mutableListOf<HealthInputError>()
        if (goals.dailyCaloriesTarget <= 0) errors += HealthInputError.MUST_BE_POSITIVE
        if (goals.proteinTargetGrams < 0 || goals.carbTargetGrams < 0 || goals.fatTargetGrams < 0) {
            errors += HealthInputError.MUST_NOT_BE_NEGATIVE
        }
        if (goals.waterTargetMl <= 0) errors += HealthInputError.MUST_BE_POSITIVE
        if (goals.dailyStepTarget <= 0) errors += HealthInputError.MUST_BE_POSITIVE
        if (goals.dailyStepTarget > MAX_STEP_TARGET) errors += HealthInputError.TOO_HIGH
        if (goals.dailyCaffeineLimitMg <= 0) errors += HealthInputError.MUST_BE_POSITIVE
        if (goals.dailyCaffeineLimitMg > MAX_CAFFEINE_LIMIT_MG) errors += HealthInputError.TOO_HIGH
        if (goals.caffeineSleepBufferHours <= 0) errors += HealthInputError.MUST_BE_POSITIVE
        if (goals.caffeineSleepBufferHours > MAX_CAFFEINE_SLEEP_BUFFER_HOURS) errors += HealthInputError.TOO_HIGH
        if (goals.exerciseTargetDaysPerWeek !in 0..7) errors += HealthInputError.TOO_HIGH
        if (goals.exerciseTargetDurationMinutes <= 0) errors += HealthInputError.MUST_BE_POSITIVE
        if (goals.smokeDailyLimit < 0) errors += HealthInputError.MUST_NOT_BE_NEGATIVE
        if (goals.baselineWeightKg <= 0f || goals.targetWeightKg <= 0f) errors += HealthInputError.MUST_BE_POSITIVE

        return if (errors.isEmpty()) ValidationResult.Valid(goals) else ValidationResult.Invalid(errors.distinct())
    }
}

private fun parseNonNegativeInt(
    input: String,
    errors: MutableList<HealthInputError>,
    max: Int,
): Int? {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return 0
    val value = trimmed.toIntOrNull()
    if (value == null) {
        errors += HealthInputError.MUST_BE_INTEGER
        return null
    }
    if (value < 0) {
        errors += HealthInputError.MUST_NOT_BE_NEGATIVE
        return null
    }
    if (value > max) {
        errors += HealthInputError.TOO_HIGH
        return null
    }
    return value
}

private fun sleepDurationMinutes(start: LocalTime, end: LocalTime): Int {
    val startMinutes = start.toSecondOfDay() / 60
    var endMinutes = end.toSecondOfDay() / 60
    if (endMinutes <= startMinutes) endMinutes += 24 * 60
    return endMinutes - startMinutes
}

private fun String.parseOrNull(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()

private fun <T> invalid(error: HealthInputError): ValidationResult<T> = ValidationResult.Invalid(listOf(error))
