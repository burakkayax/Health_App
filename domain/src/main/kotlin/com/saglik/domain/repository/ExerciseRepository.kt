package com.saglik.domain.repository

import com.saglik.core.model.ExerciseSession
import com.saglik.core.model.ExerciseType
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeExerciseSessions(): Flow<List<ExerciseSession>>

    fun observeExerciseSessionsBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<ExerciseSession>>

    suspend fun addExerciseSession(input: AddExerciseSessionInput)
}

data class AddExerciseSessionInput(
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val exerciseType: ExerciseType,
    val title: String?,
    val notes: String?,
)
