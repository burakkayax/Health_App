@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.sleep

import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SleepDurationCalculatorTest {
    private val calculator = SleepDurationCalculator()

    @Test
    fun overnightDurationIsCalculatedCorrectly() {
        val start = Instant.parse("2026-06-25T20:50:00Z")
        val end = Instant.parse("2026-06-26T04:14:00Z")

        assertEquals(444, calculator.calculateMinutes(start, end))
    }

    @Test
    fun sameDayDurationIsCalculatedCorrectly() {
        val start = Instant.parse("2026-06-26T00:00:00Z")
        val end = Instant.parse("2026-06-26T08:00:00Z")

        assertEquals(480, calculator.calculateMinutes(start, end))
    }

    @Test
    fun negativeDurationIsHandledSafely() {
        val start = Instant.parse("2026-06-26T08:00:00Z")
        val end = Instant.parse("2026-06-26T00:00:00Z")

        assertNull(calculator.calculateMinutes(start, end))
    }

    @Test
    fun durationLimitsAreEnforced() {
        assertFalse(calculator.isValidDuration(29))
        assertTrue(calculator.isValidDuration(30))
        assertTrue(calculator.isValidDuration(18 * 60))
        assertFalse(calculator.isValidDuration(18 * 60 + 1))
    }
}
