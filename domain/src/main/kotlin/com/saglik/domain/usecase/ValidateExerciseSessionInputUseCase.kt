package com.saglik.domain.usecase

import com.saglik.domain.repository.AddExerciseSessionInput

data class ExerciseSessionValidationResult(
    val isValid: Boolean,
    val durationMinutes: Int?,
    val errorMessage: String?,
)

class ValidateExerciseSessionInputUseCase {
    operator fun invoke(input: AddExerciseSessionInput): ExerciseSessionValidationResult {
        if (input.startTimeMillis <= 0L) {
            return invalid("Add an exercise start time.")
        }

        if (input.endTimeMillis <= input.startTimeMillis) {
            return invalid("Exercise end time must be after the start time.")
        }

        val durationMinutes = ((input.endTimeMillis - input.startTimeMillis) / MillisPerMinute).toInt()
        if (durationMinutes < MinimumDurationMinutes) {
            return invalid("Exercise duration must be at least 1 minute.")
        }

        return ExerciseSessionValidationResult(
            isValid = true,
            durationMinutes = durationMinutes,
            errorMessage = null,
        )
    }

    private fun invalid(message: String): ExerciseSessionValidationResult =
        ExerciseSessionValidationResult(
            isValid = false,
            durationMinutes = null,
            errorMessage = message,
        )

    private companion object {
        private const val MillisPerMinute = 60_000L
        private const val MinimumDurationMinutes = 1
    }
}
