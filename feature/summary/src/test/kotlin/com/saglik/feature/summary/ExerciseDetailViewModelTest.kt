@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.saglik.feature.summary

import com.saglik.core.model.DataSource
import com.saglik.core.model.ExerciseSession
import com.saglik.core.model.ExerciseType
import com.saglik.domain.repository.AddExerciseSessionInput
import com.saglik.domain.repository.ExerciseRepository
import com.saglik.domain.usecase.ObserveExerciseSessionsUseCase
import com.saglik.domain.usecase.ObserveExerciseSummaryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ExerciseDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun mapsEmptyState() = runTest {
        val state = createViewModel().loadedState()

        assertEquals("No exercise yet", state.sessionCountText)
        assertEquals("Sync Health Connect or add a session later.", state.sessionLabelText)
        assertEquals("0 min total", state.totalDurationText)
        assertEquals("No sessions logged", state.latestSessionText)
        assertTrue(state.isEmpty)
        assertFalse(state.isLoading)
        assertEquals(emptyList<ExerciseHistoryItemUiState>(), state.historyItems)
    }

    @Test
    fun mapsNonEmptyStateNewestFirst() = runTest {
        val state = createViewModel(
            sessions = listOf(
                exerciseSession(
                    id = "older",
                    endTimeMillis = 1_800_000L,
                    durationMinutes = 30,
                    title = "Morning walk",
                    sourceAppName = null,
                ),
                exerciseSession(
                    id = "latest",
                    endTimeMillis = 3_600_000L,
                    durationMinutes = 45,
                    title = "Evening ride",
                    sourceAppName = "Fit Sync",
                ),
            ),
        ).loadedState()

        assertEquals("2 sessions", state.sessionCountText)
        assertEquals("Sessions", state.sessionLabelText)
        assertEquals("1h 15m total", state.totalDurationText)
        assertEquals("Evening ride", state.latestSessionText)
        assertFalse(state.isEmpty)
        assertEquals("latest", state.historyItems.first().id)
        assertEquals("Fit Sync", state.historyItems.first().sourceText)
        assertEquals("30m", state.historyItems[1].durationText)
    }

    private suspend fun ExerciseDetailViewModel.loadedState(): ExerciseDetailUiState =
        uiState.drop(1).first()

    private fun createViewModel(
        sessions: List<ExerciseSession> = emptyList(),
    ): ExerciseDetailViewModel {
        val repository = FakeExerciseRepository(sessions)
        return ExerciseDetailViewModel(
            observeExerciseSummaryUseCase = ObserveExerciseSummaryUseCase(repository),
            observeExerciseSessionsUseCase = ObserveExerciseSessionsUseCase(repository),
        )
    }

    private class FakeExerciseRepository(
        private val sessions: List<ExerciseSession>,
    ) : ExerciseRepository {
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

        override suspend fun addExerciseSession(input: AddExerciseSessionInput) = Unit
    }

    private fun exerciseSession(
        id: String,
        endTimeMillis: Long,
        durationMinutes: Int,
        title: String?,
        sourceAppName: String?,
    ): ExerciseSession =
        ExerciseSession(
            id = id,
            startTimeMillis = endTimeMillis - durationMinutes * 60_000L,
            endTimeMillis = endTimeMillis,
            durationMinutes = durationMinutes,
            exerciseType = ExerciseType.CYCLING,
            title = title,
            notes = null,
            source = DataSource.HEALTH_CONNECT,
            sourceRecordId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = sourceAppName,
            createdAt = endTimeMillis,
            updatedAt = endTimeMillis,
            lastSyncedAt = null,
            deletedAt = null,
        )
}
