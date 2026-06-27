package com.saglik.core.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.saglik.core.model.HealthConnectAvailability
import com.saglik.core.model.HealthConnectAvailabilityMapper
import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.core.model.HealthConnectSdkStatus

class RealHealthConnectDataSource(
    context: Context,
) : HealthConnectDataSource {
    private val appContext = context.applicationContext

    override fun getRequiredPermissions(): Set<String> = HealthConnectPermissions.requiredPermissions

    override suspend fun getAvailability(): HealthConnectAvailability =
        HealthConnectAvailabilityMapper.fromSdkStatus(
            HealthConnectClient.getSdkStatus(appContext).toHealthConnectSdkStatus(),
        )

    override suspend fun getPermissionStatus(): HealthConnectPermissionStatus {
        if (getAvailability() != HealthConnectAvailability.Available) {
            return HealthConnectPermissionStatus.from(
                requiredPermissions = getRequiredPermissions(),
                grantedPermissions = emptySet(),
            )
        }

        val grantedPermissions = HealthConnectClient
            .getOrCreate(appContext)
            .permissionController
            .getGrantedPermissions()

        return HealthConnectPermissionStatus.from(
            requiredPermissions = getRequiredPermissions(),
            grantedPermissions = grantedPermissions,
        )
    }

    private fun Int.toHealthConnectSdkStatus(): HealthConnectSdkStatus =
        when (this) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectSdkStatus.SDK_AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                HealthConnectSdkStatus.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED
            }
            else -> HealthConnectSdkStatus.SDK_UNAVAILABLE
        }
}
