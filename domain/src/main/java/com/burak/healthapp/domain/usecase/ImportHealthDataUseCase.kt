package com.burak.healthapp.domain.usecase

import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.repository.HealthDataManagementRepository

class ImportHealthDataUseCase(
    private val repository: HealthDataManagementRepository,
) {
    suspend fun import(model: HealthDataExportModel) {
        repository.importHealthData(model)
    }
}
