package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SleepStabilityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class SleepStabilityCalculationsTest {

    private val defaultTargetBedtime = LocalTime.of(23, 0)
    private val defaultTargetWakeTime = LocalTime.of(7, 0)

    @Test
    fun emptySessions_returnsNoData() {
        val result = calculateSleepStabilityMetrics(
            sessions = emptyList(),
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.NO_DATA, result.status)
        assertEquals(0, result.recordCount)
        assertNull(result.averageBedtimeMinutes)
        assertNull(result.averageWakeTimeMinutes)
        assertNull(result.bedtimeVariabilityMinutes)
        assertNull(result.wakeTimeVariabilityMinutes)
    }

    @Test
    fun singleSession_returnsLimitedData() {
        val sessions = listOf(
            sleepSession("2026-04-27T23:00", "2026-04-28T07:00"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.LIMITED_DATA, result.status)
        assertEquals(1, result.recordCount)
        assertNotNull(result.averageBedtimeMinutes)
        assertNotNull(result.averageWakeTimeMinutes)
        assertNull(result.bedtimeVariabilityMinutes)
        assertNull(result.wakeTimeVariabilityMinutes)
    }

    @Test
    fun twoRecords_returnsReady() {
        val sessions = listOf(
            sleepSession("2026-04-27T23:00", "2026-04-28T07:00"),
            sleepSession("2026-04-28T23:30", "2026-04-29T07:30"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.READY, result.status)
        assertEquals(2, result.recordCount)
        assertNotNull(result.averageBedtimeMinutes)
        assertNotNull(result.averageWakeTimeMinutes)
        assertNotNull(result.bedtimeVariabilityMinutes)
        assertNotNull(result.wakeTimeVariabilityMinutes)
    }

    @Test
    fun weeklyAverageBedtime_handlesNormalOvernightSessions() {
        val sessions = listOf(
            sleepSession("2026-04-21T23:00", "2026-04-22T07:00"),
            sleepSession("2026-04-22T23:30", "2026-04-23T07:15"),
            sleepSession("2026-04-23T22:45", "2026-04-24T06:50"),
            sleepSession("2026-04-24T23:15", "2026-04-25T07:10"),
            sleepSession("2026-04-25T23:00", "2026-04-26T07:00"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.READY, result.status)
        // Average bedtime should be around 23:06 (= (23:00+23:30+22:45+23:15+23:00)/5)
        val avgBedtime = result.averageBedtimeMinutes!!
        assertTrue("Average bedtime $avgBedtime should be near 23:06 (1386)", avgBedtime in 1380..1392)
    }

    @Test
    fun monthlyAverageBedtime_handlesNormalOvernightSessions() {
        // Same logic, just more sessions
        val sessions = (1..15).map { dayOffset ->
            sleepSession(
                "2026-04-${String.format("%02d", dayOffset)}T23:00",
                "2026-04-${String.format("%02d", dayOffset + 1)}T07:00",
            )
        }

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.READY, result.status)
        assertEquals(15, result.recordCount)
        // All sessions are 23:00, so average should be exactly 23:00 = 1380 minutes
        assertEquals(1380, result.averageBedtimeMinutes)
    }

    @Test
    fun averageBedtime_handlesMidnightBoundary_2350And0010() {
        val sessions = listOf(
            sleepSession("2026-04-27T23:50", "2026-04-28T07:00"),
            sleepSession("2026-04-28T00:10", "2026-04-28T07:10"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.READY, result.status)
        // 23:50 -> normalized = 1430 minutes
        // 00:10 -> normalized = 1450 minutes (10 + 1440)
        // Average = 1440 -> normalizeMinutesToClock(1440) = 0 minutes = 00:00
        assertEquals(0, result.averageBedtimeMinutes)
    }

    @Test
    fun wakeAverage_handlesMorningWakeTimes() {
        val sessions = listOf(
            sleepSession("2026-04-27T23:00", "2026-04-28T07:00"),
            sleepSession("2026-04-28T23:00", "2026-04-29T07:30"),
            sleepSession("2026-04-29T23:00", "2026-04-30T06:30"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        // Wake times: 07:00=420, 07:30=450, 06:30=390 -> avg = 420 = 07:00
        assertEquals(420, result.averageWakeTimeMinutes)
    }

    @Test
    fun bedtimeVariability_isCalculatedCorrectly() {
        val sessions = listOf(
            sleepSession("2026-04-27T23:00", "2026-04-28T07:00"),
            sleepSession("2026-04-28T23:30", "2026-04-29T07:00"),
            sleepSession("2026-04-29T22:30", "2026-04-30T07:00"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        // Bedtimes normalized: 23:00=1380, 23:30=1410, 22:30=1350
        // Mean = 1380, MAD = (|0| + |30| + |-30|) / 3 = 20
        assertEquals(20, result.bedtimeVariabilityMinutes)
    }

    @Test
    fun wakeTimeVariability_isCalculatedCorrectly() {
        val sessions = listOf(
            sleepSession("2026-04-27T23:00", "2026-04-28T07:00"),
            sleepSession("2026-04-28T23:00", "2026-04-29T07:30"),
            sleepSession("2026-04-29T23:00", "2026-04-30T06:30"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        // Wake times: 420, 450, 390 -> mean=420, MAD=(|0|+|30|+|-30|)/3=20
        assertEquals(20, result.wakeTimeVariabilityMinutes)
    }

    @Test
    fun targetDeviation_usesGoalBedtimeAndWakeTime() {
        val sessions = listOf(
            sleepSession("2026-04-27T23:30", "2026-04-28T07:30"),
            sleepSession("2026-04-28T23:30", "2026-04-29T07:30"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = LocalTime.of(23, 0), // target 23:00
            targetWakeTime = LocalTime.of(7, 0), // target 07:00
        )

        // Average bedtime = 23:30, target = 23:00 -> deviation = 30 min
        assertEquals(30, result.averageBedtimeTargetDeviationMinutes)
        // Average wake = 07:30, target = 07:00 -> deviation = 30 min
        assertEquals(30, result.averageWakeTargetDeviationMinutes)
    }

    @Test
    fun invalidSession_endBeforeStart_isIgnored() {
        val sessions = listOf(
            sleepSession("2026-04-28T07:00", "2026-04-27T23:00"), // invalid: end before start
            sleepSession("2026-04-28T23:00", "2026-04-29T07:00"), // valid
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        // Only 1 valid session -> LIMITED_DATA
        assertEquals(SleepStabilityStatus.LIMITED_DATA, result.status)
        assertEquals(1, result.recordCount)
    }

    @Test
    fun allInvalidSessions_returnsNoData() {
        val sessions = listOf(
            sleepSession("2026-04-28T07:00", "2026-04-27T23:00"),
            sleepSession("2026-04-29T08:00", "2026-04-29T08:00"), // zero duration
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.NO_DATA, result.status)
    }

    @Test
    fun validOvernightSession_isIncluded() {
        val sessions = listOf(
            sleepSession("2026-04-26T23:30", "2026-04-27T07:20"),
            sleepSession("2026-04-27T23:00", "2026-04-28T06:50"),
        )

        val result = calculateSleepStabilityMetrics(
            sessions = sessions,
            targetBedtime = defaultTargetBedtime,
            targetWakeTime = defaultTargetWakeTime,
        )

        assertEquals(SleepStabilityStatus.READY, result.status)
        assertEquals(2, result.recordCount)
    }

    @Test
    fun normalizeMinutesToClock_handlesOverflow() {
        assertEquals(0, normalizeMinutesToClock(1440))
        assertEquals(0, normalizeMinutesToClock(2880))
        assertEquals(60, normalizeMinutesToClock(1500))
    }

    @Test
    fun formatClockMinutes_formatsCorrectly() {
        assertEquals("00:00", formatClockMinutes(0))
        assertEquals("00:00", formatClockMinutes(1440))
        assertEquals("23:50", formatClockMinutes(1430))
        assertEquals("07:00", formatClockMinutes(420))
    }

    private fun sleepSession(start: String, end: String): SleepSession = SleepSession(
        startTime = LocalDateTime.parse(start),
        endTime = LocalDateTime.parse(end),
    )
}
