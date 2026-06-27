@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.StepsEntry
import com.saglik.domain.repository.StepsRepository
import com.saglik.domain.steps.StepsSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

class ObserveStepsSummaryUseCase(
    private val repository: StepsRepository,
    private val todayProvider: () -> LocalDate = {
        java.time.LocalDate.now().let { LocalDate(it.year, it.monthValue, it.dayOfMonth) }
    },
    private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    operator fun invoke(): Flow<StepsSummary> {
        val today = todayProvider()
        val todayStart = today.startOfDayMillis(timeZone)
        val tomorrowStart = today.plus(1, DateTimeUnit.DAY).startOfDayMillis(timeZone)
        val lastSevenDaysStart = today.minus(6, DateTimeUnit.DAY).startOfDayMillis(timeZone)

        return repository.observeStepsEntries().map { entries ->
            buildSummary(
                entries = entries,
                todayStartInclusive = todayStart,
                todayEndExclusive = tomorrowStart,
                lastSevenDaysStartInclusive = lastSevenDaysStart,
                lastSevenDaysEndExclusive = tomorrowStart,
            )
        }
    }

    companion object {
        fun buildSummary(
            entries: List<StepsEntry>,
            todayStartInclusive: Long,
            todayEndExclusive: Long,
            lastSevenDaysStartInclusive: Long,
            lastSevenDaysEndExclusive: Long,
        ): StepsSummary {
            val latestEntry = entries.maxByOrNull { it.endTimeMillis }
            return StepsSummary(
                totalStepsToday = entries
                    .filter { it.overlaps(todayStartInclusive, todayEndExclusive) }
                    .sumOf { it.count },
                totalStepsLast7Days = entries
                    .filter { it.overlaps(lastSevenDaysStartInclusive, lastSevenDaysEndExclusive) }
                    .sumOf { it.count },
                latestEntryCount = latestEntry?.count,
                hasData = entries.isNotEmpty(),
            )
        }

        private fun StepsEntry.overlaps(startInclusive: Long, endExclusive: Long): Boolean =
            startTimeMillis < endExclusive && endTimeMillis > startInclusive
    }

    private fun LocalDate.startOfDayMillis(timeZone: TimeZone): Long =
        LocalDateTime(this, LocalTime(0, 0))
            .toInstant(timeZone)
            .toEpochMilliseconds()
}
