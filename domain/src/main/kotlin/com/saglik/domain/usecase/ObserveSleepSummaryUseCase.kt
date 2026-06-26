@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DateRange
import com.saglik.core.model.SleepEntry
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.sleep.SleepStatsCalculator
import com.saglik.domain.sleep.SleepSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

class ObserveSleepSummaryUseCase(
    private val repository: SleepRepository,
    private val nowDateProvider: () -> LocalDate = {
        java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }
    },
    private val statsCalculator: SleepStatsCalculator = SleepStatsCalculator(),
) {
    operator fun invoke(): Flow<SleepSummary> {
        val range = weeklyRange(nowDateProvider())
        return combine(
            repository.observeLatestSleepEntry(),
            repository.observeSleepEntries(range),
        ) { latest, weeklyEntries ->
            buildSummary(
                latest = latest,
                weeklyEntries = weeklyEntries,
                range = range,
                statsCalculator = statsCalculator,
            )
        }
    }

    companion object {
        fun buildSummary(
            latest: SleepEntry?,
            weeklyEntries: List<SleepEntry>,
            range: DateRange,
            statsCalculator: SleepStatsCalculator = SleepStatsCalculator(),
        ): SleepSummary =
            SleepSummary(
                latestDurationMinutes = latest?.durationMinutes,
                latestQuality = latest?.quality,
                weeklyDurations = statsCalculator.buildChartPoints(weeklyEntries, range),
                hasData = latest != null,
            )

        fun weeklyRange(today: LocalDate): DateRange =
            DateRange(
                start = today.minus(6, DateTimeUnit.DAY),
                end = today,
            )
    }
}
