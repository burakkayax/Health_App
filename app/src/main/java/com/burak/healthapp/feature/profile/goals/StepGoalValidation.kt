package com.burak.healthapp.feature.profile.goals

internal const val MAX_STEP_TARGET = 100_000

enum class StepGoalInputError {
    REQUIRED,
    MUST_BE_NUMBER,
    POSITIVE,
    TOO_HIGH,
}

sealed interface StepGoalValidationResult {
    data class Valid(val value: Int) : StepGoalValidationResult
    data class Invalid(val error: StepGoalInputError) : StepGoalValidationResult
}

fun validateStepTargetInput(input: String): StepGoalValidationResult {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) {
        return StepGoalValidationResult.Invalid(StepGoalInputError.REQUIRED)
    }

    val value = trimmed.toLongOrNull()
        ?: return StepGoalValidationResult.Invalid(StepGoalInputError.MUST_BE_NUMBER)

    if (value <= 0L) {
        return StepGoalValidationResult.Invalid(StepGoalInputError.POSITIVE)
    }
    if (value > MAX_STEP_TARGET) {
        return StepGoalValidationResult.Invalid(StepGoalInputError.TOO_HIGH)
    }

    return StepGoalValidationResult.Valid(value.toInt())
}
