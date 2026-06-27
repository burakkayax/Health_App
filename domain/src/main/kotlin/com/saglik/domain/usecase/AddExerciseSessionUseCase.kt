package com.saglik.domain.usecase

import com.saglik.domain.repository.AddExerciseSessionInput
import com.saglik.domain.repository.ExerciseRepository

class AddExerciseSessionUseCase(
    private val repository: ExerciseRepository,
    private val validateExerciseSessionInputUseCase: ValidateExerciseSessionInputUseCase =
        ValidateExerciseSessionInputUseCase(),
) {
    suspend operator fun invoke(input: AddExerciseSessionInput): Boolean {
        if (!validateExerciseSessionInputUseCase(input).isValid) {
            return false
        }

        repository.addExerciseSession(
            input.copy(
                title = input.title?.trim()?.takeIf { it.isNotBlank() },
                notes = input.notes?.trim()?.takeIf { it.isNotBlank() },
            ),
        )
        return true
    }
}
