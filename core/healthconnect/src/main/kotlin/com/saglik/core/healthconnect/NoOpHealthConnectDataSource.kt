package com.saglik.core.healthconnect

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus

class NoOpHealthConnectDataSource(
    private val availability: HealthConnectAvailability = HealthConnectAvailability.Unsupported,
    private val grantedPermissions: Set<String> = emptySet(),
    private val requiredPermissions: Set<String> = HealthConnectPermissions.requiredPermissions,
) : HealthConnectDataSource {

    override fun getRequiredPermissions(): Set<String> = requiredPermissions

    override suspend fun getAvailability(): HealthConnectAvailability = availability

    override suspend fun getPermissionStatus(): HealthConnectPermissionStatus =
        HealthConnectPermissionStatus.from(
            requiredPermissions = getRequiredPermissions(),
            grantedPermissions = grantedPermissions,
        )
}
