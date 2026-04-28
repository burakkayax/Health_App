package com.burak.healthapp.domain.model

enum class TrendsPeriod {
    WEEKLY,
    MONTHLY,
}

data class TrendPoint(
    val label: String,
    val value: Float,
)

data class CalorieBarPoint(
    val label: String,
    val calories: Int,
    val progress: Float,
)

data class TrendsSnapshot(
    val period: TrendsPeriod,
    val averageProteinGrams: Float,
    val averageSleepMinutes: Float,
    val averageWaterMl: Float,
    val averageSteps: Float,
    val averageCalories: Float,
    val weeklyCalories: List<CalorieBarPoint>,
    val weightPoints: List<TrendPoint>,
    val stepPoints: List<TrendPoint>,
)
