package com.burak.healthapp

import com.burak.healthapp.domain.calculation.metricDateWindowFor
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.buildPeriodDays
import com.burak.healthapp.feature.detail.sleep.sleepDetailDateWindowFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.LocalDate

class SleepDetailDateWindowTest {
    @Test
    fun sleepDetailMonthly_usesCalendarMonthWindow() {
        val window = sleepDetailDateWindowFor(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 5, 1), window.startDate)
        assertEquals(LocalDate.of(2026, 5, 31), window.endDateInclusive)
    }

    @Test
    fun sleepDetailWeekly_usesSharedWeekWindow() {
        val window = sleepDetailDateWindowFor(LocalDate.of(2026, 5, 10), TrendsPeriod.WEEKLY)

        assertEquals(LocalDate.of(2026, 5, 4), window.startDate)
        assertEquals(LocalDate.of(2026, 5, 10), window.endDateInclusive)
    }

    @Test
    fun sleepDetailMonthly_dayListMatchesQueryWindow() {
        val selectedDate = LocalDate.of(2026, 5, 5)
        val window = sleepDetailDateWindowFor(selectedDate, TrendsPeriod.MONTHLY)

        assertEquals(window.days(), buildPeriodDays(selectedDate, TrendsPeriod.MONTHLY))
    }

    @Test
    fun sleepDetailMonthly_earlyMonthDoesNotUseRollingThirtyDays() {
        val days = buildPeriodDays(LocalDate.of(2026, 5, 5), TrendsPeriod.MONTHLY)

        assertEquals(LocalDate.of(2026, 5, 1), days.first())
        assertFalse(days.contains(LocalDate.of(2026, 4, 6)))
    }

    @Test
    fun trendsAndSleepDetail_useSameMonthlyWindow() {
        val selectedDate = LocalDate.of(2026, 5, 5)

        assertEquals(
            metricDateWindowFor(selectedDate, TrendsPeriod.MONTHLY),
            sleepDetailDateWindowFor(selectedDate, TrendsPeriod.MONTHLY),
        )
    }
}
