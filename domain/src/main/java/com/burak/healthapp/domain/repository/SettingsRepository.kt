package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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

    suspend fun updateWaterReminderSnoozedDate(date: LocalDate?)

    suspend fun updateStepTrackingEnabled(enabled: Boolean)

    suspend fun updateDashboardCardVisibility(type: DashboardCardType, isVisible: Boolean)

    suspend fun moveDashboardCard(type: DashboardCardType, newIndex: Int)

    suspend fun resetDashboardCardsToDefault()

    suspend fun updateProfile(profile: UserProfile)

    suspend fun updateThemeMode(mode: ThemeMode)

    suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>)
}
