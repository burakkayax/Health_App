@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DateRange
import com.saglik.core.model.PeriodType
import com.saglik.core.model.SleepEntry
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.sleep.SleepDetail
import com.saglik.domain.sleep.SleepStatsCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ObserveSleepDetailUseCase(
    private val repository: SleepRepository,
    private val nowDateProvider: () -> LocalDate = {
        java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }
    },
    private val statsCalculator: SleepStatsCalculator = SleepStatsCalculator(),
) {
    operator fun invoke(periodType: PeriodType): Flow<SleepDetail> {
        val range = periodType.toRange(nowDateProvider())
        return repository.observeSleepEntries(range).map { entries ->
            buildDetail(
                periodType = periodType,
                entries = entries,
                range = range,
                statsCalculator = statsCalculator,
            )
        }
    }

    companion object {
        fun buildDetail(
            periodType: PeriodType,
            entries: List<SleepEntry>,
            range: DateRange,
            statsCalculator: SleepStatsCalculator = SleepStatsCalculator(),
        ): SleepDetail {
            val history = entries.sortedByDescending { it.endTime.toEpochMilliseconds() }
            val stats = statsCalculator.calculate(history)

            return SleepDetail(
                periodType = periodType,
                latestDurationMinutes = history.firstOrNull()?.durationMinutes,
                averageMinutes = stats.averageMinutes,
                shortestMinutes = stats.shortestMinutes,
                longestMinutes = stats.longestMinutes,
                chartPoints = statsCalculator.buildChartPoints(entries, range),
                entries = history,
            )
        }

        fun PeriodType.toRange(today: LocalDate): DateRange {
            val days = when (this) {
                PeriodType.WEEKLY -> 6
                PeriodType.MONTHLY -> 29
            }
            return DateRange(
                start = today.minus(days, DateTimeUnit.DAY),
                end = today,
            )
        }
    }
}
