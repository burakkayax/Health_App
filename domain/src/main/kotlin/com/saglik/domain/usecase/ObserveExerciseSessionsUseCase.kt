package com.saglik.domain.usecase

import com.saglik.core.model.ExerciseSession
import com.saglik.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

class ObserveExerciseSessionsUseCase(
    private val repository: ExerciseRepository,
) {
    operator fun invoke(): Flow<List<ExerciseSession>> =
        repository.observeExerciseSessions()

    operator fun invoke(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<ExerciseSession>> =
        repository.observeExerciseSessionsBetween(
            startInclusive = startInclusive,
            endExclusive = endExclusive,
        )
}
