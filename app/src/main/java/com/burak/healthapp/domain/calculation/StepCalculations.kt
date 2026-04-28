package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.TrendPoint
import java.time.LocalDate

fun averageSteps(entries: List<StepEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .associate { entry -> entry.date to entry.steps }
    return averageByLoggedDays(totalsByDay)
}

fun buildStepTrendPoints(
    entries: List<StepEntry>,
    days: List<LocalDate>,
): List<TrendPoint> {
    if (entries.none { it.date in days }) return emptyList()
    return toTrendPoints(
        days = days,
        valuesByDate = entries
            .filter { it.date in days }
            .associate { entry -> entry.date to entry.steps.toFloat() },
    )
}
