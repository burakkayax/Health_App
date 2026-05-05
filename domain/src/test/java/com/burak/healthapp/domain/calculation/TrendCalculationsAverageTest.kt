package com.burak.healthapp.domain.calculation

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TrendCalculationsAverageTest {
    @Test
    fun averageByLoggedDays_ignoresMissingDays() {
        val values = mapOf(
            LocalDate.of(2026, 5, 4) to 100,
            LocalDate.of(2026, 5, 6) to 300,
        )

        assertEquals(200f, averageByLoggedDays(values), 0.001f)
    }

    @Test
    fun averageByLoggedDays_countsRecordedZeroDay() {
        val values = mapOf(
            LocalDate.of(2026, 5, 4) to 0,
            LocalDate.of(2026, 5, 5) to 300,
        )

        assertEquals(150f, averageByLoggedDays(values), 0.001f)
    }

    @Test
    fun averageByPeriodDays_includesMissingDaysAsZero() {
        val window = MetricDateWindow(
            startDate = LocalDate.of(2026, 5, 4),
            endDateInclusive = LocalDate.of(2026, 5, 10),
        )
        val values = mapOf(
            LocalDate.of(2026, 5, 4) to 70,
            LocalDate.of(2026, 5, 7) to 70,
        )

        assertEquals(20f, averageByPeriodDays(values, window), 0.001f)
    }

    @Test
    fun averageByPeriodDays_usesWindowDayCount() {
        val window = MetricDateWindow(
            startDate = LocalDate.of(2026, 2, 1),
            endDateInclusive = LocalDate.of(2026, 2, 28),
        )
        val values = mapOf(LocalDate.of(2026, 2, 1) to 280)

        assertEquals(10f, averageByPeriodDays(values, window), 0.001f)
    }

    @Test
    fun averageByPeriodDays_ignoresValuesOutsideWindow() {
        val window = MetricDateWindow(
            startDate = LocalDate.of(2026, 5, 1),
            endDateInclusive = LocalDate.of(2026, 5, 31),
        )
        val values = mapOf(
            LocalDate.of(2026, 4, 30) to 3100,
            LocalDate.of(2026, 5, 31) to 31,
            LocalDate.of(2026, 6, 1) to 3100,
        )

        assertEquals(1f, averageByPeriodDays(values, window), 0.001f)
    }
}
