package com.saglik.core.model

object HealthConnectAvailabilityMapper {
    fun fromSdkStatus(sdkStatus: HealthConnectSdkStatus): HealthConnectAvailability =
        when (sdkStatus) {
            HealthConnectSdkStatus.SDK_AVAILABLE -> HealthConnectAvailability.Available
            HealthConnectSdkStatus.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                HealthConnectAvailability.ProviderUpdateRequired
            }
            HealthConnectSdkStatus.SDK_UNAVAILABLE -> HealthConnectAvailability.Unsupported
        }
}
