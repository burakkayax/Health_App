package com.burak.healthapp

import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.ui.sleepdetail.buildSleepCalendarWeeks
import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepDetailCalendarTest {
    @Test
    fun monthlyCalendar_usesMondayStartAndMarksOutsideDaysInactive() {
        val weeks = buildSleepCalendarWeeks(
            anchorDate = LocalDate.of(2026, 4, 24),
            sessionsByDate = emptyMap(),
            targetMinutes = 480f,
        )

        assertEquals("30", weeks.first().days.first().dayLabel)
        assertFalse(weeks.first().days.first().isInCurrentMonth)
        assertEquals("1", weeks.first().days[2].dayLabel)
        assertTrue(weeks.first().days[2].isInCurrentMonth)
        assertEquals(5, weeks.size)
    }

    @Test
    fun monthlyCalendar_clampsProgressAndDoesNotFillMissingDays() {
        val targetDate = LocalDate.of(2026, 4, 10)
        val fullNight = SleepSession(
            startTime = LocalDateTime.of(2026, 4, 9, 22, 30),
            endTime = LocalDateTime.of(2026, 4, 10, 7, 30),
        )

        val weeks = buildSleepCalendarWeeks(
            anchorDate = LocalDate.of(2026, 4, 24),
            sessionsByDate = mapOf(targetDate to fullNight),
            targetMinutes = 480f,
        )
        val loggedDay = weeks.flatMap { it.days }.first { it.isInCurrentMonth && it.dayLabel == "10" }
        val missingDay = weeks.flatMap { it.days }.first { it.isInCurrentMonth && it.dayLabel == "11" }

        assertEquals(1f, loggedDay.progress)
        assertTrue(loggedDay.hasData)
        assertTrue(loggedDay.isTargetMet)
        assertEquals(0f, missingDay.progress)
        assertFalse(missingDay.hasData)
    }
}
