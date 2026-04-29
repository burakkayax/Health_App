package com.burak.healthapp.domain.export

import kotlinx.serialization.Serializable

@Serializable
data class HealthDataExportModel(
    val schemaVersion: Int = SCHEMA_VERSION,
    val exportedAt: String,
    val appVersion: String,
    val profile: ExportedUserProfile,
    val goals: ExportedGoalSettings,
    val waterReminderSettings: ExportedWaterReminderSettings,
    val themeMode: String,
    val meals: List<ExportedMealEntry> = emptyList(),
    val hydration: List<ExportedHydrationEntry> = emptyList(),
    val sleep: List<ExportedSleepSession> = emptyList(),
    val exercise: List<ExportedExerciseEntry> = emptyList(),
    val smoking: List<ExportedSmokingEntry> = emptyList(),
    val steps: List<ExportedStepEntry> = emptyList(),
    val caffeineEntries: List<ExportedCaffeineEntry> = emptyList(),
    val bodyMeasurements: List<ExportedBodyMeasurementEntry> = emptyList(),
    val supplementTemplates: List<ExportedSupplementTemplate> = emptyList(),
    val supplementDoseEntries: List<ExportedSupplementDoseEntry> = emptyList(),
) {
    companion object {
        const val SCHEMA_VERSION = 2
    }
}

interface HealthDataJsonExporter {
    fun encode(model: HealthDataExportModel): String
}

@Serializable
data class ExportedUserProfile(
    val name: String,
    val avatarInitials: String,
    val heightCm: Float?,
)

@Serializable
data class ExportedGoalSettings(
    val dailyCaloriesTarget: Int,
    val proteinTargetGrams: Int,
    val carbTargetGrams: Int,
    val fatTargetGrams: Int,
    val waterTargetMl: Int,
    val dailyStepTarget: Int,
    val dailyCaffeineLimitMg: Int = 300,
    val caffeineCutoffTime: String = "15:00",
    val caffeineSleepBufferHours: Int = 6,
    val sleepTargetBedtime: String,
    val sleepTargetWakeTime: String,
    val exerciseTargetDaysPerWeek: Int,
    val exerciseTargetDurationMinutes: Int,
    val smokeDailyLimit: Int,
    val baselineWeightKg: Float,
    val targetWeightKg: Float,
    val baselineShoulderCm: Float,
    val baselineWaistCm: Float,
    val baselineHipCm: Float,
)

@Serializable
data class ExportedWaterReminderSettings(
    val enabled: Boolean,
    val startTime: String,
    val endTime: String,
    val intervalMinutes: Int,
)

@Serializable
data class ExportedMealEntry(
    val id: Long,
    val date: String,
    val mealType: String,
    val name: String,
    val calories: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinGrams: Int,
    val createdAt: String,
)

@Serializable
data class ExportedHydrationEntry(
    val id: Long,
    val date: String,
    val amountMl: Int,
    val createdAt: String,
)

@Serializable
data class ExportedSleepSession(
    val id: Long,
    val sessionDate: String,
    val startTime: String,
    val endTime: String,
)

@Serializable
data class ExportedExerciseEntry(
    val id: Long,
    val date: String,
    val type: String,
    val durationMinutes: Int,
    val intensity: String,
)

@Serializable
data class ExportedSmokingEntry(
    val id: Long,
    val date: String,
    val count: Int,
)

@Serializable
data class ExportedStepEntry(
    val id: Long,
    val date: String,
    val steps: Int,
    val sensorBaseline: Int?,
    val lastSensorValue: Int?,
    val updatedAt: String,
)

@Serializable
data class ExportedCaffeineEntry(
    val id: Long,
    val date: String,
    val time: String,
    val drinkType: String,
    val size: String,
    val estimatedMg: Int,
    val customName: String?,
    val createdAt: String,
)

@Serializable
data class ExportedBodyMeasurementEntry(
    val id: Long,
    val date: String,
    val weightKg: Float,
    val shoulderCm: Float,
    val waistCm: Float,
    val hipCm: Float,
    val recordedAt: String,
)

@Serializable
data class ExportedSupplementTemplate(
    val id: Long,
    val name: String,
    val targetAmount: Float,
    val unitLabel: String,
    val isActive: Boolean,
    val sortOrder: Int,
)

@Serializable
data class ExportedSupplementDoseEntry(
    val id: Long,
    val templateId: Long,
    val date: String,
    val amount: Float,
    val loggedAt: String,
)
