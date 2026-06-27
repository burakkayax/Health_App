package com.saglik.feature.profile

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.domain.usecase.HealthConnectSyncOutcome
import com.saglik.domain.usecase.HealthConnectSyncResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectSettingsUiMapperTest {
    private val readWeight = "android.permission.health.READ_WEIGHT"
    private val readSleep = "android.permission.health.READ_SLEEP"
    private val requiredPermissions = setOf(readWeight, readSleep)

    @Test
    fun availableWithAllPermissionsGrantedMapsToReadyState() {
        val state = HealthConnectSettingsUiMapper.from(
            availability = HealthConnectAvailability.Available,
            permissionStatus = HealthConnectPermissionStatus.from(
                requiredPermissions = requiredPermissions,
                grantedPermissions = requiredPermissions,
            ),
            requiredPermissions = requiredPermissions,
        )

        assertEquals(requiredPermissions, state.requiredPermissions)
        assertEquals(
            "Health Connect is ready. Sync imports weight and sleep records from the last 30 days only.",
            state.description,
        )
        assertEquals("Ready", state.item("Status").status)
        assertEquals("Granted", state.item("Permissions").status)
        assertEquals("Last 30 days", state.item("Scope").status)
        assertEquals(HealthConnectAction.SyncWeightAndSleep, state.primaryAction?.action)
        assertEquals(HealthConnectAction.OpenSettings, state.secondaryAction?.action)
        assertEquals(HealthConnectAction.Refresh, state.tertiaryAction?.action)
        assertFalse(state.isChecking)
        assertFalse(state.isSyncing)
    }

    @Test
    fun availableWithMissingPermissionsMapsToGrantPermissionsState() {
        val state = HealthConnectSettingsUiMapper.from(
            availability = HealthConnectAvailability.Available,
            permissionStatus = HealthConnectPermissionStatus.from(
                requiredPermissions = requiredPermissions,
                grantedPermissions = setOf(readWeight),
            ),
            requiredPermissions = requiredPermissions,
        )

        assertEquals(requiredPermissions, state.requiredPermissions)
        assertEquals("Permission needed", state.item("Status").status)
        assertEquals("1 missing", state.item("Permissions").status)
        assertEquals("Blocked", state.item("Scope").status)
        assertEquals(HealthConnectAction.GrantPermissions, state.primaryAction?.action)
        assertEquals(HealthConnectAction.OpenSettings, state.secondaryAction?.action)
        assertEquals(HealthConnectAction.Refresh, state.tertiaryAction?.action)
    }

    @Test
    fun providerUpdateRequiredMapsToInstallOrUpdateState() {
        val state = HealthConnectSettingsUiMapper.from(
            availability = HealthConnectAvailability.ProviderUpdateRequired,
            permissionStatus = null,
            requiredPermissions = requiredPermissions,
        )

        assertEquals(requiredPermissions, state.requiredPermissions)
        assertEquals("Update needed", state.item("Status").status)
        assertEquals("Not checked", state.item("Permissions").status)
        assertEquals(HealthConnectAction.InstallOrUpdate, state.primaryAction?.action)
        assertEquals(HealthConnectAction.Refresh, state.secondaryAction?.action)
        assertNull(state.tertiaryAction)
    }

    @Test
    fun unsupportedMapsToUnsupportedState() {
        val state = HealthConnectSettingsUiMapper.from(
            availability = HealthConnectAvailability.Unsupported,
            permissionStatus = null,
            requiredPermissions = requiredPermissions,
        )

        assertEquals(requiredPermissions, state.requiredPermissions)
        assertEquals("Unsupported", state.item("Status").status)
        assertEquals("Unavailable", state.item("Permissions").status)
        assertFalse(state.item("Status").enabled)
        assertFalse(state.item("Permissions").enabled)
        assertFalse(state.item("Scope").enabled)
        assertEquals(HealthConnectAction.Refresh, state.primaryAction?.action)
        assertNull(state.secondaryAction)
        assertNull(state.tertiaryAction)
    }

    @Test
    fun errorReturnsSafeUserFacingCopy() {
        val state = HealthConnectSettingsUiMapper.error(requiredPermissions)

        assertEquals(requiredPermissions, state.requiredPermissions)
        assertEquals("Health Connect status could not be checked right now.", state.description)
        assertEquals("Health Connect status could not be checked right now.", state.statusMessage)
        assertEquals("Error", state.item("Status").status)
        assertEquals("Not checked", state.item("Permissions").status)
        assertEquals(HealthConnectAction.Refresh, state.primaryAction?.action)
        assertFalse(state.description.contains("Exception"))
        assertFalse(state.statusMessage.contains("Exception"))
    }

    @Test
    fun withExternalActionErrorReturnsSafeUserFacingCopy() {
        val current = HealthConnectSettingsUiState(
            description = "Current Health Connect state",
            statusMessage = "SecurityException: denied by provider",
            requiredPermissions = requiredPermissions,
            items = emptyList(),
            primaryAction = HealthConnectActionUiState(
                action = HealthConnectAction.InstallOrUpdate,
                text = "Install or update",
            ),
        )

        val state = HealthConnectSettingsUiMapper.withExternalActionError(current)

        assertEquals("Health Connect could not be opened right now.", state.statusMessage)
        assertFalse(state.statusMessage.contains("SecurityException"))
        assertEquals(current.description, state.description)
        assertEquals(current.requiredPermissions, state.requiredPermissions)
        assertEquals(current.primaryAction, state.primaryAction)
    }

    @Test
    fun syncingStateDisablesActions() {
        val state = HealthConnectSettingsUiMapper.syncing(requiredPermissions)

        assertEquals(requiredPermissions, state.requiredPermissions)
        assertEquals("Syncing", state.item("Status").status)
        assertEquals("Syncing Health Connect weight and sleep records...", state.statusMessage)
        assertEquals(HealthConnectAction.SyncWeightAndSleep, state.primaryAction?.action)
        assertFalse(state.primaryAction?.enabled ?: true)
        assertTrue(state.isSyncing)
    }

    @Test
    fun successOutcomeShowsCountsAndLastSync() {
        val state = HealthConnectSettingsUiMapper.fromSyncOutcome(
            outcome = HealthConnectSyncOutcome.Success(
                HealthConnectSyncResult(
                    weightInserted = 1,
                    weightUpdated = 2,
                    sleepInserted = 3,
                    sleepUpdated = 4,
                    skipped = 1,
                    startedAtMillis = 1L,
                    finishedAtMillis = 2L,
                ),
            ),
            requiredPermissions = requiredPermissions,
        )

        assertEquals("Ready", state.item("Status").status)
        assertEquals("Synced", state.item("Last sync").status)
        assertEquals(
            "Weight: 1 inserted, 2 updated. Sleep: 3 inserted, 4 updated. Skipped: 1.",
            state.item("Last sync").description,
        )
        assertTrue(state.statusMessage.startsWith("Last sync: just now."))
        assertEquals(HealthConnectAction.SyncWeightAndSleep, state.primaryAction?.action)
    }

    @Test
    fun noDataOutcomeShowsNoDataCopy() {
        val state = HealthConnectSettingsUiMapper.fromSyncOutcome(
            outcome = HealthConnectSyncOutcome.NoData(
                HealthConnectSyncResult(
                    weightInserted = 0,
                    weightUpdated = 0,
                    sleepInserted = 0,
                    sleepUpdated = 0,
                    skipped = 0,
                    startedAtMillis = 1L,
                    finishedAtMillis = 2L,
                ),
            ),
            requiredPermissions = requiredPermissions,
        )

        assertEquals("No new Health Connect weight or sleep records were found.", state.statusMessage)
        assertEquals("No data", state.item("Last sync").status)
        assertEquals(HealthConnectAction.SyncWeightAndSleep, state.primaryAction?.action)
    }

    @Test
    fun failedOutcomeShowsSafeErrorCopy() {
        val state = HealthConnectSettingsUiMapper.fromSyncOutcome(
            outcome = HealthConnectSyncOutcome.Failed,
            requiredPermissions = requiredPermissions,
        )

        assertEquals("Health Connect sync could not be completed right now.", state.statusMessage)
        assertEquals("Error", state.item("Last sync").status)
        assertFalse(state.statusMessage.contains("Exception"))
        assertFalse(state.item("Last sync").description.contains("Exception"))
    }

    @Test
    fun syncProviderAndUnsupportedOutcomesPreserveGracefulStates() {
        val providerState = HealthConnectSettingsUiMapper.fromSyncOutcome(
            outcome = HealthConnectSyncOutcome.ProviderUpdateRequired,
            requiredPermissions = requiredPermissions,
        )
        val unsupportedState = HealthConnectSettingsUiMapper.fromSyncOutcome(
            outcome = HealthConnectSyncOutcome.Unsupported,
            requiredPermissions = requiredPermissions,
        )

        assertEquals("Update needed", providerState.item("Status").status)
        assertEquals(HealthConnectAction.InstallOrUpdate, providerState.primaryAction?.action)
        assertEquals("Unsupported", unsupportedState.item("Status").status)
        assertEquals(HealthConnectAction.Refresh, unsupportedState.primaryAction?.action)
    }

    private fun HealthConnectSettingsUiState.item(title: String): SettingsItemUiState =
        items.first { it.title == title }
}
