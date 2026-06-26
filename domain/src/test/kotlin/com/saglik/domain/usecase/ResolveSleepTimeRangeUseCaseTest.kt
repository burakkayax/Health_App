@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.datetime.Instant

class ResolveSleepTimeRangeUseCaseTest {
    private val fixedZone = ZoneId.of("UTC")
    private val fixedNow = ZonedDateTime.of(2024, 1, 2, 12, 0, 0, 0, fixedZone)

    private val useCase = ResolveSleepTimeRangeUseCase(
        nowProvider = { fixedNow },
        zoneProvider = { fixedZone }
    )

    @Test
    fun `overnight completed sleep resolves correctly`() {
        val (start, end) = useCase.invoke(
            startHour = 23,
            startMinute = 50,
            wakeHour = 7,
            wakeMinute = 14
        )

        val startZoned = start.toZonedDateTime()
        val endZoned = end.toZonedDateTime()

        assertEquals(2024, startZoned.year)
        assertEquals(1, startZoned.monthValue)
        assertEquals(1, startZoned.dayOfMonth)
        assertEquals(23, startZoned.hour)
        assertEquals(50, startZoned.minute)

        assertEquals(2024, endZoned.year)
        assertEquals(1, endZoned.monthValue)
        assertEquals(2, endZoned.dayOfMonth)
        assertEquals(7, endZoned.hour)
        assertEquals(14, endZoned.minute)

        assertTrue(end.toEpochMilliseconds() > start.toEpochMilliseconds())
    }

    @Test
    fun `same day sleep resolves correctly`() {
        val (start, end) = useCase.invoke(
            startHour = 0,
            startMinute = 30,
            wakeHour = 8,
            wakeMinute = 0
        )

        val startZoned = start.toZonedDateTime()
        val endZoned = end.toZonedDateTime()

        assertEquals(2, startZoned.dayOfMonth)
        assertEquals(0, startZoned.hour)
        assertEquals(30, startZoned.minute)

        assertEquals(2, endZoned.dayOfMonth)
        assertEquals(8, endZoned.hour)
        assertEquals(0, endZoned.minute)

        assertTrue(end.toEpochMilliseconds() > start.toEpochMilliseconds())
    }

    @Test
    fun `wake time in future resolves to previous day`() {
        // Now is 12:00. Wake time is 14:00 (future). Should resolve to yesterday.
        val (start, end) = useCase.invoke(
            startHour = 6,
            startMinute = 0,
            wakeHour = 14,
            wakeMinute = 0
        )

        val startZoned = start.toZonedDateTime()
        val endZoned = end.toZonedDateTime()

        assertEquals(1, startZoned.dayOfMonth)
        assertEquals(6, startZoned.hour)

        assertEquals(1, endZoned.dayOfMonth)
        assertEquals(14, endZoned.hour)

        assertTrue(end.toEpochMilliseconds() > start.toEpochMilliseconds())
    }

    @Test
    fun `wake time earlier than start time resolves across midnight correctly`() {
        // Start 22:00, Wake 6:00
        val (start, end) = useCase.invoke(
            startHour = 22,
            startMinute = 0,
            wakeHour = 6,
            wakeMinute = 0
        )

        val startZoned = start.toZonedDateTime()
        val endZoned = end.toZonedDateTime()

        assertEquals(1, startZoned.dayOfMonth)
        assertEquals(22, startZoned.hour)

        assertEquals(2, endZoned.dayOfMonth)
        assertEquals(6, endZoned.hour)
    }

    private fun Instant.toZonedDateTime(): ZonedDateTime =
        java.time.Instant.ofEpochMilli(toEpochMilliseconds()).atZone(fixedZone)
}
