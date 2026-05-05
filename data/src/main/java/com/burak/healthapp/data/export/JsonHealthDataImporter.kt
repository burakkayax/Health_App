package com.burak.healthapp.data.export

import com.burak.healthapp.domain.export.ExportedBodyMeasurementEntry
import com.burak.healthapp.domain.export.ExportedCaffeineEntry
import com.burak.healthapp.domain.export.ExportedCustomFood
import com.burak.healthapp.domain.export.ExportedExerciseEntry
import com.burak.healthapp.domain.export.ExportedGoalSettings
import com.burak.healthapp.domain.export.ExportedHydrationEntry
import com.burak.healthapp.domain.export.ExportedMealEntry
import com.burak.healthapp.domain.export.ExportedSleepSession
import com.burak.healthapp.domain.export.ExportedSmokingEntry
import com.burak.healthapp.domain.export.ExportedStepEntry
import com.burak.healthapp.domain.export.ExportedSupplementDoseEntry
import com.burak.healthapp.domain.export.ExportedSupplementTemplate
import com.burak.healthapp.domain.export.ExportedWaterReminderSettings
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.HealthDataImportPreview
import com.burak.healthapp.domain.export.HealthDataJsonImporter
import com.burak.healthapp.domain.export.ImportValidationError
import com.burak.healthapp.domain.export.ImportValidationResult
import com.burak.healthapp.domain.model.CaffeineDrinkSize
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.ThemeMode
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

class JsonHealthDataImporter(
    private val json: Json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    },
) : HealthDataJsonImporter {
    override fun validate(json: String): ImportValidationResult {
        if (json.isBlank()) {
            return ImportValidationResult.Invalid(ImportValidationError.EmptyFile)
        }

        val root = try {
            this.json.parseToJsonElement(json)
        } catch (_: SerializationException) {
            return ImportValidationResult.Invalid(ImportValidationError.InvalidJson)
        } catch (_: IllegalArgumentException) {
            return ImportValidationResult.Invalid(ImportValidationError.InvalidJson)
        }

        val rootObject = root as? JsonObject
            ?: return ImportValidationResult.Invalid(ImportValidationError.InvalidJson)

        val schemaVersion = rootObject
            .get("schemaVersion")
            ?.jsonPrimitive
            ?.intOrNull
            ?: return ImportValidationResult.Invalid(ImportValidationError.MissingSchemaVersion)

        if (schemaVersion !in SUPPORTED_SCHEMA_VERSIONS) {
            return ImportValidationResult.Invalid(ImportValidationError.UnsupportedSchemaVersion(schemaVersion))
        }

        val model = try {
            this.json.decodeFromString<HealthDataExportModel>(json)
        } catch (exception: SerializationException) {
            return ImportValidationResult.Invalid(exception.toValidationError())
        } catch (_: IllegalArgumentException) {
            return ImportValidationResult.Invalid(ImportValidationError.DecodeFailure)
        }

        model.validateSemantics()?.let { error ->
            return ImportValidationResult.Invalid(error)
        }

        return ImportValidationResult.Valid(
            model = model,
            preview = HealthDataImportPreview.from(model),
        )
    }

    private companion object {
        val SUPPORTED_SCHEMA_VERSIONS = setOf(1, 2, HealthDataExportModel.SCHEMA_VERSION)
    }
}

private fun SerializationException.toValidationError(): ImportValidationError {
    val message = message.orEmpty()
    val missingField = MISSING_FIELD_REGEX.find(message)
        ?.groupValues
        ?.getOrNull(1)
    return if (missingField != null) {
        ImportValidationError.MissingRequiredField(missingField)
    } else {
        ImportValidationError.DecodeFailure
    }
}

private fun HealthDataExportModel.validateSemantics(): ImportValidationError? =
    validateInstant(exportedAt, "exportedAt")
        ?: validateProfile()
        ?: goals.validate()
        ?: waterReminderSettings.validate()
        ?: validateEnum<ThemeMode>(themeMode, "themeMode")
        ?: meals.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: hydration.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: sleep.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: exercise.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: smoking.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: steps.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: caffeineEntries.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: bodyMeasurements.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: supplementTemplates.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: supplementDoseEntries.firstErrorIndexed { index, entry -> entry.validate(index) }
        ?: customFoods.firstErrorIndexed { index, entry -> entry.validate(index) }

private fun HealthDataExportModel.validateProfile(): ImportValidationError? =
    profile.heightCm?.let { height ->
        validateNonNegative(height, "profile.heightCm")
    }

private fun ExportedGoalSettings.validate(): ImportValidationError? =
    validatePositive(dailyCaloriesTarget, "goals.dailyCaloriesTarget")
        ?: validateNonNegative(proteinTargetGrams, "goals.proteinTargetGrams")
        ?: validateNonNegative(carbTargetGrams, "goals.carbTargetGrams")
        ?: validateNonNegative(fatTargetGrams, "goals.fatTargetGrams")
        ?: validatePositive(waterTargetMl, "goals.waterTargetMl")
        ?: validateNonNegative(dailyStepTarget, "goals.dailyStepTarget")
        ?: validateNonNegative(dailyCaffeineLimitMg, "goals.dailyCaffeineLimitMg")
        ?: validateTime(caffeineCutoffTime, "goals.caffeineCutoffTime")
        ?: validateNonNegative(caffeineSleepBufferHours, "goals.caffeineSleepBufferHours")
        ?: validateTime(sleepTargetBedtime, "goals.sleepTargetBedtime")
        ?: validateTime(sleepTargetWakeTime, "goals.sleepTargetWakeTime")
        ?: validateRange(exerciseTargetDaysPerWeek, 0..7, "goals.exerciseTargetDaysPerWeek")
        ?: validateNonNegative(exerciseTargetDurationMinutes, "goals.exerciseTargetDurationMinutes")
        ?: validateNonNegative(smokeDailyLimit, "goals.smokeDailyLimit")
        ?: validatePositiveFloat(baselineWeightKg, "goals.baselineWeightKg")
        ?: validatePositiveFloat(targetWeightKg, "goals.targetWeightKg")
        ?: validateNonNegative(baselineShoulderCm, "goals.baselineShoulderCm")
        ?: validateNonNegative(baselineWaistCm, "goals.baselineWaistCm")
        ?: validateNonNegative(baselineHipCm, "goals.baselineHipCm")

private fun ExportedWaterReminderSettings.validate(): ImportValidationError? =
    validateTime(startTime, "waterReminderSettings.startTime")
        ?: validateTime(endTime, "waterReminderSettings.endTime")
        ?: validateRange(intervalMinutes, com.burak.healthapp.domain.config.DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES..Int.MAX_VALUE, "waterReminderSettings.intervalMinutes")

private fun ExportedMealEntry.validate(index: Int): ImportValidationError? =
    validateNotBlank(name, "meals[$index].name")
        ?: validateDate(date, "meals[$index].date")
        ?: validateEnum<MealType>(mealType, "meals[$index].mealType")
        ?: validateNonNegative(calories, "meals[$index].calories")
        ?: validateNonNegative(carbsGrams, "meals[$index].carbsGrams")
        ?: validateNonNegative(fatGrams, "meals[$index].fatGrams")
        ?: validateNonNegative(proteinGrams, "meals[$index].proteinGrams")
        ?: validateDateTime(createdAt, "meals[$index].createdAt")

private fun ExportedHydrationEntry.validate(index: Int): ImportValidationError? =
    validateDate(date, "hydration[$index].date")
        ?: validatePositive(amountMl, "hydration[$index].amountMl")
        ?: validateDateTime(createdAt, "hydration[$index].createdAt")

private fun ExportedSleepSession.validate(index: Int): ImportValidationError? {
    validateDate(sessionDate, "sleep[$index].sessionDate")?.let { return it }
    validateDateTime(startTime, "sleep[$index].startTime")?.let { return it }
    validateDateTime(endTime, "sleep[$index].endTime")?.let { return it }
    val start = java.time.LocalDateTime.parse(startTime)
    val end = java.time.LocalDateTime.parse(endTime)
    if (!end.isAfter(start)) {
        return ImportValidationError.NonPositiveValue("sleep[$index].duration")
    }
    if (java.time.Duration.between(start, end).toHours() > 24) {
        return ImportValidationError.InvalidRange("sleep[$index].duration")
    }
    return null
}

private fun ExportedExerciseEntry.validate(index: Int): ImportValidationError? =
    validateDate(date, "exercise[$index].date")
        ?: validateEnum<ExerciseType>(type, "exercise[$index].type")
        ?: validatePositive(durationMinutes, "exercise[$index].durationMinutes")
        ?: validateEnum<ExerciseIntensity>(intensity, "exercise[$index].intensity")

private fun ExportedSmokingEntry.validate(index: Int): ImportValidationError? =
    validateDate(date, "smoking[$index].date")
        ?: validateNonNegative(count, "smoking[$index].count")

private fun ExportedStepEntry.validate(index: Int): ImportValidationError? =
    validateDate(date, "steps[$index].date")
        ?: validateNonNegative(steps, "steps[$index].steps")
        ?: sensorBaseline?.let { validateNonNegative(it, "steps[$index].sensorBaseline") }
        ?: lastSensorValue?.let { validateNonNegative(it, "steps[$index].lastSensorValue") }
        ?: validateDateTime(updatedAt, "steps[$index].updatedAt")

private fun ExportedCaffeineEntry.validate(index: Int): ImportValidationError? =
    validateDate(date, "caffeineEntries[$index].date")
        ?: validateTime(time, "caffeineEntries[$index].time")
        ?: validateEnum<CaffeineDrinkType>(drinkType, "caffeineEntries[$index].drinkType")
        ?: validateEnum<CaffeineDrinkSize>(size, "caffeineEntries[$index].size")
        ?: validatePositive(estimatedMg, "caffeineEntries[$index].estimatedMg")
        ?: validateDateTime(createdAt, "caffeineEntries[$index].createdAt")

private fun ExportedBodyMeasurementEntry.validate(index: Int): ImportValidationError? =
    validateDate(date, "bodyMeasurements[$index].date")
        ?: validatePositiveFloat(weightKg, "bodyMeasurements[$index].weightKg")
        ?: validateNonNegative(shoulderCm, "bodyMeasurements[$index].shoulderCm")
        ?: validateNonNegative(waistCm, "bodyMeasurements[$index].waistCm")
        ?: validateNonNegative(hipCm, "bodyMeasurements[$index].hipCm")
        ?: validateDateTime(recordedAt, "bodyMeasurements[$index].recordedAt")

private fun ExportedSupplementTemplate.validate(index: Int): ImportValidationError? =
    validateNotBlank(name, "supplementTemplates[$index].name")
        ?: validateNotBlank(unitLabel, "supplementTemplates[$index].unitLabel")
        ?: validatePositiveFloat(targetAmount, "supplementTemplates[$index].targetAmount")
        ?: validateNonNegative(sortOrder, "supplementTemplates[$index].sortOrder")

private fun ExportedSupplementDoseEntry.validate(index: Int): ImportValidationError? =
    validatePositive(templateId, "supplementDoseEntries[$index].templateId")
        ?: validateDate(date, "supplementDoseEntries[$index].date")
        ?: validatePositiveFloat(amount, "supplementDoseEntries[$index].amount")
        ?: validateDateTime(loggedAt, "supplementDoseEntries[$index].loggedAt")

private fun ExportedCustomFood.validate(index: Int): ImportValidationError? =
    validateNotBlank(name, "customFoods[$index].name")
        ?: validateNotBlank(servingName, "customFoods[$index].servingName")
        ?: validatePositiveFloat(servingGrams, "customFoods[$index].servingGrams")
        ?: validateNonNegative(calories, "customFoods[$index].calories")
        ?: validateNonNegative(proteinGrams, "customFoods[$index].proteinGrams")
        ?: validateNonNegative(carbsGrams, "customFoods[$index].carbsGrams")
        ?: validateNonNegative(fatGrams, "customFoods[$index].fatGrams")
        ?: fiberGrams?.let { validateNonNegative(it, "customFoods[$index].fiberGrams") }
        ?: sugarGrams?.let { validateNonNegative(it, "customFoods[$index].sugarGrams") }
        ?: sodiumMg?.let { validateNonNegative(it, "customFoods[$index].sodiumMg") }
        ?: validateDateTime(createdAt, "customFoods[$index].createdAt")
        ?: validateDateTime(updatedAt, "customFoods[$index].updatedAt")

private inline fun <T> List<T>.firstErrorIndexed(
    validate: (Int, T) -> ImportValidationError?,
): ImportValidationError? {
    if (isEmpty()) return null
    forEachIndexed { index, item ->
        validate(index, item)?.let { return it }
    }
    return null
}

private inline fun <reified T : Enum<T>> validateEnum(value: String, fieldPath: String): ImportValidationError? =
    if (enumValues<T>().any { enum -> enum.name == value }) {
        null
    } else {
        ImportValidationError.InvalidEnum(fieldPath)
    }

private fun validateDate(value: String, fieldPath: String): ImportValidationError? =
    try {
        LocalDate.parse(value)
        null
    } catch (_: DateTimeParseException) {
        ImportValidationError.InvalidDate(fieldPath)
    }

private fun validateTime(value: String, fieldPath: String): ImportValidationError? =
    try {
        LocalTime.parse(value)
        null
    } catch (_: DateTimeParseException) {
        ImportValidationError.InvalidTime(fieldPath)
    }

private fun validateDateTime(value: String, fieldPath: String): ImportValidationError? =
    try {
        LocalDateTime.parse(value)
        null
    } catch (_: DateTimeParseException) {
        ImportValidationError.InvalidDateTime(fieldPath)
    }

private fun validateInstant(value: String, fieldPath: String): ImportValidationError? =
    try {
        Instant.parse(value)
        null
    } catch (_: DateTimeParseException) {
        ImportValidationError.InvalidDateTime(fieldPath)
    }

private fun validateNonNegative(value: Int, fieldPath: String): ImportValidationError? =
    if (value < 0) ImportValidationError.NegativeValue(fieldPath) else null

private fun validatePositive(value: Int, fieldPath: String): ImportValidationError? =
    if (value <= 0) ImportValidationError.NonPositiveValue(fieldPath) else null

private fun validatePositive(value: Long, fieldPath: String): ImportValidationError? =
    if (value <= 0L) ImportValidationError.NonPositiveValue(fieldPath) else null

private fun validateRange(value: Int, range: IntRange, fieldPath: String): ImportValidationError? =
    if (value !in range) ImportValidationError.InvalidRange(fieldPath) else null

private fun validateNonNegative(value: Long, fieldPath: String): ImportValidationError? =
    if (value < 0L) ImportValidationError.NegativeValue(fieldPath) else null

private fun validateNonNegative(value: Float, fieldPath: String): ImportValidationError? =
    when {
        !value.isFinite() -> ImportValidationError.InvalidNumber(fieldPath)
        value < 0f -> ImportValidationError.NegativeValue(fieldPath)
        else -> null
    }

private val MISSING_FIELD_REGEX = Regex("Field '([^']+)' is required")

private fun validateNotBlank(value: String, fieldPath: String): ImportValidationError? =
    if (value.isBlank()) ImportValidationError.MissingRequiredField(fieldPath) else null

private fun validatePositiveFloat(value: Float, fieldPath: String): ImportValidationError? =
    when {
        !value.isFinite() -> ImportValidationError.InvalidNumber(fieldPath)
        value <= 0f -> ImportValidationError.NonPositiveValue(fieldPath)
        else -> null
    }
