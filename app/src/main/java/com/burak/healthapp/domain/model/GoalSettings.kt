package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalTime

data class GoalSettings(
    val dailyCaloriesTarget: Int = DefaultHealthGoals.DAILY_CALORIES,
    val proteinTargetGrams: Int = DefaultHealthGoals.PROTEIN_GRAMS,
    val carbTargetGrams: Int = DefaultHealthGoals.CARB_GRAMS,
    val fatTargetGrams: Int = DefaultHealthGoals.FAT_GRAMS,
    val waterTargetMl: Int = DefaultHealthGoals.WATER_TARGET_ML,
    val dailyStepTarget: Int = DefaultHealthGoals.DAILY_STEPS,
    val sleepTargetBedtime: LocalTime = DefaultHealthGoals.SLEEP_BEDTIME,
    val sleepTargetWakeTime: LocalTime = DefaultHealthGoals.SLEEP_WAKE_TIME,
    val exerciseTargetDaysPerWeek: Int = DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK,
    val exerciseTargetDurationMinutes: Int = DefaultHealthGoals.EXERCISE_DURATION_MINUTES,
    val smokeDailyLimit: Int = DefaultHealthGoals.SMOKE_DAILY_LIMIT,
    val baselineWeightKg: Float = DefaultHealthGoals.BASELINE_WEIGHT_KG,
    val targetWeightKg: Float = DefaultHealthGoals.TARGET_WEIGHT_KG,
    val baselineShoulderCm: Float = DefaultHealthGoals.BASELINE_SHOULDER_CM,
    val baselineWaistCm: Float = DefaultHealthGoals.BASELINE_WAIST_CM,
    val baselineHipCm: Float = DefaultHealthGoals.BASELINE_HIP_CM,
) {
    val sleepTargetMinutes: Int
        get() {
            val bedtimeMinutes = sleepTargetBedtime.toSecondOfDay() / 60
            var wakeMinutes = sleepTargetWakeTime.toSecondOfDay() / 60
            if (wakeMinutes <= bedtimeMinutes) {
                wakeMinutes += (24 * 60)
            }
            return (wakeMinutes - bedtimeMinutes).coerceAtLeast(0)
        }
}

data class WaterReminderSettings(
    val enabled: Boolean = false,
    val startTime: LocalTime = DefaultHealthGoals.WATER_REMINDER_START_TIME,
    val endTime: LocalTime = DefaultHealthGoals.WATER_REMINDER_END_TIME,
    val intervalMinutes: Int = DefaultHealthGoals.WATER_REMINDER_INTERVAL_MINUTES,
)
