package com.burak.healthapp.domain.model

import java.time.LocalDate

data class SettingsState(
    val onboardingCompleted: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val goalSettings: GoalSettings = GoalSettings(),
    val waterReminderSettings: WaterReminderSettings = WaterReminderSettings(),
    val waterReminderSnoozedDate: LocalDate? = null,
    val stepTrackingEnabled: Boolean = false,
    val dashboardCards: List<DashboardCardConfig> = defaultDashboardCardConfig(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)
