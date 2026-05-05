package com.burak.healthapp.data.repository

import com.burak.healthapp.domain.calculation.metricDateWindowFor
import com.burak.healthapp.domain.model.TrendsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrendsRepositoryDateWindowTest {
    @Test
    fun trendsWeekly_usesSharedWeekWindow() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 6), TrendsPeriod.WEEKLY)

        assertEquals(LocalDate.of(2026, 5, 4), window.startDate)
        assertEquals(LocalDate.of(2026, 5, 10), window.endDateInclusive)
    }

    @Test
    fun trendsMonthly_usesCalendarMonthWindow() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 5, 1), window.startDate)
        assertEquals(LocalDate.of(2026, 5, 31), window.endDateInclusive)
    }

    @Test
    fun trendsMonthly_earlyMonthDoesNotIncludePreviousMonthDays() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertFalse(LocalDate.of(2026, 4, 30) in window)
    }

    @Test
    fun trendsMonthly_endBoundaryIncludesLastDayOfMonth() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertTrue(LocalDate.of(2026, 5, 31) in window)
    }

    @Test
    fun trendsMonthly_endExclusiveExcludesNextMonthFirstDay() {
        val window = metricDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 6, 1), window.endDateExclusive)
        assertFalse(LocalDate.of(2026, 6, 1) in window)
    }
}
