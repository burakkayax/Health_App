@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.sleep

import kotlinx.datetime.Instant

class SleepDurationCalculator {
    fun calculateMinutes(
        startTime: Instant,
        endTime: Instant,
    ): Int? {
        val durationMillis = endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()
        if (durationMillis <= 0) return null

        return (durationMillis / MILLIS_PER_MINUTE).toInt()
    }

    fun isValidDuration(durationMinutes: Int): Boolean =
        durationMinutes in MIN_DURATION_MINUTES..MAX_DURATION_MINUTES

    companion object {
        const val MIN_DURATION_MINUTES = 30
        const val MAX_DURATION_MINUTES = 18 * 60
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
