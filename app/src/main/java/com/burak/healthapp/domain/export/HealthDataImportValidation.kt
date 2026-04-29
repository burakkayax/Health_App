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

enum class ImportValidationError {
    EMPTY_FILE,
    INVALID_JSON,
    MISSING_SCHEMA_VERSION,
    UNSUPPORTED_SCHEMA_VERSION,
}

interface HealthDataJsonImporter {
    fun validate(json: String): ImportValidationResult
}
