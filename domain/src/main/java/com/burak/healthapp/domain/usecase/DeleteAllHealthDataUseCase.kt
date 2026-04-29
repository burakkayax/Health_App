package com.burak.healthapp.domain.usecase

import com.burak.healthapp.domain.repository.HealthDataManagementRepository

class DeleteAllHealthDataUseCase(
    private val repository: HealthDataManagementRepository,
) {
    suspend fun deleteAllHealthData() {
        repository.deleteAllHealthData()
    }
}
