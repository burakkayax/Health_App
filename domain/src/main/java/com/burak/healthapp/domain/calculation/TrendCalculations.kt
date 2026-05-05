package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.TrendPoint
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun toTrendPoints(
    days: List<LocalDate>,
    valuesByDate: Map<LocalDate, Float>,
): List<TrendPoint> = days.map { date ->
    TrendPoint(
        label = date.toTrendLabel(),
        value = valuesByDate[date] ?: 0f,
    )
}

fun clipWeightTrendDays(
    days: List<LocalDate>,
    earliestMeasurementDate: LocalDate?,
): List<LocalDate> {
    if (earliestMeasurementDate == null) return emptyList()
    return days.filterNot { it.isBefore(earliestMeasurementDate) }
}

fun buildInterpolatedWeightTrendPoints(
    days: List<LocalDate>,
    measurements: List<WeightMeasurementSample>,
): List<TrendPoint> {
    if (days.isEmpty() || measurements.isEmpty()) return emptyList()

    val resolvedSamples = measurements
        .groupBy(WeightMeasurementSample::date)
        .mapNotNull { (date, samples) ->
            samples.lastOrNull()?.let { sample ->
                WeightMeasurementSample(
                    date = date,
                    weightKg = sample.weightKg,
                )
            }
        }
        .sortedBy(WeightMeasurementSample::date)

    if (resolvedSamples.isEmpty()) return emptyList()

    return days.map { date ->
        TrendPoint(
            label = date.toTrendLabel(),
            value = interpolateWeightForDate(
                date = date,
                measurements = resolvedSamples,
            ),
        )
    }
}

data class WeightMeasurementSample(
    val date: LocalDate,
    val weightKg: Float,
)

private fun interpolateWeightForDate(
    date: LocalDate,
    measurements: List<WeightMeasurementSample>,
): Float {
    measurements.firstOrNull { it.date == date }?.let { sample ->
        return sample.weightKg
    }

    val previous = measurements.lastOrNull { it.date.isBefore(date) }
    val next = measurements.firstOrNull { it.date.isAfter(date) }

    return when {
        previous != null && next != null -> {
            val totalDays = ChronoUnit.DAYS.between(previous.date, next.date).toFloat()
            val elapsedDays = ChronoUnit.DAYS.between(previous.date, date).toFloat()
            if (totalDays <= 0f) {
                previous.weightKg
            } else {
                previous.weightKg + ((next.weightKg - previous.weightKg) * (elapsedDays / totalDays))
            }
        }

        previous != null -> previous.weightKg
        next != null -> next.weightKg
        else -> measurements.first().weightKg
    }
}

internal fun averageByLoggedDays(valuesByDay: Map<LocalDate, Int>): Float {
    if (valuesByDay.isEmpty()) return 0f
    return valuesByDay.values.sum().toFloat() / valuesByDay.size
}

internal fun averageByPeriodDays(
    valuesByDay: Map<LocalDate, Int>,
    window: MetricDateWindow,
): Float {
    if (window.dayCount <= 0) return 0f
    return valuesByDay
        .filterKeys { date -> date in window }
        .values
        .sum()
        .toFloat() / window.dayCount
}
