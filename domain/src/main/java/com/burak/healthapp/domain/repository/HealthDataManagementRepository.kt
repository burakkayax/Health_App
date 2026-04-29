package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.export.HealthDataExportModel

interface HealthDataManagementRepository {
    suspend fun importHealthData(model: HealthDataExportModel)

    suspend fun deleteAllHealthData()
}
