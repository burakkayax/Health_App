package com.burak.healthapp.core.ui.model

import com.burak.healthapp.domain.calculation.directionAwareProgress
import com.burak.healthapp.domain.model.TrendPoint

data class WeeklyCalorieBarState(
    val label: String,
    val calories: Int,
    val progress: Float,
)

data class BmiGaugeState(
    val indicatorFraction: Float? = null,
    val valueLabel: String? = null,
    val helperMessage: String? = null,
)

data class WeightTrendChartState(
    val points: List<TrendPoint>,
    val startWeightKg: Float,
    val targetWeightKg: Float,
    val currentWeightKg: Float,
    val progress: Float,
)

fun buildWeightTrendChartState(
    points: List<TrendPoint>,
    targetWeightKg: Float,
): WeightTrendChartState? {
    if (points.isEmpty()) return null
    val startWeightKg = points.first().value
    val currentWeightKg = points.last().value
    return WeightTrendChartState(
        points = points,
        startWeightKg = startWeightKg,
        targetWeightKg = targetWeightKg,
        currentWeightKg = currentWeightKg,
        progress = directionAwareProgress(
            baseline = startWeightKg,
            current = currentWeightKg,
            target = targetWeightKg,
        ),
    )
}
