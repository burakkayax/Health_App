package com.burak.healthapp.feature.profile.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.core.reminder.WaterReminderScheduler
import com.burak.healthapp.feature.profile.goals.ProfileGoalsUiState
import com.burak.healthapp.feature.root.healthApplication
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
                stepTrackingEnabled = settings.stepTrackingEnabled,
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
            stepTrackingEnabled = false,
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

    fun updateStepTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateStepTrackingEnabled(enabled)
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
