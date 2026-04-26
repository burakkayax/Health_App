package com.burak.healthapp.data.export

import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.HealthDataJsonExporter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonHealthDataExporter(
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    },
) : HealthDataJsonExporter {
    override fun encode(model: HealthDataExportModel): String {
        return json.encodeToString(model)
    }
}
