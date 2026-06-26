package com.saglik.core.healthconnect

import kotlinx.coroutines.flow.StateFlow

interface HealthConnectDataSource {
    val availability: StateFlow<HealthConnectAvailability>

    suspend fun hasPermissions(permissionTypes: Set<HealthPermissionType>): Boolean
}
