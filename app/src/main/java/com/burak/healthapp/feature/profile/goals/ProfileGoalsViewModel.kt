package com.burak.healthapp.feature.profile.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.profile.goals.ProfileGoalsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileGoalsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
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
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileGoalsUiState(
                userName = "Misafir",
                avatarInitials = "M",
                goalSettings = GoalSettings(),
                latestMeasurement = null,
                heightCm = null,
            ),
        )

    fun saveGoalsAndMeasurement(
        goals: GoalSettings,
        measurement: BodyMeasurementEntry,
        heightCm: Float?,
        onSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            settingsRepository.updateGoalSettings(goals)
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
}
