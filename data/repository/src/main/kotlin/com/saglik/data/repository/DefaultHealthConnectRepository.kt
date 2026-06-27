package com.saglik.data.repository

import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.domain.repository.HealthConnectRepository

class DefaultHealthConnectRepository(
    private val dataSource: HealthConnectDataSource,
) : HealthConnectRepository {
    override fun getRequiredPermissions(): Set<String> = dataSource.getRequiredPermissions()

    override suspend fun getAvailability(): HealthConnectAvailability =
        dataSource.getAvailability()

    override suspend fun getPermissionStatus(): HealthConnectPermissionStatus =
        dataSource.getPermissionStatus()
}
