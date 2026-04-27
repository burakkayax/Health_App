package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TodaySnapshot
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<SettingsState>

    fun observeSupplementTemplates(): Flow<List<SupplementTemplate>>

    suspend fun completeOnboarding(
        profile: UserProfile,
        goals: GoalSettings,
        initialMeasurement: BodyMeasurementEntry,
        supplements: List<String>,
    )

    suspend fun updateGoalSettings(goals: GoalSettings)

    suspend fun updateWaterReminderSettings(settings: WaterReminderSettings)

    suspend fun updateStepTrackingEnabled(enabled: Boolean)

    suspend fun updateProfile(profile: UserProfile)

    suspend fun updateThemeMode(mode: ThemeMode)

    suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>)
}
