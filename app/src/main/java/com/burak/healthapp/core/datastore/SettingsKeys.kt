package com.burak.healthapp.core.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val onboardingComplete = booleanPreferencesKey("onboarding_complete")
    val userName = stringPreferencesKey("user_name")
    val avatarInitials = stringPreferencesKey("avatar_initials")
    val heightCm = floatPreferencesKey("height_cm")
    val themeMode = stringPreferencesKey("theme_mode")
    val dailyCaloriesTarget = intPreferencesKey("daily_calories_target")
    val proteinTarget = intPreferencesKey("protein_target")
    val carbTarget = intPreferencesKey("carb_target")
    val fatTarget = intPreferencesKey("fat_target")
    val waterTarget = intPreferencesKey("water_target")
    val dailyStepTarget = intPreferencesKey("daily_step_target")
    val sleepTarget = intPreferencesKey("sleep_target")
    val sleepTargetBedtime = stringPreferencesKey("sleep_target_bedtime")
    val sleepTargetWakeTime = stringPreferencesKey("sleep_target_wake_time")
    val exerciseTargetDays = intPreferencesKey("exercise_target_days")
    val exerciseTargetDuration = intPreferencesKey("exercise_target_duration")
    val smokeDailyLimit = intPreferencesKey("smoke_daily_limit")
    val waterReminderEnabled = booleanPreferencesKey("water_reminder_enabled")
    val waterReminderStartTime = stringPreferencesKey("water_reminder_start_time")
    val waterReminderEndTime = stringPreferencesKey("water_reminder_end_time")
    val waterReminderIntervalMinutes = intPreferencesKey("water_reminder_interval_minutes")
    val baselineWeight = floatPreferencesKey("baseline_weight")
    val targetWeight = floatPreferencesKey("target_weight")
    val baselineShoulder = floatPreferencesKey("baseline_shoulder")
    val baselineWaist = floatPreferencesKey("baseline_waist")
    val baselineHip = floatPreferencesKey("baseline_hip")
}
