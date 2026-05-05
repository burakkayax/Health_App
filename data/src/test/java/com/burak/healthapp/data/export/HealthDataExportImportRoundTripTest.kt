package com.burak.healthapp.data.export

import com.burak.healthapp.domain.export.ExportedCustomFood
import com.burak.healthapp.domain.export.ExportedGoalSettings
import com.burak.healthapp.domain.export.ExportedUserProfile
import com.burak.healthapp.domain.export.ExportedWaterReminderSettings
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.ImportValidationResult
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthDataExportImportRoundTripTest {
    @Test
    fun exportImportRoundTrip_customFoodOptionalNutrientsStable() {
        val encoded = JsonHealthDataExporter().encode(modelWithCustomFood())
        val result = JsonHealthDataImporter().validate(encoded) as ImportValidationResult.Valid

        val food = result.model.customFoods.single()
        assertEquals(5.5f, food.fiberGrams)
        assertEquals(2f, food.sugarGrams)
        assertEquals(120f, food.sodiumMg)
    }

    @Test
    fun exportImportRoundTrip_schemaVersionCurrentStable() {
        val encoded = JsonHealthDataExporter().encode(modelWithCustomFood())
        val result = JsonHealthDataImporter().validate(encoded) as ImportValidationResult.Valid

        assertEquals(HealthDataExportModel.SCHEMA_VERSION, result.model.schemaVersion)
        assertEquals(1, result.preview.customFoodsCount)
    }

    private fun modelWithCustomFood(): HealthDataExportModel {
        val goals = GoalSettings()
        return HealthDataExportModel(
            exportedAt = "2026-04-27T10:15:30Z",
            appVersion = "1.0-test",
            profile = ExportedUserProfile(
                name = "Ada",
                avatarInitials = "A",
                heightCm = 170f,
            ),
            goals = ExportedGoalSettings(
                dailyCaloriesTarget = goals.dailyCaloriesTarget,
                proteinTargetGrams = goals.proteinTargetGrams,
                carbTargetGrams = goals.carbTargetGrams,
                fatTargetGrams = goals.fatTargetGrams,
                waterTargetMl = goals.waterTargetMl,
                dailyStepTarget = goals.dailyStepTarget,
                dailyCaffeineLimitMg = goals.dailyCaffeineLimitMg,
                caffeineCutoffTime = goals.caffeineCutoffTime.toString(),
                caffeineSleepBufferHours = goals.caffeineSleepBufferHours,
                sleepTargetBedtime = goals.sleepTargetBedtime.toString(),
                sleepTargetWakeTime = goals.sleepTargetWakeTime.toString(),
                exerciseTargetDaysPerWeek = goals.exerciseTargetDaysPerWeek,
                exerciseTargetDurationMinutes = goals.exerciseTargetDurationMinutes,
                smokeDailyLimit = goals.smokeDailyLimit,
                baselineWeightKg = goals.baselineWeightKg,
                targetWeightKg = goals.targetWeightKg,
                baselineShoulderCm = goals.baselineShoulderCm,
                baselineWaistCm = goals.baselineWaistCm,
                baselineHipCm = goals.baselineHipCm,
            ),
            waterReminderSettings = ExportedWaterReminderSettings(
                enabled = true,
                startTime = "09:00",
                endTime = "21:00",
                intervalMinutes = 60,
            ),
            themeMode = ThemeMode.DARK.name,
            customFoods = listOf(
                ExportedCustomFood(
                    id = 1,
                    name = "Yulaf",
                    brand = "Ev",
                    servingName = "kase",
                    servingGrams = 100f,
                    calories = 350,
                    proteinGrams = 10,
                    carbsGrams = 50,
                    fatGrams = 5,
                    fiberGrams = 5.5f,
                    sugarGrams = 2f,
                    sodiumMg = 120f,
                    isFavorite = true,
                    createdAt = "2026-04-27T10:00:00",
                    updatedAt = "2026-04-27T11:00:00",
                ),
            ),
        )
    }
}
