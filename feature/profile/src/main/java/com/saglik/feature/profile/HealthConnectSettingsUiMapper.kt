package com.saglik.feature.profile

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus

object HealthConnectSettingsUiMapper {
    fun checking(
        requiredPermissions: Set<String> = emptySet(),
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = "Checking Health Connect...",
            statusMessage = "Checking Health Connect...",
            requiredPermissions = requiredPermissions,
            items = listOf(
                SettingsItemUiState(
                    title = "Status",
                    description = "Health Connect availability is being checked.",
                    status = "Checking",
                    enabled = false,
                ),
                SettingsItemUiState(
                    title = "Permissions",
                    description = "Weight and sleep permission status will appear here.",
                    status = "Checking",
                    enabled = false,
                ),
            ),
            tertiaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
            ),
            isChecking = true,
        )

    fun from(
        availability: HealthConnectAvailability,
        permissionStatus: HealthConnectPermissionStatus?,
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        when (availability) {
            HealthConnectAvailability.Available -> {
                if (permissionStatus?.allRequiredGranted == true) {
                    ready(permissionStatus)
                } else {
                    permissionsMissing(
                        permissionStatus = permissionStatus ?: HealthConnectPermissionStatus.from(
                            requiredPermissions = requiredPermissions,
                            grantedPermissions = emptySet(),
                        ),
                    )
                }
            }

            HealthConnectAvailability.ProviderUpdateRequired -> providerUpdateRequired(requiredPermissions)
            HealthConnectAvailability.Unsupported -> unsupported(requiredPermissions)
        }

    fun error(
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = "Health Connect status could not be checked right now.",
            statusMessage = "Health Connect status could not be checked right now.",
            requiredPermissions = requiredPermissions,
            items = listOf(
                SettingsItemUiState(
                    title = "Status",
                    description = "The Settings screen is still available.",
                    status = "Error",
                    enabled = false,
                ),
                SettingsItemUiState(
                    title = "Permissions",
                    description = "You can try checking again.",
                    status = "Not checked",
                    enabled = false,
                ),
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
            ),
        )

    fun withExternalActionError(
        current: HealthConnectSettingsUiState,
    ): HealthConnectSettingsUiState =
        current.copy(statusMessage = "Health Connect could not be opened right now.")

    private fun ready(
        permissionStatus: HealthConnectPermissionStatus,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = "Health Connect is ready. Weight and sleep sync will be added in a later update.",
            statusMessage = "No data is being imported yet. You can grant or revoke permissions at any time.",
            requiredPermissions = permissionStatus.requiredPermissions,
            items = foundationItems(
                statusDescription = "Health Connect is available and required permissions are granted.",
                status = "Ready",
                permissionsDescription = "Weight and sleep read access is granted.",
                permissionsStatus = "Granted",
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.OpenSettings,
                text = "Open Health Connect settings",
            ),
            secondaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
            ),
        )

    private fun permissionsMissing(
        permissionStatus: HealthConnectPermissionStatus,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = "Health Connect is available. Grant access to weight and sleep data to enable future sync.",
            statusMessage = "No data is being imported yet. You can grant or revoke permissions at any time.",
            requiredPermissions = permissionStatus.requiredPermissions,
            items = foundationItems(
                statusDescription = "Health Connect is available.",
                status = "Permission needed",
                permissionsDescription = "Weight and sleep read access is not fully granted.",
                permissionsStatus = missingPermissionStatus(permissionStatus),
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.GrantPermissions,
                text = "Grant permissions",
            ),
            secondaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.OpenSettings,
                text = "Open Health Connect settings",
            ),
            tertiaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
            ),
        )

    private fun providerUpdateRequired(
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = "Health Connect needs to be installed or updated before it can be connected.",
            statusMessage = "No data is being imported yet.",
            requiredPermissions = requiredPermissions,
            items = foundationItems(
                statusDescription = "Install or update Health Connect, then refresh this section.",
                status = "Update needed",
                permissionsDescription = "Permissions can be checked after Health Connect is available.",
                permissionsStatus = "Not checked",
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.InstallOrUpdate,
                text = "Install or update",
            ),
            secondaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
            ),
        )

    private fun unsupported(
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = "Health Connect is not available on this device.",
            statusMessage = "No data is being imported yet.",
            requiredPermissions = requiredPermissions,
            items = foundationItems(
                statusDescription = "This device or profile does not support Health Connect.",
                status = "Unsupported",
                permissionsDescription = "Permissions are not available on this device.",
                permissionsStatus = "Unavailable",
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
            ),
        )

    private fun foundationItems(
        statusDescription: String,
        status: String,
        permissionsDescription: String,
        permissionsStatus: String,
    ): List<SettingsItemUiState> =
        listOf(
            SettingsItemUiState(
                title = "Status",
                description = statusDescription,
                status = status,
                enabled = status != "Unsupported",
            ),
            SettingsItemUiState(
                title = "Permissions",
                description = permissionsDescription,
                status = permissionsStatus,
                enabled = status != "Unsupported",
            ),
            SettingsItemUiState(
                title = "Scope",
                description = "This foundation only prepares weight and sleep access. Weight and sleep sync will be added in a later update.",
                status = "No import",
                enabled = false,
            ),
        )

    private fun missingPermissionStatus(
        permissionStatus: HealthConnectPermissionStatus,
    ): String =
        when (permissionStatus.missingPermissions.size) {
            0 -> "Granted"
            1 -> "1 missing"
            else -> "${permissionStatus.missingPermissions.size} missing"
        }
}
