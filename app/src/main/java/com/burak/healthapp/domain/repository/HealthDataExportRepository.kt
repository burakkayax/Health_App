package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.export.HealthDataExportModel
import java.time.Instant

interface HealthDataExportRepository {
    suspend fun buildExportModel(
        exportedAt: Instant,
        appVersion: String,
    ): HealthDataExportModel
}
