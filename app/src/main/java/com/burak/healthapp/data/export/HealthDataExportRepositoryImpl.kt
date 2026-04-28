package com.burak.healthapp.data.export

import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.ExerciseDao
import com.burak.healthapp.data.local.dao.HydrationDao
import com.burak.healthapp.data.local.dao.MealDao
import com.burak.healthapp.data.local.dao.SleepDao
import com.burak.healthapp.data.local.dao.SmokingDao
import com.burak.healthapp.data.local.dao.StepDao
import com.burak.healthapp.data.local.dao.SupplementDoseDao
import com.burak.healthapp.data.local.dao.SupplementTemplateDao
import com.burak.healthapp.data.local.mapper.toDomain
import com.burak.healthapp.domain.export.ExportedBodyMeasurementEntry
import com.burak.healthapp.domain.export.ExportedExerciseEntry
import com.burak.healthapp.domain.export.ExportedGoalSettings
import com.burak.healthapp.domain.export.ExportedHydrationEntry
import com.burak.healthapp.domain.export.ExportedMealEntry
import com.burak.healthapp.domain.export.ExportedSleepSession
import com.burak.healthapp.domain.export.ExportedSmokingEntry
import com.burak.healthapp.domain.export.ExportedStepEntry
import com.burak.healthapp.domain.export.ExportedSupplementDoseEntry
import com.burak.healthapp.domain.export.ExportedSupplementTemplate
import com.burak.healthapp.domain.export.ExportedUserProfile
import com.burak.healthapp.domain.export.ExportedWaterReminderSettings
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.HealthDataExportRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.Instant

class HealthDataExportRepositoryImpl(
    private val settingsRepository: SettingsRepository,
    private val mealDao: MealDao,
    private val hydrationDao: HydrationDao,
    private val sleepDao: SleepDao,
    private val exerciseDao: ExerciseDao,
    private val smokingDao: SmokingDao,
    private val stepDao: StepDao,
    private val measurementDao: BodyMeasurementDao,
    private val templateDao: SupplementTemplateDao,
    private val doseDao: SupplementDoseDao,
) : HealthDataExportRepository {
    override suspend fun buildExportModel(
        exportedAt: Instant,
        appVersion: String,
    ): HealthDataExportModel = withContext(Dispatchers.IO) {
        val settings = settingsRepository.settings.first()

        HealthDataExportModel(
            schemaVersion = HealthDataExportModel.SCHEMA_VERSION,
            exportedAt = exportedAt.toString(),
            appVersion = appVersion,
            profile = settings.userProfile.toExported(),
            goals = settings.goalSettings.toExported(),
            waterReminderSettings = settings.waterReminderSettings.toExported(),
            themeMode = settings.themeMode.name,
            meals = mealDao.getAll().map { it.toDomain().toExported() },
            hydration = hydrationDao.getAll().map { it.toDomain().toExported() },
            sleep = sleepDao.getAll().map { it.toDomain().toExported() },
            exercise = exerciseDao.getAll().map { it.toDomain().toExported() },
            smoking = smokingDao.getAll().map { it.toDomain().toExported() },
            steps = stepDao.getAll().map { it.toDomain().toExported() },
            bodyMeasurements = measurementDao.getAll().map { it.toDomain().toExported() },
            supplementTemplates = templateDao.getAll().map { it.toDomain().toExported() },
            supplementDoseEntries = doseDao.getAll().map { it.toDomain().toExported() },
        )
    }
}

private fun UserProfile.toExported(): ExportedUserProfile = ExportedUserProfile(
    name = name,
    avatarInitials = avatarInitials,
    heightCm = heightCm,
)

private fun GoalSettings.toExported(): ExportedGoalSettings = ExportedGoalSettings(
    dailyCaloriesTarget = dailyCaloriesTarget,
    proteinTargetGrams = proteinTargetGrams,
    carbTargetGrams = carbTargetGrams,
    fatTargetGrams = fatTargetGrams,
    waterTargetMl = waterTargetMl,
    dailyStepTarget = dailyStepTarget,
    sleepTargetBedtime = sleepTargetBedtime.toString(),
    sleepTargetWakeTime = sleepTargetWakeTime.toString(),
    exerciseTargetDaysPerWeek = exerciseTargetDaysPerWeek,
    exerciseTargetDurationMinutes = exerciseTargetDurationMinutes,
    smokeDailyLimit = smokeDailyLimit,
    baselineWeightKg = baselineWeightKg,
    targetWeightKg = targetWeightKg,
    baselineShoulderCm = baselineShoulderCm,
    baselineWaistCm = baselineWaistCm,
    baselineHipCm = baselineHipCm,
)

private fun WaterReminderSettings.toExported(): ExportedWaterReminderSettings = ExportedWaterReminderSettings(
    enabled = enabled,
    startTime = startTime.toString(),
    endTime = endTime.toString(),
    intervalMinutes = intervalMinutes,
)

private fun MealEntry.toExported(): ExportedMealEntry = ExportedMealEntry(
    id = id,
    date = date.toString(),
    mealType = mealType.name,
    name = name,
    calories = calories,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    proteinGrams = proteinGrams,
    createdAt = createdAt.toString(),
)

private fun HydrationEntry.toExported(): ExportedHydrationEntry = ExportedHydrationEntry(
    id = id,
    date = date.toString(),
    amountMl = amountMl,
    createdAt = createdAt.toString(),
)

private fun SleepSession.toExported(): ExportedSleepSession = ExportedSleepSession(
    id = id,
    sessionDate = sessionDate.toString(),
    startTime = startTime.toString(),
    endTime = endTime.toString(),
)

private fun ExerciseEntry.toExported(): ExportedExerciseEntry = ExportedExerciseEntry(
    id = id,
    date = date.toString(),
    type = type.name,
    durationMinutes = durationMinutes,
    intensity = intensity.name,
)

private fun SmokingEntry.toExported(): ExportedSmokingEntry = ExportedSmokingEntry(
    id = id,
    date = date.toString(),
    count = count,
)

private fun StepEntry.toExported(): ExportedStepEntry = ExportedStepEntry(
    id = id,
    date = date.toString(),
    steps = steps,
    sensorBaseline = sensorBaseline,
    lastSensorValue = lastSensorValue,
    updatedAt = updatedAt.toString(),
)

private fun BodyMeasurementEntry.toExported(): ExportedBodyMeasurementEntry = ExportedBodyMeasurementEntry(
    id = id,
    date = date.toString(),
    weightKg = weightKg,
    shoulderCm = shoulderCm,
    waistCm = waistCm,
    hipCm = hipCm,
    recordedAt = recordedAt.toString(),
)

private fun SupplementTemplate.toExported(): ExportedSupplementTemplate = ExportedSupplementTemplate(
    id = id,
    name = name,
    targetAmount = targetAmount,
    unitLabel = unitLabel,
    isActive = isActive,
    sortOrder = sortOrder,
)

private fun SupplementDoseEntry.toExported(): ExportedSupplementDoseEntry = ExportedSupplementDoseEntry(
    id = id,
    templateId = templateId,
    date = date.toString(),
    amount = amount,
    loggedAt = loggedAt.toString(),
)
