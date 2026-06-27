package com.saglik.domain.usecase

import com.saglik.core.model.HealthConnectAvailability
import com.saglik.domain.repository.HealthConnectRepository

class CheckHealthConnectAvailabilityUseCase(
    private val repository: HealthConnectRepository,
) {
    suspend operator fun invoke(): HealthConnectAvailability = repository.getAvailability()
}
