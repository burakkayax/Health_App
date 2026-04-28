package com.burak.healthapp

import com.burak.healthapp.core.reminder.calculateNextWaterReminderDelay
import com.burak.healthapp.core.reminder.isInsideWaterReminderWindow
import com.burak.healthapp.core.reminder.shouldShowWaterReminder
import com.burak.healthapp.domain.model.WaterReminderSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class WaterReminderScheduleTest {
    @Test
    fun initialDelay_alignsToSameDayStartWhenBeforeWindow() {
        val delay = calculateNextWaterReminderDelay(
            now = LocalDateTime.of(2026, 4, 27, 8, 30),
            settings = settings(start = LocalTime.of(9, 0), end = LocalTime.of(21, 0), interval = 60),
        )

        assertEquals(Duration.ofMinutes(30), delay)
    }

    @Test
    fun initialDelay_alignsToNextIntervalInsideWindow() {
        val delay = calculateNextWaterReminderDelay(
            now = LocalDateTime.of(2026, 4, 27, 9, 30),
            settings = settings(start = LocalTime.of(9, 0), end = LocalTime.of(21, 0), interval = 60),
        )

        assertEquals(Duration.ofMinutes(30), delay)
    }

    @Test
    fun initialDelay_movesToNextStartAfterWindow() {
        val delay = calculateNextWaterReminderDelay(
            now = LocalDateTime.of(2026, 4, 27, 22, 0),
            settings = settings(start = LocalTime.of(9, 0), end = LocalTime.of(21, 0), interval = 60),
        )

        assertEquals(Duration.ofHours(11), delay)
    }

    @Test
    fun overnightWindow_isHandledAcrossMidnight() {
        val reminder = settings(start = LocalTime.of(22, 0), end = LocalTime.of(2, 0), interval = 60)

        assertTrue(isInsideWaterReminderWindow(LocalTime.of(23, 0), reminder))
        assertTrue(isInsideWaterReminderWindow(LocalTime.of(1, 30), reminder))
        assertFalse(isInsideWaterReminderWindow(LocalTime.of(12, 0), reminder))
        assertEquals(
            Duration.ofMinutes(30),
            calculateNextWaterReminderDelay(LocalDateTime.of(2026, 4, 27, 21, 30), reminder),
        )
    }

    @Test
    fun initialDelay_usesMinimumIntervalWhenConfiguredBelowLimit() {
        val delay = calculateNextWaterReminderDelay(
            now = LocalDateTime.of(2026, 4, 27, 9, 5),
            settings = settings(start = LocalTime.of(9, 0), end = LocalTime.of(21, 0), interval = 1),
        )

        assertEquals(Duration.ofMinutes(10), delay)
    }

    @Test
    fun initialDelay_keepsExactWindowStartAtZeroDelay() {
        val delay = calculateNextWaterReminderDelay(
            now = LocalDateTime.of(2026, 4, 27, 9, 0),
            settings = settings(start = LocalTime.of(9, 0), end = LocalTime.of(21, 0), interval = 60),
        )

        assertEquals(Duration.ZERO, delay)
    }

    @Test
    fun shouldShowReminder_respectsSnoozeAndCompletedTarget() {
        val today = LocalDate.of(2026, 4, 27)

        assertFalse(shouldShowWaterReminder(today, today, currentMl = 500, targetMl = 2500))
        assertFalse(shouldShowWaterReminder(today, null, currentMl = 2500, targetMl = 2500))
        assertTrue(shouldShowWaterReminder(today, today.minusDays(1), currentMl = 500, targetMl = 2500))
    }

    private fun settings(
        start: LocalTime,
        end: LocalTime,
        interval: Int,
    ): WaterReminderSettings = WaterReminderSettings(
        enabled = true,
        startTime = start,
        endTime = end,
        intervalMinutes = interval,
    )
}
