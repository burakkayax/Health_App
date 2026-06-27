package com.saglik.feature.profile

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertEquals("Health Connect is ready. Weight and sleep sync will be added in a later update.", state.description)
        assertEquals("Ready", state.item("Status").status)
        assertEquals("Granted", state.item("Permissions").status)
        assertEquals(HealthConnectAction.OpenSettings, state.primaryAction?.action)
        assertEquals(HealthConnectAction.Refresh, state.secondaryAction?.action)
        assertNull(state.tertiaryAction)
        assertFalse(state.isChecking)
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

    private fun HealthConnectSettingsUiState.item(title: String): SettingsItemUiState =
        items.first { it.title == title }
}
