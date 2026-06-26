@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DateRange
import com.saglik.core.model.SleepQuality
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveSleepSummaryUseCaseTest {
    private val today = LocalDate(2026, 6, 26)

    @Test
    fun latestSleepAndWeeklyDurationsAreGenerated() = runBlocking {
        val old = sleepEntry(
            id = "old",
            start = "2026-06-24T23:00:00Z",
            end = "2026-06-25T07:00:00Z",
            durationMinutes = 480,
            quality = SleepQuality.GOOD,
        )
        val latest = sleepEntry(
            id = "latest",
            start = "2026-06-25T23:50:00Z",
            end = "2026-06-26T07:14:00Z",
            durationMinutes = 444,
            quality = SleepQuality.GOOD,
        )
        val useCase = ObserveSleepSummaryUseCase(
            repository = FakeSleepRepository(listOf(old, latest)),
            nowDateProvider = { today },
        )

        val summary = useCase().first()

        assertTrue(summary.hasData)
        assertEquals(444, summary.latestDurationMinutes)
        assertEquals(7, summary.weeklyDurations.size)
        assertEquals(listOf(480f, 444f), summary.weeklyDurations.takeLast(2).map { it.value })
    }

    @Test
    fun emptyRepositoryReturnsNoDataSummary() = runBlocking {
        val useCase = ObserveSleepSummaryUseCase(
            repository = FakeSleepRepository(),
            nowDateProvider = { today },
        )

        val summary = useCase().first()

        assertFalse(summary.hasData)
        assertEquals(null, summary.latestDurationMinutes)
        assertEquals(7, summary.weeklyDurations.size)
    }

    @Test
    fun weeklyRangeCoversLastSevenDays() {
        assertEquals(
            DateRange(
                start = LocalDate(2026, 6, 20),
                end = LocalDate(2026, 6, 26),
            ),
            ObserveSleepSummaryUseCase.weeklyRange(today),
        )
    }
}
