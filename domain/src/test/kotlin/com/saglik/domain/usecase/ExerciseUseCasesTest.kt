package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.ExerciseSession
import com.saglik.core.model.ExerciseType
import com.saglik.domain.repository.AddExerciseSessionInput
import com.saglik.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseUseCasesTest {
    @Test
    fun validateExerciseSessionInputAcceptsValidInput() {
        val result = ValidateExerciseSessionInputUseCase()(
            AddExerciseSessionInput(
                startTimeMillis = 1_000L,
                endTimeMillis = 61_000L,
                exerciseType = ExerciseType.WALKING,
                title = null,
                notes = null,
            ),
        )

        assertTrue(result.isValid)
        assertEquals(1, result.durationMinutes)
        assertNull(result.errorMessage)
    }

    @Test
    fun validateExerciseSessionInputRejectsMissingStartTime() {
        val result = ValidateExerciseSessionInputUseCase()(
            AddExerciseSessionInput(
                startTimeMillis = 0L,
                endTimeMillis = 61_000L,
                exerciseType = ExerciseType.WALKING,
                title = null,
                notes = null,
            ),
        )

        assertFalse(result.isValid)
        assertNull(result.durationMinutes)
    }

    @Test
    fun validateExerciseSessionInputRejectsEndBeforeOrEqualStart() {
        val result = ValidateExerciseSessionInputUseCase()(
            AddExerciseSessionInput(
                startTimeMillis = 61_000L,
                endTimeMillis = 61_000L,
                exerciseType = ExerciseType.WALKING,
                title = null,
                notes = null,
            ),
        )

        assertFalse(result.isValid)
        assertNull(result.durationMinutes)
    }

    @Test
    fun validateExerciseSessionInputRejectsDurationUnderOneMinute() {
        val result = ValidateExerciseSessionInputUseCase()(
            AddExerciseSessionInput(
                startTimeMillis = 1_000L,
                endTimeMillis = 60_999L,
                exerciseType = ExerciseType.WALKING,
                title = null,
                notes = null,
            ),
        )

        assertFalse(result.isValid)
        assertNull(result.durationMinutes)
    }

    @Test
    fun observeExerciseSummaryComputesCountAndTotalDuration() = runBlocking {
        val useCase = ObserveExerciseSummaryUseCase(
            FakeExerciseRepository(
                sessions = listOf(
                    exerciseSession(id = "exercise-1", endTimeMillis = 10_000L, durationMinutes = 30),
                    exerciseSession(id = "exercise-2", endTimeMillis = 20_000L, durationMinutes = 45),
                ),
            ),
        )

        val summary = useCase().first()

        assertEquals(2, summary.sessionCount)
        assertEquals(75, summary.totalDurationMinutes)
        assertEquals("exercise-2", summary.mostRecentSession?.id)
    }

    private class FakeExerciseRepository(
        private val sessions: List<ExerciseSession> = emptyList(),
    ) : ExerciseRepository {
        val addedInputs = mutableListOf<AddExerciseSessionInput>()

        override fun observeExerciseSessions(): Flow<List<ExerciseSession>> =
            flowOf(sessions)

        override fun observeExerciseSessionsBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Flow<List<ExerciseSession>> =
            flowOf(
                sessions.filter {
                    it.endTimeMillis >= startInclusive && it.endTimeMillis < endExclusive
                },
            )

        override suspend fun addExerciseSession(input: AddExerciseSessionInput) {
            addedInputs += input
        }
    }

    private fun exerciseSession(
        id: String,
        endTimeMillis: Long,
        durationMinutes: Int,
    ): ExerciseSession =
        ExerciseSession(
            id = id,
            startTimeMillis = endTimeMillis - durationMinutes * 60_000L,
            endTimeMillis = endTimeMillis,
            durationMinutes = durationMinutes,
            exerciseType = ExerciseType.WALKING,
            title = null,
            notes = null,
            source = DataSource.MANUAL,
            sourceRecordId = null,
            sourcePackageName = null,
            sourceAppName = null,
            createdAt = endTimeMillis,
            updatedAt = endTimeMillis,
            lastSyncedAt = null,
            deletedAt = null,
        )
}
