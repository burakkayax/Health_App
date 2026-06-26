@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DateRange
import com.saglik.core.model.PeriodType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveSleepDetailUseCaseTest {
    private val today = LocalDate(2026, 6, 26)

    @Test
    fun weeklyPeriodFiltersAndStatsAreCorrect() = runBlocking {
        val entries = listOf(
            sleepEntry("out", "2026-06-01T23:00:00Z", "2026-06-02T07:00:00Z", 480),
            sleepEntry("short", "2026-06-23T01:10:00Z", "2026-06-23T07:00:00Z", 350),
            sleepEntry("long", "2026-06-24T23:30:00Z", "2026-06-25T07:45:00Z", 495),
            sleepEntry("latest", "2026-06-25T23:50:00Z", "2026-06-26T07:14:00Z", 444),
        )
        val useCase = ObserveSleepDetailUseCase(
            repository = FakeSleepRepository(entries),
            nowDateProvider = { today },
        )

        val detail = useCase(PeriodType.WEEKLY).first()

        assertEquals(PeriodType.WEEKLY, detail.periodType)
        assertEquals(444, detail.latestDurationMinutes)
        assertEquals(430, detail.averageMinutes)
        assertEquals(350, detail.shortestMinutes)
        assertEquals(495, detail.longestMinutes)
        assertEquals(listOf("latest", "long", "short"), detail.entries.map { it.id })
        assertEquals(7, detail.chartPoints.size)
    }

    @Test
    fun monthlyPeriodFiltersCorrectly() = runBlocking {
        val entries = listOf(
            sleepEntry("old", "2026-05-01T23:00:00Z", "2026-05-02T07:00:00Z", 480),
            sleepEntry("monthly", "2026-06-10T23:00:00Z", "2026-06-11T07:00:00Z", 480),
        )
        val useCase = ObserveSleepDetailUseCase(
            repository = FakeSleepRepository(entries),
            nowDateProvider = { today },
        )

        val detail = useCase(PeriodType.MONTHLY).first()

        assertEquals(listOf("monthly"), detail.entries.map { it.id })
        assertEquals(30, detail.chartPoints.size)
    }

    @Test
    fun emptyDetailIsSafe() = runBlocking {
        val useCase = ObserveSleepDetailUseCase(
            repository = FakeSleepRepository(),
            nowDateProvider = { today },
        )

        val detail = useCase(PeriodType.WEEKLY).first()

        assertTrue(detail.entries.isEmpty())
        assertEquals(null, detail.averageMinutes)
        assertEquals(null, detail.shortestMinutes)
        assertEquals(null, detail.longestMinutes)
    }

    @Test
    fun periodRangesAreCorrect() {
        assertEquals(
            DateRange(LocalDate(2026, 6, 20), LocalDate(2026, 6, 26)),
            with(ObserveSleepDetailUseCase.Companion) {
                PeriodType.WEEKLY.toRange(today)
            },
        )
        assertEquals(
            DateRange(LocalDate(2026, 5, 28), LocalDate(2026, 6, 26)),
            with(ObserveSleepDetailUseCase.Companion) {
                PeriodType.MONTHLY.toRange(today)
            },
        )
    }
}
