package com.burak.healthapp.domain.export

data class HealthDataImportPreview(
    val mealCount: Int,
    val hydrationCount: Int,
    val sleepCount: Int,
    val exerciseCount: Int,
    val smokingCount: Int,
    val stepCount: Int,
    val caffeineCount: Int,
    val bodyMeasurementCount: Int,
    val supplementTemplateCount: Int,
    val supplementDoseCount: Int,
    val customFoodsCount: Int = 0,
) {
    companion object {
        fun from(model: HealthDataExportModel): HealthDataImportPreview = HealthDataImportPreview(
            mealCount = model.meals.size,
            hydrationCount = model.hydration.size,
            sleepCount = model.sleep.size,
            exerciseCount = model.exercise.size,
            smokingCount = model.smoking.size,
            stepCount = model.steps.size,
            caffeineCount = model.caffeineEntries.size,
            bodyMeasurementCount = model.bodyMeasurements.size,
            supplementTemplateCount = model.supplementTemplates.size,
            supplementDoseCount = model.supplementDoseEntries.size,
            customFoodsCount = model.customFoods.size,
        )
    }
}

sealed interface ImportValidationResult {
    data class Valid(
        val model: HealthDataExportModel,
        val preview: HealthDataImportPreview,
    ) : ImportValidationResult

    data class Invalid(
        val error: ImportValidationError,
    ) : ImportValidationResult
}

sealed interface ImportValidationError {
    data object EmptyFile : ImportValidationError
    data object InvalidJson : ImportValidationError
    data object MissingSchemaVersion : ImportValidationError
    data class UnsupportedSchemaVersion(val schemaVersion: Int? = null) : ImportValidationError
    data class MissingRequiredField(val fieldPath: String) : ImportValidationError
    data class InvalidDate(val fieldPath: String) : ImportValidationError
    data class InvalidTime(val fieldPath: String) : ImportValidationError
    data class InvalidDateTime(val fieldPath: String) : ImportValidationError
    data class InvalidEnum(val fieldPath: String) : ImportValidationError
    data class InvalidNumber(val fieldPath: String) : ImportValidationError
    data class NegativeValue(val fieldPath: String) : ImportValidationError
    data class NonPositiveValue(val fieldPath: String) : ImportValidationError
    data class InvalidRange(val fieldPath: String) : ImportValidationError
    data class FileTooLarge(val limitBytes: Long) : ImportValidationError
    data object DecodeFailure : ImportValidationError
    data object DatabaseFailure : ImportValidationError
    data object SettingsFailure : ImportValidationError
    data object PartialSettingsFailure : ImportValidationError
    data object Unknown : ImportValidationError
}

class HealthDataImportException(
    val error: ImportValidationError,
    cause: Throwable? = null,
) : RuntimeException(cause)

typealias HealthDataImportError = ImportValidationError

object HealthDataImportLimits {
    const val MAX_IMPORT_FILE_BYTES: Long = 5L * 1024L * 1024L
}

fun validateImportFileSize(
    sizeBytes: Long?,
    maxBytes: Long = HealthDataImportLimits.MAX_IMPORT_FILE_BYTES,
): ImportValidationError? = if (sizeBytes != null && sizeBytes > maxBytes) {
    ImportValidationError.FileTooLarge(maxBytes)
} else {
    null
}

interface HealthDataJsonImporter {
    fun validate(json: String): ImportValidationResult
}
