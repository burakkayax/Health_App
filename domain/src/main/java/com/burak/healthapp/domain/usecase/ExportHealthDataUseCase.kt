package com.burak.healthapp.domain.usecase

import com.burak.healthapp.domain.export.HealthDataJsonExporter
import com.burak.healthapp.domain.repository.HealthDataExportRepository
import java.time.Clock
import java.time.Instant

class ExportHealthDataUseCase(
    private val repository: HealthDataExportRepository,
    private val jsonExporter: HealthDataJsonExporter,
    private val appVersion: String,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend fun exportJson(exportedAt: Instant = clock.instant()): String {
        val model = repository.buildExportModel(
            exportedAt = exportedAt,
            appVersion = appVersion.ifBlank { UNKNOWN_APP_VERSION },
        )
        return jsonExporter.encode(model)
    }

    private companion object {
        const val UNKNOWN_APP_VERSION = "unknown"
    }
}
