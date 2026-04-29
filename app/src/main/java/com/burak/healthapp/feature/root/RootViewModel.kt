package com.burak.healthapp.feature.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class RootUiState(
    val isLoaded: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val userName: String = "",
    val avatarInitials: String = "M",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val waterReminderEnabled: Boolean = false,
    val stepTrackingEnabled: Boolean = false,
)

@HiltViewModel
class RootViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {
    val uiState = settingsRepository.settings
        .map { settings ->
            RootUiState(
                isLoaded = true,
                onboardingCompleted = settings.onboardingCompleted,
                userName = settings.userProfile.name,
                avatarInitials = settings.userProfile.avatarInitials,
                themeMode = settings.themeMode,
                waterReminderEnabled = settings.waterReminderSettings.enabled,
                stepTrackingEnabled = settings.stepTrackingEnabled,
            )
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RootUiState(),
        )
}
