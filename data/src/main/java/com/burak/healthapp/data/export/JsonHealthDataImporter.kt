package com.burak.healthapp.data.export

import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.HealthDataImportPreview
import com.burak.healthapp.domain.export.HealthDataJsonImporter
import com.burak.healthapp.domain.export.ImportValidationError
import com.burak.healthapp.domain.export.ImportValidationResult
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

class JsonHealthDataImporter(
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    },
) : HealthDataJsonImporter {
    override fun validate(json: String): ImportValidationResult {
        if (json.isBlank()) {
            return ImportValidationResult.Invalid(ImportValidationError.EMPTY_FILE)
        }

        val root = try {
            this.json.parseToJsonElement(json)
        } catch (_: SerializationException) {
            return ImportValidationResult.Invalid(ImportValidationError.INVALID_JSON)
        } catch (_: IllegalArgumentException) {
            return ImportValidationResult.Invalid(ImportValidationError.INVALID_JSON)
        }

        val schemaVersion = (root as? JsonObject)
            ?.get("schemaVersion")
            ?.jsonPrimitive
            ?.intOrNull
            ?: return ImportValidationResult.Invalid(ImportValidationError.MISSING_SCHEMA_VERSION)

        if (schemaVersion !in SUPPORTED_SCHEMA_VERSIONS) {
            return ImportValidationResult.Invalid(ImportValidationError.UNSUPPORTED_SCHEMA_VERSION)
        }

        val model = try {
            this.json.decodeFromString<HealthDataExportModel>(json)
        } catch (_: SerializationException) {
            return ImportValidationResult.Invalid(ImportValidationError.INVALID_JSON)
        } catch (_: IllegalArgumentException) {
            return ImportValidationResult.Invalid(ImportValidationError.INVALID_JSON)
        }

        return ImportValidationResult.Valid(
            model = model,
            preview = HealthDataImportPreview.from(model),
        )
    }

    private companion object {
        val SUPPORTED_SCHEMA_VERSIONS = setOf(1, HealthDataExportModel.SCHEMA_VERSION)
    }
}
