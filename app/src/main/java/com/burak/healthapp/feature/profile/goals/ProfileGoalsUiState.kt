package com.burak.healthapp.feature.profile.goals

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.WaterReminderSettings

data class ProfileGoalsUiState(
    val userName: String,
    val avatarInitials: String,
    val goalSettings: GoalSettings,
    val latestMeasurement: BodyMeasurementEntry?,
    val heightCm: Float? = null,
    val waterReminderSettings: WaterReminderSettings = WaterReminderSettings(),
    val stepTrackingEnabled: Boolean = false,
)
