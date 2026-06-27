package com.saglik.core.healthconnect

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot

interface HealthConnectDataSource {
    fun getRequiredPermissions(): Set<String>

    suspend fun getAvailability(): HealthConnectAvailability

    suspend fun getPermissionStatus(): HealthConnectPermissionStatus

    suspend fun readWeightRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectWeightRecordSnapshot>

    suspend fun readSleepSessionRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectSleepSessionSnapshot>
}
