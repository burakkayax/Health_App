package com.saglik.core.healthconnect

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot

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

    override suspend fun readWeightRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectWeightRecordSnapshot> = emptyList()

    override suspend fun readSleepSessionRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectSleepSessionSnapshot> = emptyList()
}
