package com.saglik.core.model

sealed interface HealthConnectAvailability {
    data object Available : HealthConnectAvailability
    data object ProviderUpdateRequired : HealthConnectAvailability
    data object Unsupported : HealthConnectAvailability
}
