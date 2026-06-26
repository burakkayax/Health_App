@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.datetime.Instant

class ResolveSleepTimeRangeUseCase(
    private val nowProvider: () -> ZonedDateTime = { ZonedDateTime.now(ZoneId.systemDefault()) },
    private val zoneProvider: () -> ZoneId = { ZoneId.systemDefault() }
) {
    fun invoke(
        startHour: Int,
        startMinute: Int,
        wakeHour: Int,
        wakeMinute: Int
    ): Pair<Instant, Instant> {
        val startTime = LocalTime.of(startHour, startMinute)
        val endTime = LocalTime.of(wakeHour, wakeMinute)
        
        val zone = zoneProvider()
        val endInstant = endTime.resolveEndInstant(zone)
        val startInstant = startTime.resolveStartInstant(endTime, zone)
        
        return Pair(startInstant, endInstant)
    }

    private fun LocalTime.resolveEndInstant(zone: ZoneId): Instant {
        val now = nowProvider()
        val endDate = if (this.isAfter(now.toLocalTime())) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        return ZonedDateTime.of(endDate, this, zone).toKotlinInstant()
    }

    private fun LocalTime.resolveStartInstant(endTime: LocalTime, zone: ZoneId): Instant {
        val now = nowProvider()
        val endDate = if (endTime.isAfter(now.toLocalTime())) {
            now.toLocalDate().minusDays(1)
        } else {
            now.toLocalDate()
        }
        val startDate = if (this.isAfter(endTime)) {
            endDate.minusDays(1)
        } else {
            endDate
        }
        return ZonedDateTime.of(startDate, this, zone).toKotlinInstant()
    }

    private fun ZonedDateTime.toKotlinInstant(): Instant =
        Instant.fromEpochMilliseconds(toInstant().toEpochMilli())
}
