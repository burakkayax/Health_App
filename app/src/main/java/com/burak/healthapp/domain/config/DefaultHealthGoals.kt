package com.burak.healthapp.domain.config

import java.time.LocalTime

object DefaultHealthGoals {
    const val DAILY_CALORIES = 2200
    const val PROTEIN_GRAMS = 160
    const val CARB_GRAMS = 220
    const val CARBS_GRAMS = CARB_GRAMS
    const val FAT_GRAMS = 70
    const val WATER_TARGET_ML = 2500
    const val DAILY_STEPS = 8000
    val SLEEP_BEDTIME: LocalTime = LocalTime.of(23, 0)
    val SLEEP_WAKE_TIME: LocalTime = LocalTime.of(7, 0)
    const val LEGACY_SLEEP_TARGET_MINUTES = 480
    const val EXERCISE_DAYS_PER_WEEK = 4
    const val EXERCISE_DURATION_MINUTES = 45
    const val SMOKE_DAILY_LIMIT = 0
    const val BASELINE_WEIGHT_KG = 78f
    const val TARGET_WEIGHT_KG = 74f
    const val BASELINE_SHOULDER_CM = 118f
    const val BASELINE_WAIST_CM = 88f
    const val BASELINE_HIP_CM = 99f
    val WATER_REMINDER_START_TIME: LocalTime = LocalTime.of(9, 0)
    val WATER_REMINDER_END_TIME: LocalTime = LocalTime.of(21, 0)
    const val WATER_REMINDER_INTERVAL_MINUTES = 60
    const val DEFAULT_WATER_REMINDER_INTERVAL_MINUTES = WATER_REMINDER_INTERVAL_MINUTES
    const val MIN_WATER_REMINDER_INTERVAL_MINUTES = 15
}
