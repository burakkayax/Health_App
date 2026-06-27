package com.saglik.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex
import com.saglik.core.model.UserProfile
import com.saglik.domain.usecase.CheckHealthConnectAvailabilityUseCase
import com.saglik.domain.usecase.GetHealthConnectPermissionStatusUseCase
import com.saglik.domain.usecase.GetHealthConnectRequiredPermissionsUseCase
import com.saglik.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val checkHealthConnectAvailabilityUseCase: CheckHealthConnectAvailabilityUseCase,
    private val getHealthConnectPermissionStatusUseCase: GetHealthConnectPermissionStatusUseCase,
    getHealthConnectRequiredPermissionsUseCase: GetHealthConnectRequiredPermissionsUseCase,
) : ViewModel() {
    private val requiredHealthConnectPermissions = getHealthConnectRequiredPermissionsUseCase()
    private val healthConnectState = MutableStateFlow(
        HealthConnectSettingsUiMapper.checking(requiredHealthConnectPermissions),
    )

    val uiState: StateFlow<SettingsUiState> =
        combine(
            observeUserProfileUseCase(),
            healthConnectState,
        ) { profile, healthConnect ->
                SettingsUiState(
                    profileSummary = profile.toProfileSummary(),
                    personalHealthContext = profile.toPersonalHealthContext(),
                    healthConnect = healthConnect,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SettingsUiState.loading(
                    healthConnect = HealthConnectSettingsUiMapper.checking(
                        requiredHealthConnectPermissions,
                    ),
                ),
            )

    init {
        refreshHealthConnectStatus()
    }

    fun refreshHealthConnectStatus() {
        viewModelScope.launch {
            healthConnectState.value = HealthConnectSettingsUiMapper.checking(
                requiredHealthConnectPermissions,
            )

            runCatching {
                val availability = checkHealthConnectAvailabilityUseCase()
                val permissionStatus = if (availability == HealthConnectAvailability.Available) {
                    getHealthConnectPermissionStatusUseCase()
                } else {
                    null
                }

                HealthConnectSettingsUiMapper.from(
                    availability = availability,
                    permissionStatus = permissionStatus,
                    requiredPermissions = requiredHealthConnectPermissions,
                )
            }.onSuccess { healthConnect ->
                healthConnectState.value = healthConnect
            }.onFailure {
                healthConnectState.value = HealthConnectSettingsUiMapper.error(
                    requiredHealthConnectPermissions,
                )
            }
        }
    }

    fun onGrantHealthConnectPermissionsClick() = Unit

    @Suppress("UNUSED_PARAMETER")
    fun onHealthConnectPermissionResult(grantedPermissions: Set<String>) {
        refreshHealthConnectStatus()
    }

    fun onHealthConnectExternalActionError() {
        healthConnectState.update { current ->
            HealthConnectSettingsUiMapper.withExternalActionError(current)
        }
    }

    private fun UserProfile?.toProfileSummary(): ProfileSummaryUiState {
        if (this == null) {
            return ProfileSummaryUiState.empty()
        }

        return ProfileSummaryUiState(
            displayName = "Your health profile",
            supportingText = "Basics saved from onboarding.",
            initials = "P",
            details = listOf(
                SettingsMetricUiState("Age", age?.let { "$it years" } ?: birthDate?.let { "Born ${it.year}" } ?: "Not set"),
                SettingsMetricUiState("Height", heightCm.formatCentimeters()),
                SettingsMetricUiState("Goal", goal.displayName()),
                SettingsMetricUiState("Sex", sex.displayName()),
            ),
            editActionText = "Edit profile (coming soon)",
            hasProfile = true,
        )
    }

    private fun UserProfile?.toPersonalHealthContext(): PersonalHealthContextUiState {
        if (this == null) {
            return PersonalHealthContextUiState.empty()
        }

        return PersonalHealthContextUiState(
            description = "Used to personalize future trends and insights.",
            metrics = listOf(
                SettingsMetricUiState("Height", heightCm.formatCentimeters()),
                SettingsMetricUiState("Weight goal", goal.displayName()),
                SettingsMetricUiState("Tracking goal", goal.displayName()),
                SettingsMetricUiState("Preferred units", "Metric"),
                SettingsMetricUiState("Health context", "More health context options will be added later"),
            ),
        )
    }

    private fun Float.formatCentimeters(): String =
        if (this % 1f == 0f) {
            "${toInt()} cm"
        } else {
            String.format(Locale.US, "%.1f cm", this)
        }

    private fun HealthGoal.displayName(): String =
        when (this) {
            HealthGoal.LOSE_WEIGHT -> "Lose weight"
            HealthGoal.GAIN_WEIGHT -> "Gain weight"
            HealthGoal.MAINTAIN_WEIGHT -> "Maintain weight"
            HealthGoal.BUILD_MUSCLE -> "Build muscle"
            HealthGoal.GENERAL_HEALTH -> "General health"
        }

    private fun Sex.displayName(): String =
        when (this) {
            Sex.MALE -> "Male"
            Sex.FEMALE -> "Female"
            Sex.OTHER -> "Other"
            Sex.UNSPECIFIED -> "Not specified"
        }
}
