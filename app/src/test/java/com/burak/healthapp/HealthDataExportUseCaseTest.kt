package com.burak.healthapp

import com.burak.healthapp.data.export.JsonHealthDataExporter
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.export.ExportedGoalSettings
import com.burak.healthapp.domain.export.ExportedUserProfile
import com.burak.healthapp.domain.export.ExportedWaterReminderSettings
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.repository.HealthDataExportRepository
import com.burak.healthapp.domain.usecase.ExportHealthDataUseCase
import java.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class HealthDataExportUseCaseTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val fixedInstant = Instant.parse("2026-04-27T10:15:30Z")

    @Test
    fun exportJson_containsSchemaVersionAndExportedAt() = runTest {
        val exported = decodeExport(emptyExportUseCase().exportJson(fixedInstant))

        assertEquals(HealthDataExportModel.SCHEMA_VERSION, exported.schemaVersion)
        assertEquals("2026-04-27T10:15:30Z", exported.exportedAt)
        assertFalse(exported.exportedAt.isBlank())
    }

    @Test
    fun exportJson_withEmptyDatabaseProducesParseableJsonWithEmptyArrays() = runTest {
        val exported = decodeExport(emptyExportUseCase().exportJson(fixedInstant))

        assertEquals(emptyList<Any>(), exported.meals)
        assertEquals(emptyList<Any>(), exported.hydration)
        assertEquals(emptyList<Any>(), exported.sleep)
        assertEquals(emptyList<Any>(), exported.exercise)
        assertEquals(emptyList<Any>(), exported.smoking)
        assertEquals(emptyList<Any>(), exported.steps)
        assertEquals(emptyList<Any>(), exported.bodyMeasurements)
        assertEquals(emptyList<Any>(), exported.supplementTemplates)
        assertEquals(emptyList<Any>(), exported.supplementDoseEntries)
    }

    @Test
    fun exportJson_exportsSettingsFields() = runTest {
        val exported = decodeExport(emptyExportUseCase().exportJson(fixedInstant))

        assertEquals("Burak", exported.profile.name)
        assertEquals("BK", exported.profile.avatarInitials)
        assertEquals(182f, exported.profile.heightCm)
        assertEquals(DefaultHealthGoals.DAILY_STEPS, exported.goals.dailyStepTarget)
        assertEquals(DefaultHealthGoals.WATER_TARGET_ML, exported.goals.waterTargetMl)
        assertEquals(true, exported.waterReminderSettings.enabled)
        assertEquals("09:00", exported.waterReminderSettings.startTime)
        assertEquals("21:00", exported.waterReminderSettings.endTime)
        assertEquals(60, exported.waterReminderSettings.intervalMinutes)
        assertEquals(ThemeMode.SYSTEM.name, exported.themeMode)
        assertEquals("1.0-test", exported.appVersion)
    }

    private fun emptyExportUseCase(): ExportHealthDataUseCase {
        return ExportHealthDataUseCase(
            repository = FakeHealthDataExportRepository(emptyExportModel()),
            jsonExporter = JsonHealthDataExporter(),
            appVersion = "1.0-test",
        )
    }

    private fun decodeExport(jsonString: String): HealthDataExportModel {
        return json.decodeFromString(jsonString)
    }
}

private class FakeHealthDataExportRepository(
    private val model: HealthDataExportModel,
) : HealthDataExportRepository {
    override suspend fun buildExportModel(
        exportedAt: Instant,
        appVersion: String,
    ): HealthDataExportModel {
        return model.copy(
            exportedAt = exportedAt.toString(),
            appVersion = appVersion,
        )
    }
}

private fun emptyExportModel(): HealthDataExportModel {
    return HealthDataExportModel(
        exportedAt = "",
        appVersion = "",
        profile = ExportedUserProfile(
            name = "Burak",
            avatarInitials = "BK",
            heightCm = 182f,
        ),
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
            intervalMinutes = DefaultHealthGoals.WATER_REMINDER_INTERVAL_MINUTES,
        ),
        themeMode = ThemeMode.SYSTEM.name,
    )
}
