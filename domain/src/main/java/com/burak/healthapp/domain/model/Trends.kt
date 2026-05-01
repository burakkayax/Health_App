package com.burak.healthapp.domain.model

import java.time.LocalDate

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
    val days: List<LocalDate>,
    val previousDays: List<LocalDate>,
    val averageProteinGrams: Float,
    val previousAverageProteinGrams: Float,
    val averageSleepMinutes: Float,
    val previousAverageSleepMinutes: Float,
    val averageWaterMl: Float,
    val previousAverageWaterMl: Float,
    val averageSteps: Float,
    val previousAverageSteps: Float,
    val averageCalories: Float,
    val previousAverageCalories: Float,
    val averageCaffeineMg: Float,
    val previousAverageCaffeineMg: Float,
    val averageSmokingCount: Float,
    val previousAverageSmokingCount: Float,
    val exerciseTotalMinutes: Int,
    val previousExerciseTotalMinutes: Int,
    val exerciseActiveDays: Int,
    val previousExerciseActiveDays: Int,
    val waterGoalMetDays: Int,
    val stepGoalMetDays: Int,
    val sleepGoalMetDays: Int,
    val caffeineUnderLimitDays: Int,
    val caffeineOverLimitDays: Int,
    val caffeineAfterCutoffDays: Int,
    val smokingUnderLimitDays: Int,
    val smokingOverLimitDays: Int,
    val smokingZeroDays: Int,
    val nutritionLoggedDays: Int,
    val hydrationLoggedDays: Int,
    val sleepLoggedDays: Int,
    val stepLoggedDays: Int,
    val caffeineLoggedDays: Int,
    val smokingLoggedDays: Int,
    val exerciseLoggedDays: Int,
    val weightRecordCount: Int,
    val weightStartKg: Float?,
    val weightEndKg: Float?,
    val weeklyCalories: List<CalorieBarPoint>,
    val weightPoints: List<TrendPoint>,
    val stepPoints: List<TrendPoint>,
)
