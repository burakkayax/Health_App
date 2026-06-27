package com.saglik.domain.usecase

import com.saglik.core.model.HealthConnectPermissionStatus
import com.saglik.domain.repository.HealthConnectRepository

class GetHealthConnectPermissionStatusUseCase(
    private val repository: HealthConnectRepository,
) {
    suspend operator fun invoke(): HealthConnectPermissionStatus =
        repository.getPermissionStatus()
}
