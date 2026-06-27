package com.saglik.domain.usecase

import com.saglik.domain.repository.HealthConnectRepository

class GetHealthConnectRequiredPermissionsUseCase(
    private val repository: HealthConnectRepository,
) {
    operator fun invoke(): Set<String> = repository.getRequiredPermissions()
}
