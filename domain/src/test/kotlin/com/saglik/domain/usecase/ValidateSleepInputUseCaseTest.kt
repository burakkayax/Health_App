@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import kotlinx.datetime.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateSleepInputUseCaseTest {
    private val now = Instant.parse("2026-06-26T12:00:00Z")
    private val useCase = ValidateSleepInputUseCase(nowProvider = { now })

    @Test
    fun missingStartIsInvalid() {
        val result = useCase(
            SleepInput(
                startTime = null,
                endTime = Instant.parse("2026-06-26T07:00:00Z"),
                quality = null,
                note = null,
            ),
        )

        assertFalse(result.isValid)
    }

    @Test
    fun missingEndIsInvalid() {
        val result = useCase(
            SleepInput(
                startTime = Instant.parse("2026-06-25T23:00:00Z"),
                endTime = null,
                quality = null,
                note = null,
            ),
        )

        assertFalse(result.isValid)
    }

    @Test
    fun futureEndIsInvalid() {
        val result = useCase(
            SleepInput(
                startTime = Instant.parse("2026-06-26T10:00:00Z"),
                endTime = Instant.parse("2026-06-26T13:00:00Z"),
                quality = null,
                note = null,
            ),
        )

        assertFalse(result.isValid)
    }

    @Test
    fun validOvernightSleepPasses() {
        val result = useCase(
            SleepInput(
                startTime = Instant.parse("2026-06-25T23:50:00Z"),
                endTime = Instant.parse("2026-06-26T07:14:00Z"),
                quality = null,
                note = null,
            ),
        )

        assertTrue(result.isValid)
    }

    @Test
    fun tooShortSleepIsInvalid() {
        val result = useCase(
            SleepInput(
                startTime = Instant.parse("2026-06-26T07:00:00Z"),
                endTime = Instant.parse("2026-06-26T07:29:00Z"),
                quality = null,
                note = null,
            ),
        )

        assertFalse(result.isValid)
    }

    @Test
    fun tooLongSleepIsInvalid() {
        val result = useCase(
            SleepInput(
                startTime = Instant.parse("2026-06-25T12:00:00Z"),
                endTime = Instant.parse("2026-06-26T06:01:00Z"),
                quality = null,
                note = null,
            ),
        )

        assertFalse(result.isValid)
    }
}
