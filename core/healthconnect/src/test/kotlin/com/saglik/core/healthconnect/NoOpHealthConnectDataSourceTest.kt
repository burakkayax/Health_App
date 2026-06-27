package com.saglik.core.healthconnect

import com.saglik.core.model.HealthConnectAvailability
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoOpHealthConnectDataSourceTest {
    private val readWeight = "android.permission.health.READ_WEIGHT"
    private val readSleep = "android.permission.health.READ_SLEEP"
    private val readSteps = "android.permission.health.READ_STEPS"
    private val readExercise = "android.permission.health.READ_EXERCISE"
    private val requiredPermissions = setOf(readWeight, readSleep, readSteps, readExercise)

    @Test
    fun defaultAvailabilityIsUnsupported() = runBlocking {
        val dataSource = NoOpHealthConnectDataSource()

        assertEquals(HealthConnectAvailability.Unsupported, dataSource.getAvailability())
    }

    @Test
    fun requiredPermissionsAreExposed() {
        val dataSource = NoOpHealthConnectDataSource()

        assertEquals(HealthConnectPermissions.requiredPermissions, dataSource.getRequiredPermissions())
    }

    @Test
    fun emptyGrantedPermissionsProduceMissingRequiredPermissions() = runBlocking {
        val dataSource = NoOpHealthConnectDataSource(
            grantedPermissions = emptySet(),
            requiredPermissions = requiredPermissions,
        )

        val status = dataSource.getPermissionStatus()

        assertFalse(status.allRequiredGranted)
        assertEquals(requiredPermissions, status.missingPermissions)
    }

    @Test
    fun injectedGrantedPermissionsProduceAllRequiredGrantedWhenComplete() = runBlocking {
        val dataSource = NoOpHealthConnectDataSource(
            grantedPermissions = requiredPermissions,
            requiredPermissions = requiredPermissions,
        )

        val status = dataSource.getPermissionStatus()

        assertTrue(status.allRequiredGranted)
        assertEquals(emptySet<String>(), status.missingPermissions)
    }

    @Test
    fun readMethodsReturnEmptyLists() = runBlocking {
        val dataSource = NoOpHealthConnectDataSource()

        assertTrue(dataSource.readWeightRecords(0L, 1L).isEmpty())
        assertTrue(dataSource.readSleepSessionRecords(0L, 1L).isEmpty())
        assertTrue(dataSource.readStepsRecords(0L, 1L).isEmpty())
        assertTrue(dataSource.readExerciseSessionRecords(0L, 1L).isEmpty())
    }
}
