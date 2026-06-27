package com.saglik.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthConnectPermissionStatusTest {
    private val readWeight = "android.permission.health.READ_WEIGHT"
    private val readSleep = "android.permission.health.READ_SLEEP"
    private val readSteps = "android.permission.health.READ_STEPS"
    private val requiredPermissions = setOf(readWeight, readSleep, readSteps)

    @Test
    fun allRequiredPermissionsGranted() {
        val status = HealthConnectPermissionStatus.from(
            requiredPermissions = requiredPermissions,
            grantedPermissions = requiredPermissions,
        )

        assertTrue(status.allRequiredGranted)
        assertEquals(emptySet<String>(), status.missingPermissions)
    }

    @Test
    fun missingOneRequiredPermission() {
        val status = HealthConnectPermissionStatus.from(
            requiredPermissions = requiredPermissions,
            grantedPermissions = setOf(readWeight, readSleep),
        )

        assertFalse(status.allRequiredGranted)
        assertEquals(setOf(readSteps), status.missingPermissions)
    }

    @Test
    fun missingAllRequiredPermissions() {
        val status = HealthConnectPermissionStatus.from(
            requiredPermissions = requiredPermissions,
            grantedPermissions = emptySet(),
        )

        assertFalse(status.allRequiredGranted)
        assertEquals(requiredPermissions, status.missingPermissions)
    }
}
