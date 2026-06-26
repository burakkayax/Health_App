@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.WeightEntry
import com.saglik.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class WeightTrendSummary(
    val latestEntry: WeightEntry?,
    val chartPoints: List<WeightTrendPoint>,
    val highestKg: Float?,
    val lowestKg: Float?,
    val history: List<WeightEntry>,
)

data class WeightTrendPoint(
    val recordedAt: Instant,
    val weightKg: Float,
)

class ObserveWeightTrendSummaryUseCase(
    private val repository: WeightRepository,
) {
    operator fun invoke(): Flow<WeightTrendSummary> =
        repository.observeWeightEntries().map(::buildSummary)

    companion object {
        fun buildSummary(entries: List<WeightEntry>): WeightTrendSummary {
            val history = entries.sortedByDescending { it.recordedAt.toEpochMilliseconds() }
            val latestPerDay = entries
                .groupBy { it.recordedAt.toLocalDateTime(TimeZone.UTC).date }
                .values
                .mapNotNull { dayEntries ->
                    dayEntries.maxByOrNull { it.recordedAt.toEpochMilliseconds() }
                }
                .sortedBy { it.recordedAt.toEpochMilliseconds() }
            val points = latestPerDay.map { entry ->
                WeightTrendPoint(
                    recordedAt = entry.recordedAt,
                    weightKg = entry.weightKg,
                )
            }

            return WeightTrendSummary(
                latestEntry = history.firstOrNull(),
                chartPoints = points,
                highestKg = points.maxByOrNull { it.weightKg }?.weightKg,
                lowestKg = points.minByOrNull { it.weightKg }?.weightKg,
                history = history,
            )
        }
    }
}
