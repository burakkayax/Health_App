package com.saglik.domain.repository

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectPermissionStatus

interface HealthConnectRepository {
    fun getRequiredPermissions(): Set<String>

    suspend fun getAvailability(): HealthConnectAvailability

    suspend fun getPermissionStatus(): HealthConnectPermissionStatus
}
