package com.burak.healthapp.feature.root

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.HealthApplication
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RootUiState(
    val isLoaded: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val userName: String = "",
    val avatarInitials: String = "M",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val waterReminderEnabled: Boolean = false,
)

class RootViewModel(
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
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RootUiState(),
        )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                RootViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                )
            }
        }
    }
}

fun CreationExtras.healthApplication(): HealthApplication {
    return this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as HealthApplication
}
