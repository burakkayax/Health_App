package com.burak.healthapp

import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.buildMonthGridDays
import com.burak.healthapp.feature.detail.buildMonthToDateDays
import com.burak.healthapp.feature.detail.buildPeriodDays
import com.burak.healthapp.feature.detail.buildTrailingDays
import com.burak.healthapp.feature.detail.buildTrailingWeekDays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MetricDateWindowsTest {
    private val anchorDate = LocalDate.of(2026, 4, 27)

    @Test
    fun trailingWeekDays_returnsCalendarWeekContainingAnchor() {
        val days = buildTrailingWeekDays(anchorDate)

        assertEquals(7, days.size)
        assertEquals(LocalDate.of(2026, 4, 27), days.first())
        assertEquals(LocalDate.of(2026, 5, 3), days.last())
    }

    @Test
    fun monthToDateDays_returnsFullCalendarMonthForMetricPeriods() {
        val days = buildMonthToDateDays(anchorDate)

        assertEquals(LocalDate.of(2026, 4, 1), days.first())
        assertEquals(LocalDate.of(2026, 4, 30), days.last())
        assertEquals(30, days.size)
    }

    @Test
    fun buildPeriodDays_matchesSelectedPeriod() {
        assertEquals(buildTrailingWeekDays(anchorDate), buildPeriodDays(anchorDate, TrendsPeriod.WEEKLY))
        assertEquals(buildMonthToDateDays(anchorDate), buildPeriodDays(anchorDate, TrendsPeriod.MONTHLY))
    }

    @Test
    fun buildTrailingDays_handlesNonPositiveCount() {
        assertTrue(buildTrailingDays(anchorDate, 0).isEmpty())
        assertTrue(buildTrailingDays(anchorDate, -1).isEmpty())
    }

    @Test
    fun monthGridDays_startsOnMondayAndEndsOnSunday() {
        val gridDays = buildMonthGridDays(anchorDate)

        assertEquals(1, gridDays.first().dayOfWeek.value)
        assertEquals(7, gridDays.last().dayOfWeek.value)
        assertTrue(gridDays.contains(LocalDate.of(2026, 4, 1)))
        assertTrue(gridDays.contains(LocalDate.of(2026, 4, 30)))
    }
}
