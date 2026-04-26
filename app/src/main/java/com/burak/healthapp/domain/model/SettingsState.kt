package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class SettingsState(
    val onboardingCompleted: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val goalSettings: GoalSettings = GoalSettings(),
    val waterReminderSettings: WaterReminderSettings = WaterReminderSettings(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
