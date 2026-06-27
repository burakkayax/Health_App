@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.saglik.feature.summary

import com.saglik.core.model.DataSource
import com.saglik.core.model.StepsEntry
import com.saglik.domain.repository.StepsRepository
import com.saglik.domain.usecase.ObserveStepsEntriesUseCase
import com.saglik.domain.usecase.ObserveStepsSummaryUseCase
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StepsDetailViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun mapsEmptyState() = runTest {
        val state = createViewModel().loadedState()

        assertEquals("No steps yet", state.todayStepsText)
        assertEquals("Sync Health Connect to import steps.", state.todayLabelText)
        assertEquals("Last 7 days unavailable", state.lastSevenDaysText)
        assertEquals("No entries logged", state.latestEntryText)
        assertTrue(state.isEmpty)
        assertFalse(state.isLoading)
        assertEquals(emptyList<StepsHistoryItemUiState>(), state.historyItems)
    }

    @Test
    fun mapsNonEmptyStateNewestFirst() = runTest {
        val state = createViewModel(
            entries = listOf(
                stepsEntry(
                    id = "older",
                    start = "2026-06-26T08:00:00Z",
                    end = "2026-06-26T09:00:00Z",
                    count = 1_500L,
                    sourceAppName = null,
                ),
                stepsEntry(
                    id = "latest",
                    start = "2026-06-27T10:00:00Z",
                    end = "2026-06-27T11:00:00Z",
                    count = 6_240L,
                    sourceAppName = "Fit Sync",
                ),
            ),
        ).loadedState()

        assertEquals("6,240 steps", state.todayStepsText)
        assertEquals("Today", state.todayLabelText)
        assertEquals("7,740 in last 7 days", state.lastSevenDaysText)
        assertEquals("6,240 steps", state.latestEntryText)
        assertFalse(state.isEmpty)
        assertEquals("latest", state.historyItems.first().id)
        assertEquals("Fit Sync", state.historyItems.first().sourceText)
        assertEquals("1,500 steps", state.historyItems[1].countText)
    }

    private suspend fun StepsDetailViewModel.loadedState(): StepsDetailUiState =
        uiState.drop(1).first()

    private fun createViewModel(
        entries: List<StepsEntry> = emptyList(),
    ): StepsDetailViewModel {
        val repository = FakeStepsRepository(entries)
        return StepsDetailViewModel(
            observeStepsSummaryUseCase = ObserveStepsSummaryUseCase(
                repository = repository,
                todayProvider = { LocalDate(2026, 6, 27) },
                timeZone = TimeZone.UTC,
            ),
            observeStepsEntriesUseCase = ObserveStepsEntriesUseCase(repository),
        )
    }

    private class FakeStepsRepository(
        private val entries: List<StepsEntry>,
    ) : StepsRepository {
        override fun observeStepsEntries(): Flow<List<StepsEntry>> =
            flowOf(entries)

        override fun observeStepsEntriesBetween(
            startInclusive: Long,
            endExclusive: Long,
        ): Flow<List<StepsEntry>> =
            flowOf(
                entries.filter {
                    it.startTimeMillis < endExclusive && it.endTimeMillis > startInclusive
                },
            )
    }

    private fun stepsEntry(
        id: String,
        start: String,
        end: String,
        count: Long,
        sourceAppName: String?,
    ): StepsEntry =
        StepsEntry(
            id = id,
            startTimeMillis = Instant.parse(start).toEpochMilli(),
            endTimeMillis = Instant.parse(end).toEpochMilli(),
            count = count,
            source = DataSource.HEALTH_CONNECT,
            note = null,
            sourceRecordId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = sourceAppName,
            createdAt = Instant.parse(end).toEpochMilli(),
            updatedAt = Instant.parse(end).toEpochMilli(),
            lastSyncedAt = null,
            deletedAt = null,
        )
}
