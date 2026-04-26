package com.burak.healthapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.data.repository.DashboardRepository
import com.burak.healthapp.data.repository.SettingsRepository
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.reminders.WaterReminderScheduler
import com.burak.healthapp.ui.model.ProfileGoalsUiState
import com.burak.healthapp.ui.root.healthApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileGoalsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
    private val waterReminderScheduler: WaterReminderScheduler,
) : ViewModel() {
    val uiState = combine(
        settingsRepository.settings,
        dashboardRepository.observeLatestMeasurement(),
    ) { settings, latestMeasurement ->
            ProfileGoalsUiState(
                userName = settings.userProfile.name,
                avatarInitials = settings.userProfile.avatarInitials,
                goalSettings = settings.goalSettings,
                latestMeasurement = latestMeasurement,
                heightCm = settings.userProfile.heightCm,
                waterReminderSettings = settings.waterReminderSettings,
            )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileGoalsUiState(
            userName = "Misafir",
            avatarInitials = "M",
            goalSettings = GoalSettings(),
            latestMeasurement = null,
            heightCm = null,
            waterReminderSettings = WaterReminderSettings(),
        ),
    )

    fun saveGoalsAndMeasurement(
        goals: GoalSettings,
        measurement: BodyMeasurementEntry,
        heightCm: Float?,
        waterReminderSettings: WaterReminderSettings,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            settingsRepository.updateGoalSettings(goals)
            settingsRepository.updateWaterReminderSettings(waterReminderSettings)
            waterReminderScheduler.apply(waterReminderSettings)
            settingsRepository.updateProfile(
                UserProfile(
                    name = uiState.value.userName,
                    avatarInitials = uiState.value.avatarInitials,
                    heightCm = heightCm,
                ),
            )
            dashboardRepository.saveBodyMeasurement(measurement)
            onSaved()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileGoalsViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                    dashboardRepository = healthApplication().container.dashboardRepository,
                    waterReminderScheduler = healthApplication().container.waterReminderScheduler,
                )
            }
        }
    }
}
