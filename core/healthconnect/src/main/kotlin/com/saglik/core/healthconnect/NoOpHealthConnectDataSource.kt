package com.saglik.core.healthconnect

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoOpHealthConnectDataSource : HealthConnectDataSource {
    private val availabilityState = MutableStateFlow(HealthConnectAvailability.UNKNOWN)

    override val availability: StateFlow<HealthConnectAvailability> =
        availabilityState.asStateFlow()

    override suspend fun hasPermissions(permissionTypes: Set<HealthPermissionType>): Boolean = false
}
