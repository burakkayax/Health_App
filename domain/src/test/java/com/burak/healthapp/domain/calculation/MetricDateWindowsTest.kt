package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.TrendsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MetricDateWindowsTest {
    @Test
    fun weeklyWindow_startsOnMondayAndEndsOnSunday() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 6), TrendsPeriod.WEEKLY)

        assertEquals(LocalDate.of(2026, 5, 4), window.startDate)
        assertEquals(LocalDate.of(2026, 5, 10), window.endDateInclusive)
        assertEquals(7, window.dayCount)
    }

    @Test
    fun weeklyWindow_forSundayUsesPreviousMonday() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 10), TrendsPeriod.WEEKLY)

        assertEquals(LocalDate.of(2026, 5, 4), window.startDate)
        assertEquals(LocalDate.of(2026, 5, 10), window.endDateInclusive)
    }

    @Test
    fun monthlyWindow_startsAtFirstDayOfMonth() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 5, 1), window.startDate)
    }

    @Test
    fun monthlyWindow_endsAtLastDayOfMonth() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 5, 31), window.endDateInclusive)
        assertEquals(31, window.dayCount)
    }

    @Test
    fun monthlyWindow_handlesFebruaryLeapYear() {
        val window = metricDateWindowFor(LocalDate.of(2028, 2, 14), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2028, 2, 1), window.startDate)
        assertEquals(LocalDate.of(2028, 2, 29), window.endDateInclusive)
        assertEquals(29, window.dayCount)
    }

    @Test
    fun monthlyWindow_handlesDecemberToJanuaryBoundary() {
        val current = metricDateWindowFor(LocalDate.of(2026, 1, 3), TrendsPeriod.MONTHLY)
        val previous = previousMetricDateWindowFor(LocalDate.of(2026, 1, 3), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 1, 1), current.startDate)
        assertEquals(LocalDate.of(2026, 1, 31), current.endDateInclusive)
        assertEquals(LocalDate.of(2025, 12, 1), previous.startDate)
        assertEquals(LocalDate.of(2025, 12, 31), previous.endDateInclusive)
    }

    @Test
    fun monthlyWindow_endExclusiveExcludesNextMonth() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertTrue(LocalDate.of(2026, 5, 31) in window)
        assertFalse(LocalDate.of(2026, 6, 1) in window)
    }

    @Test
    fun endExclusive_isDayAfterEndInclusive() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 6, 1), window.endDateExclusive)
    }

    @Test
    fun days_returnsAllDaysInclusive() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 6), TrendsPeriod.WEEKLY)

        assertEquals(
            listOf(
                LocalDate.of(2026, 5, 4),
                LocalDate.of(2026, 5, 5),
                LocalDate.of(2026, 5, 6),
                LocalDate.of(2026, 5, 7),
                LocalDate.of(2026, 5, 8),
                LocalDate.of(2026, 5, 9),
                LocalDate.of(2026, 5, 10),
            ),
            window.days(),
        )
    }
}
