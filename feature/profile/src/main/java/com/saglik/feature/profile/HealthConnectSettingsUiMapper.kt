package com.saglik.feature.profile

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.domain.usecase.HealthConnectSyncOutcome
import com.saglik.domain.usecase.HealthConnectSyncResult

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

    fun syncing(
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = ReadyDescription,
            statusMessage = "Syncing Health Connect weight and sleep records...",
            requiredPermissions = requiredPermissions,
            items = syncItems(
                statusDescription = "Health Connect is available and required permissions are granted.",
                status = "Syncing",
                permissionsDescription = "Weight and sleep read access is granted.",
                permissionsStatus = "Granted",
                resultItem = SettingsItemUiState(
                    title = "Sync window",
                    description = "Importing Health Connect weight and sleep records from the last 30 days.",
                    status = "Last 30 days",
                ),
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.SyncWeightAndSleep,
                text = "Syncing weight & sleep",
                enabled = false,
            ),
            secondaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.OpenSettings,
                text = "Open Health Connect settings",
                enabled = false,
            ),
            tertiaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
                enabled = false,
            ),
            isSyncing = true,
        )

    fun fromSyncOutcome(
        outcome: HealthConnectSyncOutcome,
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        when (outcome) {
            is HealthConnectSyncOutcome.Success -> syncSuccess(
                result = outcome.result,
                requiredPermissions = requiredPermissions,
            )
            is HealthConnectSyncOutcome.NoData -> noData(
                result = outcome.result,
                requiredPermissions = requiredPermissions,
            )
            HealthConnectSyncOutcome.PermissionMissing -> permissionsMissing(
                permissionStatus = HealthConnectPermissionStatus.from(
                    requiredPermissions = requiredPermissions,
                    grantedPermissions = emptySet(),
                ),
            )
            HealthConnectSyncOutcome.ProviderUpdateRequired -> providerUpdateRequired(requiredPermissions)
            HealthConnectSyncOutcome.Unsupported -> unsupported(requiredPermissions)
            HealthConnectSyncOutcome.Failed -> syncFailed(requiredPermissions)
        }

    private fun ready(
        permissionStatus: HealthConnectPermissionStatus,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = ReadyDescription,
            statusMessage = "Sync starts only when you tap the button. You can grant or revoke permissions at any time.",
            requiredPermissions = permissionStatus.requiredPermissions,
            items = syncItems(
                statusDescription = "Health Connect is available and required permissions are granted.",
                status = "Ready",
                permissionsDescription = "Weight and sleep read access is granted.",
                permissionsStatus = "Granted",
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.SyncWeightAndSleep,
                text = "Sync weight & sleep",
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

    private fun permissionsMissing(
        permissionStatus: HealthConnectPermissionStatus,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = "Health Connect is available. Grant access to weight and sleep data before syncing.",
            statusMessage = "Sync is not available until required permissions are granted.",
            requiredPermissions = permissionStatus.requiredPermissions,
            items = syncItems(
                statusDescription = "Health Connect is available.",
                status = "Permission needed",
                permissionsDescription = "Weight and sleep read access is not fully granted.",
                permissionsStatus = missingPermissionStatus(permissionStatus),
                scopeStatus = "Blocked",
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
            items = syncItems(
                statusDescription = "Install or update Health Connect, then refresh this section.",
                status = "Update needed",
                permissionsDescription = "Permissions can be checked after Health Connect is available.",
                permissionsStatus = "Not checked",
                scopeStatus = "Unavailable",
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
            items = syncItems(
                statusDescription = "This device or profile does not support Health Connect.",
                status = "Unsupported",
                permissionsDescription = "Permissions are not available on this device.",
                permissionsStatus = "Unavailable",
                scopeStatus = "Unavailable",
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.Refresh,
                text = "Refresh status",
            ),
        )

    private fun syncSuccess(
        result: HealthConnectSyncResult,
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = ReadyDescription,
            statusMessage = "Last sync: just now. ${result.summaryText()}",
            requiredPermissions = requiredPermissions,
            items = syncItems(
                statusDescription = "Health Connect is available and required permissions are granted.",
                status = "Ready",
                permissionsDescription = "Weight and sleep read access is granted.",
                permissionsStatus = "Granted",
                resultItem = result.toSettingsItem(),
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.SyncWeightAndSleep,
                text = "Sync weight & sleep",
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

    private fun noData(
        result: HealthConnectSyncResult,
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = ReadyDescription,
            statusMessage = "No new Health Connect weight or sleep records were found.",
            requiredPermissions = requiredPermissions,
            items = syncItems(
                statusDescription = "Health Connect is available and required permissions are granted.",
                status = "Ready",
                permissionsDescription = "Weight and sleep read access is granted.",
                permissionsStatus = "Granted",
                resultItem = result.toSettingsItem(status = "No data"),
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.SyncWeightAndSleep,
                text = "Sync weight & sleep",
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

    private fun syncFailed(
        requiredPermissions: Set<String>,
    ): HealthConnectSettingsUiState =
        HealthConnectSettingsUiState(
            description = ReadyDescription,
            statusMessage = "Health Connect sync could not be completed right now.",
            requiredPermissions = requiredPermissions,
            items = syncItems(
                statusDescription = "Health Connect is available, but the last sync did not finish.",
                status = "Ready",
                permissionsDescription = "Weight and sleep read access should be checked before trying again.",
                permissionsStatus = "Check again",
                resultItem = SettingsItemUiState(
                    title = "Last sync",
                    description = "Health Connect sync could not be completed right now.",
                    status = "Error",
                    enabled = false,
                ),
            ),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.SyncWeightAndSleep,
                text = "Try sync again",
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

    private fun syncItems(
        statusDescription: String,
        status: String,
        permissionsDescription: String,
        permissionsStatus: String,
        scopeStatus: String = "Last 30 days",
        resultItem: SettingsItemUiState? = null,
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
                description = "User-initiated foreground sync imports only weight and sleep records from the last 30 days.",
                status = scopeStatus,
                enabled = status != "Unsupported" && scopeStatus != "Unavailable",
            ),
        ) + listOfNotNull(resultItem)

    private fun missingPermissionStatus(
        permissionStatus: HealthConnectPermissionStatus,
    ): String =
        when (permissionStatus.missingPermissions.size) {
            0 -> "Granted"
            1 -> "1 missing"
            else -> "${permissionStatus.missingPermissions.size} missing"
        }

    private fun HealthConnectSyncResult.summaryText(): String {
        val skippedText = if (skipped > 0) " Skipped: $skipped." else ""
        return "Weight: $weightInserted inserted, $weightUpdated updated. " +
            "Sleep: $sleepInserted inserted, $sleepUpdated updated.$skippedText"
    }

    private fun HealthConnectSyncResult.toSettingsItem(
        status: String = "Synced",
    ): SettingsItemUiState =
        SettingsItemUiState(
            title = "Last sync",
            description = summaryText(),
            status = status,
        )

    private const val ReadyDescription =
        "Health Connect is ready. Sync imports weight and sleep records from the last 30 days only."
}
