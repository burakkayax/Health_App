@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.sleep

import com.saglik.core.model.ChartPoint
import com.saglik.core.model.DateRange
import com.saglik.core.model.SleepEntry
import kotlin.math.roundToInt
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

data class SleepStats(
    val averageMinutes: Int?,
    val shortestMinutes: Int?,
    val longestMinutes: Int?,
)

class SleepStatsCalculator(
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    fun calculate(entries: List<SleepEntry>): SleepStats {
        val durations = entries.map { it.durationMinutes }
        if (durations.isEmpty()) {
            return SleepStats(
                averageMinutes = null,
                shortestMinutes = null,
                longestMinutes = null,
            )
        }

        return SleepStats(
            averageMinutes = durations.average().roundToInt(),
            shortestMinutes = durations.minOrNull(),
            longestMinutes = durations.maxOrNull(),
        )
    }

    fun buildChartPoints(
        entries: List<SleepEntry>,
        range: DateRange,
    ): List<ChartPoint> {
        val latestByDay = entries
            .groupBy { it.endTime.toLocalDateTime(timeZone).date }
            .mapValues { (_, dayEntries) ->
                dayEntries.maxByOrNull { it.endTime.toEpochMilliseconds() }
            }

        return range.days().map { date ->
            val entry = latestByDay[date]
            ChartPoint(
                label = date.dayOfMonth.toString(),
                value = entry?.durationMinutes?.toFloat() ?: 0f,
            )
        }
    }

    private fun DateRange.days(): List<LocalDate> {
        val days = mutableListOf<LocalDate>()
        var date = start
        while (date <= end) {
            days += date
            date = date.plus(1, DateTimeUnit.DAY)
        }
        return days
    }
}
