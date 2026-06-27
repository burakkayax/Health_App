package com.saglik.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HealthConnectAvailabilityMapperTest {
    @Test
    fun sdkAvailableMapsToAvailable() {
        assertEquals(
            HealthConnectAvailability.Available,
            HealthConnectAvailabilityMapper.fromSdkStatus(HealthConnectSdkStatus.SDK_AVAILABLE),
        )
    }

    @Test
    fun sdkProviderUpdateRequiredMapsToProviderUpdateRequired() {
        assertEquals(
            HealthConnectAvailability.ProviderUpdateRequired,
            HealthConnectAvailabilityMapper.fromSdkStatus(
                HealthConnectSdkStatus.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED,
            ),
        )
    }

    @Test
    fun sdkUnavailableMapsToUnsupported() {
        assertEquals(
            HealthConnectAvailability.Unsupported,
            HealthConnectAvailabilityMapper.fromSdkStatus(HealthConnectSdkStatus.SDK_UNAVAILABLE),
        )
    }
}
