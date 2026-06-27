package com.saglik.domain.usecase

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.domain.repository.HealthConnectRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HealthConnectUseCasesTest {
    private val readWeight = "android.permission.health.READ_WEIGHT"
    private val readSleep = "android.permission.health.READ_SLEEP"
    private val requiredPermissions = setOf(readWeight, readSleep)

    @Test
    fun fakeRepositoryReportsUnsupportedAndMissingPermissions() = runBlocking {
        val repository = FakeHealthConnectRepository(
            requiredPermissions = requiredPermissions,
            availability = HealthConnectAvailability.Unsupported,
            grantedPermissions = emptySet(),
        )

        assertEquals(
            HealthConnectAvailability.Unsupported,
            CheckHealthConnectAvailabilityUseCase(repository)(),
        )

        val status = GetHealthConnectPermissionStatusUseCase(repository)()

        assertFalse(status.allRequiredGranted)
        assertEquals(requiredPermissions, status.requiredPermissions)
        assertEquals(requiredPermissions, status.missingPermissions)
    }

    private class FakeHealthConnectRepository(
        private val requiredPermissions: Set<String>,
        private val availability: HealthConnectAvailability,
        private val grantedPermissions: Set<String>,
    ) : HealthConnectRepository {
        override fun getRequiredPermissions(): Set<String> = requiredPermissions

        override suspend fun getAvailability(): HealthConnectAvailability = availability

        override suspend fun getPermissionStatus(): HealthConnectPermissionStatus =
            HealthConnectPermissionStatus.from(
                requiredPermissions = requiredPermissions,
                grantedPermissions = grantedPermissions,
            )
    }
}
