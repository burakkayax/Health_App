@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.StepsEntry
import com.saglik.domain.repository.StepsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveStepsSummaryUseCaseTest {
    private val today = LocalDate(2026, 6, 27)

    @Test
    fun emptyRepositoryReturnsNoDataSummary() = runBlocking {
        val useCase = ObserveStepsSummaryUseCase(
            repository = FakeStepsRepository(),
            todayProvider = { today },
            timeZone = TimeZone.UTC,
        )

        val summary = useCase().first()

        assertFalse(summary.hasData)
        assertEquals(0L, summary.totalStepsToday)
        assertEquals(0L, summary.totalStepsLast7Days)
        assertEquals(null, summary.latestEntryCount)
    }

    @Test
    fun sumsTodayAndLastSevenDays() = runBlocking {
        val useCase = ObserveStepsSummaryUseCase(
            repository = FakeStepsRepository(
                entries = listOf(
                    stepsEntry(
                        id = "outside-range",
                        start = "2026-06-19T12:00:00Z",
                        end = "2026-06-19T12:30:00Z",
                        count = 400L,
                    ),
                    stepsEntry(
                        id = "range-start",
                        start = "2026-06-21T07:00:00Z",
                        end = "2026-06-21T07:30:00Z",
                        count = 1_000L,
                    ),
                    stepsEntry(
                        id = "yesterday",
                        start = "2026-06-26T18:00:00Z",
                        end = "2026-06-26T18:30:00Z",
                        count = 2_000L,
                    ),
                    stepsEntry(
                        id = "overlaps-today",
                        start = "2026-06-26T23:30:00Z",
                        end = "2026-06-27T00:30:00Z",
                        count = 500L,
                    ),
                    stepsEntry(
                        id = "today",
                        start = "2026-06-27T08:00:00Z",
                        end = "2026-06-27T09:00:00Z",
                        count = 6_240L,
                    ),
                ),
            ),
            todayProvider = { today },
            timeZone = TimeZone.UTC,
        )

        val summary = useCase().first()

        assertTrue(summary.hasData)
        assertEquals(6_740L, summary.totalStepsToday)
        assertEquals(9_740L, summary.totalStepsLast7Days)
        assertEquals(6_240L, summary.latestEntryCount)
    }

    private class FakeStepsRepository(
        private val entries: List<StepsEntry> = emptyList(),
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
    ): StepsEntry =
        StepsEntry(
            id = id,
            startTimeMillis = Instant.parse(start).toEpochMilliseconds(),
            endTimeMillis = Instant.parse(end).toEpochMilliseconds(),
            count = count,
            source = DataSource.HEALTH_CONNECT,
            note = null,
            sourceRecordId = id,
            sourcePackageName = "com.example.source",
            sourceAppName = "Example Source",
            createdAt = Instant.parse(end).toEpochMilliseconds(),
            updatedAt = Instant.parse(end).toEpochMilliseconds(),
            lastSyncedAt = null,
            deletedAt = null,
        )
}
