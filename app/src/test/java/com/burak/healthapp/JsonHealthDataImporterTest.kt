package com.burak.healthapp

import com.burak.healthapp.data.export.JsonHealthDataExporter
import com.burak.healthapp.data.export.JsonHealthDataImporter
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.export.ExportedCaffeineEntry
import com.burak.healthapp.domain.export.ExportedCustomFood
import com.burak.healthapp.domain.export.ExportedGoalSettings
import com.burak.healthapp.domain.export.ExportedHydrationEntry
import com.burak.healthapp.domain.export.ExportedMealEntry
import com.burak.healthapp.domain.export.ExportedSleepSession
import com.burak.healthapp.domain.export.ExportedSupplementTemplate
import com.burak.healthapp.domain.export.ExportedUserProfile
import com.burak.healthapp.domain.export.ExportedWaterReminderSettings
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.ImportValidationError
import com.burak.healthapp.domain.export.ImportValidationResult
import com.burak.healthapp.domain.export.validateImportFileSize
import com.burak.healthapp.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonHealthDataImporterTest {
    private val importer = JsonHealthDataImporter()
    private val exporter = JsonHealthDataExporter()

    @Test
    fun validate_acceptsSupportedSchemaVersion() {
        val result = importer.validate(exporter.encode(sampleModel()))

        assertTrue(result is ImportValidationResult.Valid)
        assertEquals(HealthDataExportModel.SCHEMA_VERSION, (result as ImportValidationResult.Valid).model.schemaVersion)
    }

    @Test
    fun validate_rejectsMissingSchemaVersion() {
        val result = importer.validate("""{"exportedAt":"2026-04-27T10:00:00Z"}""")

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.MissingSchemaVersion),
            result,
        )
    }

    @Test
    fun validate_rejectsUnsupportedSchemaVersion() {
        val result = importer.validate("""{"schemaVersion":99}""")

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.UnsupportedSchemaVersion(99)),
            result,
        )
    }

    @Test
    fun validate_rejectsInvalidJson() {
        val result = importer.validate("{not-json")

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.InvalidJson),
            result,
        )
    }

    @Test
    fun validate_rejectsEmptyFile() {
        val result = importer.validate("   ")

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.EmptyFile),
            result,
        )
    }

    @Test
    fun validate_rejectsSleepDateTimeWithoutDate() {
        val model = sampleModel().copy(
            sleep = listOf(
                ExportedSleepSession(
                    id = 1,
                    sessionDate = "2026-04-27",
                    startTime = "22:30",
                    endTime = "07:00",
                ),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.InvalidDateTime("sleep[0].startTime")),
            result,
        )
    }

    @Test
    fun validate_rejectsInvalidDate() {
        val model = sampleModel().copy(
            hydration = listOf(
                ExportedHydrationEntry(
                    id = 1,
                    date = "2026-99-27",
                    amountMl = 250,
                    createdAt = "2026-04-27T09:00:00",
                ),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.InvalidDate("hydration[0].date")),
            result,
        )
    }

    @Test
    fun validate_rejectsNegativeHydration() {
        val model = sampleModel().copy(
            hydration = listOf(
                ExportedHydrationEntry(
                    id = 1,
                    date = "2026-04-27",
                    amountMl = -200,
                    createdAt = "2026-04-27T09:00:00",
                ),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("hydration[0].amountMl")),
            result,
        )
    }

    @Test
    fun validate_rejectsNegativeCaffeine() {
        val model = sampleModel().copy(
            caffeineEntries = listOf(
                ExportedCaffeineEntry(
                    id = 1,
                    date = "2026-04-27",
                    time = "10:30",
                    drinkType = "FILTER_COFFEE",
                    size = "MEDIUM",
                    estimatedMg = -80,
                    customName = null,
                    createdAt = "2026-04-27T10:30:00",
                ),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("caffeineEntries[0].estimatedMg")),
            result,
        )
    }

    @Test
    fun validate_rejectsInvalidEnum() {
        val model = sampleModel().copy(
            meals = listOf(
                ExportedMealEntry(
                    id = 1,
                    date = "2026-04-27",
                    mealType = "BRUNCH",
                    name = "Yulaf",
                    calories = 300,
                    carbsGrams = 40,
                    fatGrams = 8,
                    proteinGrams = 20,
                    createdAt = "2026-04-27T08:00:00",
                ),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.InvalidEnum("meals[0].mealType")),
            result,
        )
    }

    @Test
    fun validateImportFileSize_rejectsOversizedFile() {
        assertEquals(
            ImportValidationError.FileTooLarge(10),
            validateImportFileSize(sizeBytes = 11, maxBytes = 10),
        )
    }

    @Test
    fun validate_buildsPreviewCounts() {
        val result = importer.validate(exporter.encode(sampleModel()))

        val preview = (result as ImportValidationResult.Valid).preview
        assertEquals(1, preview.mealCount)
        assertEquals(1, preview.hydrationCount)
        assertEquals(1, preview.sleepCount)
        assertEquals(1, preview.supplementTemplateCount)
        assertEquals(0, preview.exerciseCount)
        assertEquals(0, preview.smokingCount)
        assertEquals(0, preview.stepCount)
        assertEquals(0, preview.caffeineCount)
        assertEquals(0, preview.bodyMeasurementCount)
        assertEquals(0, preview.supplementDoseCount)
        assertEquals(1, preview.customFoodsCount)
    }

    @Test
    fun validate_supportsSchemaVersion1WithoutCaffeineEntries() {
        val legacyJson = """
            {
              "schemaVersion": 1,
              "exportedAt": "2026-04-27T10:00:00Z",
              "appVersion": "1.0-test",
              "profile": { "name": "Burak", "avatarInitials": "BK", "heightCm": 182.0 },
              "goals": {
                "dailyCaloriesTarget": 2200,
                "proteinTargetGrams": 160,
                "carbTargetGrams": 220,
                "fatTargetGrams": 70,
                "waterTargetMl": 2500,
                "dailyStepTarget": 8000,
                "sleepTargetBedtime": "23:00",
                "sleepTargetWakeTime": "07:00",
                "exerciseTargetDaysPerWeek": 4,
                "exerciseTargetDurationMinutes": 45,
                "smokeDailyLimit": 0,
                "baselineWeightKg": 78.0,
                "targetWeightKg": 74.0,
                "baselineShoulderCm": 118.0,
                "baselineWaistCm": 88.0,
                "baselineHipCm": 99.0
              },
              "waterReminderSettings": {
                "enabled": true,
                "startTime": "09:00",
                "endTime": "21:00",
                "intervalMinutes": 60
              },
              "themeMode": "SYSTEM"
            }
        """.trimIndent()

        val result = importer.validate(legacyJson)

        assertTrue(result is ImportValidationResult.Valid)
        val model = (result as ImportValidationResult.Valid).model
        assertEquals(1, model.schemaVersion)
        assertTrue(model.caffeineEntries.isEmpty())
        assertTrue(model.customFoods.isEmpty())
    }

    @Test
    fun validate_supportsSchemaVersion2WithoutCustomFoods() {
        val legacyJson = """
            {
              "schemaVersion": 2,
              "exportedAt": "2026-04-27T10:00:00Z",
              "appVersion": "1.0-test",
              "profile": { "name": "Burak", "avatarInitials": "BK", "heightCm": 182.0 },
              "goals": {
                "dailyCaloriesTarget": 2200,
                "proteinTargetGrams": 160,
                "carbTargetGrams": 220,
                "fatTargetGrams": 70,
                "waterTargetMl": 2500,
                "dailyStepTarget": 8000,
                "sleepTargetBedtime": "23:00",
                "sleepTargetWakeTime": "07:00",
                "exerciseTargetDaysPerWeek": 4,
                "exerciseTargetDurationMinutes": 45,
                "smokeDailyLimit": 0,
                "baselineWeightKg": 78.0,
                "targetWeightKg": 74.0,
                "baselineShoulderCm": 118.0,
                "baselineWaistCm": 88.0,
                "baselineHipCm": 99.0
              },
              "waterReminderSettings": {
                "enabled": true,
                "startTime": "09:00",
                "endTime": "21:00",
                "intervalMinutes": 60
              },
              "themeMode": "SYSTEM"
            }
        """.trimIndent()

        val result = importer.validate(legacyJson)

        assertTrue(result is ImportValidationResult.Valid)
        val model = (result as ImportValidationResult.Valid).model
        assertEquals(2, model.schemaVersion)
        assertTrue(model.customFoods.isEmpty())
    }

    @Test
    fun validate_rejectsCustomFoodWithBlankName() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(name = " "),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.MissingRequiredField("customFoods[0].name")),
            result,
        )
    }

    @Test
    fun validate_rejectsCustomFoodWithNegativeCalories() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(calories = -10),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NegativeValue("customFoods[0].calories")),
            result,
        )
    }

    @Test
    fun validate_rejectsCustomFoodWithZeroServingGrams() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(servingGrams = 0f),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("customFoods[0].servingGrams")),
            result,
        )
    }

    @Test
    fun validate_rejectsCustomFoodWithNegativeProtein() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(proteinGrams = -5),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NegativeValue("customFoods[0].proteinGrams")),
            result,
        )
    }

    @Test
    fun validate_rejectsCustomFoodWithNegativeCarbs() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(carbsGrams = -5),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NegativeValue("customFoods[0].carbsGrams")),
            result,
        )
    }

    @Test
    fun validate_rejectsCustomFoodWithNegativeFat() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(fatGrams = -5),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NegativeValue("customFoods[0].fatGrams")),
            result,
        )
    }

    @Test
    fun validate_rejectsCustomFoodWithNegativeNullableFiber() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(fiberGrams = -5f),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NegativeValue("customFoods[0].fiberGrams")),
            result,
        )
    }

    @Test
    fun validate_rejectsCustomFoodWithInvalidCreatedAt() {
        val model = sampleModel().copy(
            customFoods = listOf(
                sampleCustomFood().copy(createdAt = "invalid-date"),
            ),
        )

        val result = importer.validate(exporter.encode(model))

        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.InvalidDateTime("customFoods[0].createdAt")),
            result,
        )
    }

    @Test
    fun validate_acceptsValidCustomFoods() {
        val model = sampleModel().copy(
            customFoods = listOf(sampleCustomFood()),
        )

        val result = importer.validate(exporter.encode(model))

        assertTrue(result is ImportValidationResult.Valid)
        assertEquals(1, (result as ImportValidationResult.Valid).preview.customFoodsCount)
    }

    @Test
    fun validate_rejectsWaterReminderIntervalBelowMinimum() {
        val model = sampleModel().copy(
            waterReminderSettings = sampleModel().waterReminderSettings.copy(intervalMinutes = 14),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.InvalidRange("waterReminderSettings.intervalMinutes")),
            result,
        )
    }

    @Test
    fun validate_rejectsZeroWaterTarget() {
        val model = sampleModel().copy(
            goals = sampleModel().goals.copy(waterTargetMl = 0),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("goals.waterTargetMl")),
            result,
        )
    }

    @Test
    fun validate_rejectsZeroHydrationAmount() {
        val model = sampleModel().copy(
            hydration = listOf(sampleModel().hydration.first().copy(amountMl = 0)),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("hydration[0].amountMl")),
            result,
        )
    }

    @Test
    fun validate_rejectsBlankMealName() {
        val model = sampleModel().copy(
            meals = listOf(sampleModel().meals.first().copy(name = "   ")),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.MissingRequiredField("meals[0].name")),
            result,
        )
    }

    @Test
    fun validate_rejectsZeroExerciseDuration() {
        val model = sampleModel().copy(
            exercise = listOf(com.burak.healthapp.domain.export.ExportedExerciseEntry(1, "2026-04-27", "RUN", 0, "HIGH")),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("exercise[0].durationMinutes")),
            result,
        )
    }

    @Test
    fun validate_rejectsZeroSleepDuration() {
        val model = sampleModel().copy(
            sleep = listOf(sampleModel().sleep.first().copy(startTime = "2026-04-27T07:00:00", endTime = "2026-04-27T07:00:00")),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("sleep[0].duration")),
            result,
        )
    }

    @Test
    fun validate_acceptsValidOvernightSleep() {
        val model = sampleModel().copy(
            sleep = listOf(sampleModel().sleep.first().copy(startTime = "2026-04-26T22:00:00", endTime = "2026-04-27T06:00:00")),
        )
        val result = importer.validate(exporter.encode(model))
        assertTrue(result is ImportValidationResult.Valid)
    }

    @Test
    fun validate_rejectsBlankSupplementTemplateName() {
        val model = sampleModel().copy(
            supplementTemplates = listOf(sampleModel().supplementTemplates.first().copy(name = "")),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.MissingRequiredField("supplementTemplates[0].name")),
            result,
        )
    }

    @Test
    fun validate_rejectsZeroSupplementDoseAmount() {
        val model = sampleModel().copy(
            supplementDoseEntries = listOf(com.burak.healthapp.domain.export.ExportedSupplementDoseEntry(1, 1, "2026-04-27", 0f, "2026-04-27T10:00:00")),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("supplementDoseEntries[0].amount")),
            result,
        )
    }

    @Test
    fun validate_rejectsBlankCustomFoodServingName() {
        val model = sampleModel().copy(
            customFoods = listOf(sampleCustomFood().copy(servingName = "  ")),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.MissingRequiredField("customFoods[0].servingName")),
            result,
        )
    }

    @Test
    fun validate_rejectsZeroCaffeineEstimatedMg() {
        val model = sampleModel().copy(
            caffeineEntries = listOf(com.burak.healthapp.domain.export.ExportedCaffeineEntry(1, "2026-04-27", "10:00", "BLACK_TEA", "SMALL", 0, null, "2026-04-27T10:00:00")),
        )
        val result = importer.validate(exporter.encode(model))
        assertEquals(
            ImportValidationResult.Invalid(ImportValidationError.NonPositiveValue("caffeineEntries[0].estimatedMg")),
            result,
        )
    }
}

private fun sampleModel(): HealthDataExportModel = HealthDataExportModel(
    exportedAt = "2026-04-27T10:00:00Z",
    appVersion = "1.0-test",
    profile = ExportedUserProfile("Burak", "BK", 182f),
    goals = ExportedGoalSettings(
        dailyCaloriesTarget = DefaultHealthGoals.DAILY_CALORIES,
        proteinTargetGrams = DefaultHealthGoals.PROTEIN_GRAMS,
        carbTargetGrams = DefaultHealthGoals.CARB_GRAMS,
        fatTargetGrams = DefaultHealthGoals.FAT_GRAMS,
        waterTargetMl = DefaultHealthGoals.WATER_TARGET_ML,
        dailyStepTarget = DefaultHealthGoals.DAILY_STEPS,
        sleepTargetBedtime = DefaultHealthGoals.SLEEP_BEDTIME.toString(),
        sleepTargetWakeTime = DefaultHealthGoals.SLEEP_WAKE_TIME.toString(),
        exerciseTargetDaysPerWeek = DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK,
        exerciseTargetDurationMinutes = DefaultHealthGoals.EXERCISE_DURATION_MINUTES,
        smokeDailyLimit = DefaultHealthGoals.SMOKE_DAILY_LIMIT,
        baselineWeightKg = DefaultHealthGoals.BASELINE_WEIGHT_KG,
        targetWeightKg = DefaultHealthGoals.TARGET_WEIGHT_KG,
        baselineShoulderCm = DefaultHealthGoals.BASELINE_SHOULDER_CM,
        baselineWaistCm = DefaultHealthGoals.BASELINE_WAIST_CM,
        baselineHipCm = DefaultHealthGoals.BASELINE_HIP_CM,
    ),
    waterReminderSettings = ExportedWaterReminderSettings(
        enabled = true,
        startTime = "09:00",
        endTime = "21:00",
        intervalMinutes = 60,
    ),
    themeMode = ThemeMode.SYSTEM.name,
    meals = listOf(
        ExportedMealEntry(
            id = 1,
            date = "2026-04-27",
            mealType = "BREAKFAST",
            name = "Yulaf",
            calories = 300,
            carbsGrams = 40,
            fatGrams = 8,
            proteinGrams = 20,
            createdAt = "2026-04-27T08:00:00",
        ),
    ),
    hydration = listOf(
        ExportedHydrationEntry(
            id = 1,
            date = "2026-04-27",
            amountMl = 250,
            createdAt = "2026-04-27T09:00:00",
        ),
    ),
    sleep = listOf(
        ExportedSleepSession(
            id = 1,
            sessionDate = "2026-04-27",
            startTime = "2026-04-26T23:00:00",
            endTime = "2026-04-27T07:00:00",
        ),
    ),
    supplementTemplates = listOf(
        ExportedSupplementTemplate(
            id = 10,
            name = "D3",
            targetAmount = 25f,
            unitLabel = "mcg",
            isActive = true,
            sortOrder = 0,
        ),
    ),
    customFoods = listOf(sampleCustomFood()),
)

private fun sampleCustomFood(): ExportedCustomFood = ExportedCustomFood(
    id = 1,
    name = "Yulaf ezmesi",
    brand = "Bünting",
    servingName = "porsiyon",
    servingGrams = 40f,
    calories = 150,
    proteinGrams = 5,
    carbsGrams = 27,
    fatGrams = 3,
    isFavorite = true,
    createdAt = "2026-04-27T08:00:00",
    updatedAt = "2026-04-27T08:00:00",
)
