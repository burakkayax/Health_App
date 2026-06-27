package com.saglik.domain.usecase

import com.saglik.core.model.ExerciseSession
import com.saglik.core.model.ExerciseSummary
import com.saglik.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveExerciseSummaryUseCase(
    private val repository: ExerciseRepository,
) {
    operator fun invoke(): Flow<ExerciseSummary> =
        repository.observeExerciseSessions().map { sessions ->
            buildSummary(sessions)
        }

    companion object {
        fun buildSummary(sessions: List<ExerciseSession>): ExerciseSummary =
            ExerciseSummary(
                sessionCount = sessions.size,
                totalDurationMinutes = sessions.sumOf { it.durationMinutes },
                mostRecentSession = sessions.maxByOrNull { it.endTimeMillis },
            )
    }
}
