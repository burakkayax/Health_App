package com.burak.healthapp.data.export

import androidx.room.withTransaction
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
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
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.HealthDataManagementRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HealthDataManagementRepositoryImpl(
    private val database: HealthDatabase,
    private val settingsRepository: SettingsRepository,
) : HealthDataManagementRepository {
    override suspend fun importHealthData(model: HealthDataExportModel) {
        val prepared = model.toPreparedImport()

        withContext(Dispatchers.IO) {
            database.withTransaction {
                importMeals(prepared.meals)
                importHydration(prepared.hydration)
                importSleep(prepared.sleep)
                importExercise(prepared.exercise)
                importSmoking(prepared.smoking)
                importSteps(prepared.steps)
                importBodyMeasurements(prepared.bodyMeasurements)
                val templateIdMap = importSupplementTemplates(prepared.supplementTemplates)
                importSupplementDoses(prepared.supplementDoses, templateIdMap)
            }
        }

        settingsRepository.updateProfile(prepared.profile)
        settingsRepository.updateGoalSettings(prepared.goals)
        settingsRepository.updateWaterReminderSettings(prepared.waterReminderSettings)
        settingsRepository.updateThemeMode(prepared.themeMode)
    }

    override suspend fun deleteAllHealthData() {
        withContext(Dispatchers.IO) {
            database.withTransaction {
                database.supplementDoseDao().deleteAll()
                database.supplementCheckDao().deleteAll()
                database.supplementTemplateDao().deleteAll()
                database.mealDao().deleteAll()
                database.hydrationDao().deleteAll()
                database.sleepDao().deleteAll()
                database.exerciseDao().deleteAll()
                database.smokingDao().deleteAll()
                database.stepDao().deleteAll()
                database.bodyMeasurementDao().deleteAll()
            }
        }
    }

    private suspend fun importMeals(entries: List<MealEntryEntity>) {
        val seenKeys = database.mealDao().getAll()
            .map { it.importKey() }
            .toMutableSet()
        entries.forEach { entry ->
            val key = entry.importKey()
            if (seenKeys.add(key)) {
                database.mealDao().upsert(entry.copy(id = 0))
            }
        }
    }

    private suspend fun importHydration(entries: List<HydrationEntryEntity>) {
        val seenKeys = database.hydrationDao().getAll()
            .map { it.importKey() }
            .toMutableSet()
        entries.forEach { entry ->
            val key = entry.importKey()
            if (seenKeys.add(key)) {
                database.hydrationDao().upsert(entry.copy(id = 0))
            }
        }
    }

    private suspend fun importSleep(entries: List<SleepSessionEntity>) {
        entries.forEach { entry ->
            database.sleepDao().deleteForDate(entry.sessionDate)
            database.sleepDao().upsert(entry.copy(id = 0))
        }
    }

    private suspend fun importExercise(entries: List<ExerciseEntryEntity>) {
        entries.forEach { entry ->
            database.exerciseDao().deleteForDate(entry.date)
            database.exerciseDao().upsert(entry.copy(id = 0))
        }
    }

    private suspend fun importSmoking(entries: List<SmokingEntryEntity>) {
        entries.forEach { entry ->
            database.smokingDao().deleteForDate(entry.date)
            database.smokingDao().upsert(entry.copy(id = 0))
        }
    }

    private suspend fun importSteps(entries: List<StepEntryEntity>) {
        entries.forEach { entry ->
            database.stepDao().deleteForDate(entry.date)
            database.stepDao().upsert(entry.copy(id = 0))
        }
    }

    private suspend fun importBodyMeasurements(entries: List<BodyMeasurementEntity>) {
        entries.forEach { entry ->
            database.bodyMeasurementDao().deleteForDate(entry.date)
            database.bodyMeasurementDao().upsert(entry.copy(id = 0))
        }
    }

    private suspend fun importSupplementTemplates(
        templates: List<SupplementTemplateImport>,
    ): Map<Long, Long> {
        val currentByName = database.supplementTemplateDao()
            .getAll()
            .associateBy { it.name.normalizedTemplateName() }
            .toMutableMap()
        val templateIdMap = mutableMapOf<Long, Long>()

        templates.forEachIndexed { index, templateImport ->
            val exported = templateImport.entity.copy(sortOrder = index)
            val normalizedName = exported.name.normalizedTemplateName()
            val existing = currentByName[normalizedName]
            if (existing != null) {
                val updated = existing.copy(
                    name = exported.name,
                    targetAmount = exported.targetAmount,
                    unitLabel = exported.unitLabel,
                    isActive = exported.isActive,
                    sortOrder = exported.sortOrder,
                )
                database.supplementTemplateDao().upsertAll(listOf(updated))
                templateIdMap[templateImport.oldId] = existing.id
                currentByName[normalizedName] = updated
            } else {
                val newId = database.supplementTemplateDao().insert(exported.copy(id = 0))
                templateIdMap[templateImport.oldId] = newId
                currentByName[normalizedName] = exported.copy(id = newId)
            }
        }

        return templateIdMap
    }

    private suspend fun importSupplementDoses(
        entries: List<SupplementDoseEntryEntity>,
        templateIdMap: Map<Long, Long>,
    ) {
        val seenKeys = database.supplementDoseDao().getAll()
            .map { it.importKey() }
            .toMutableSet()
        entries.forEach { entry ->
            val mappedTemplateId = templateIdMap[entry.templateId] ?: return@forEach
            val mappedEntry = entry.copy(id = 0, templateId = mappedTemplateId)
            val key = mappedEntry.importKey()
            if (seenKeys.add(key)) {
                database.supplementDoseDao().upsertAll(listOf(mappedEntry))
            }
        }
    }
}

private data class PreparedHealthDataImport(
    val profile: UserProfile,
    val goals: GoalSettings,
    val waterReminderSettings: WaterReminderSettings,
    val themeMode: ThemeMode,
    val meals: List<MealEntryEntity>,
    val hydration: List<HydrationEntryEntity>,
    val sleep: List<SleepSessionEntity>,
    val exercise: List<ExerciseEntryEntity>,
    val smoking: List<SmokingEntryEntity>,
    val steps: List<StepEntryEntity>,
    val bodyMeasurements: List<BodyMeasurementEntity>,
    val supplementTemplates: List<SupplementTemplateImport>,
    val supplementDoses: List<SupplementDoseEntryEntity>,
)

private data class SupplementTemplateImport(
    val oldId: Long,
    val entity: SupplementTemplateEntity,
)

private fun HealthDataExportModel.toPreparedImport(): PreparedHealthDataImport {
    return PreparedHealthDataImport(
        profile = profile.toDomain(),
        goals = goals.toDomain(),
        waterReminderSettings = waterReminderSettings.toDomain(),
        themeMode = ThemeMode.entries.firstOrNull { it.name == themeMode } ?: ThemeMode.SYSTEM,
        meals = meals.map(ExportedMealEntry::toEntity),
        hydration = hydration.map(ExportedHydrationEntry::toEntity),
        sleep = sleep.map(ExportedSleepSession::toEntity),
        exercise = exercise.map(ExportedExerciseEntry::toEntity),
        smoking = smoking.map(ExportedSmokingEntry::toEntity),
        steps = steps.map(ExportedStepEntry::toEntity),
        bodyMeasurements = bodyMeasurements.map(ExportedBodyMeasurementEntry::toEntity),
        supplementTemplates = supplementTemplates.map(ExportedSupplementTemplate::toImport),
        supplementDoses = supplementDoseEntries.map(ExportedSupplementDoseEntry::toEntity),
    )
}

private fun ExportedUserProfile.toDomain(): UserProfile {
    return UserProfile(
        name = name.trim().ifBlank { "Misafir" },
        avatarInitials = avatarInitials.trim().ifBlank { "M" },
        heightCm = heightCm,
    )
}

private fun ExportedGoalSettings.toDomain(): GoalSettings {
    return GoalSettings(
        dailyCaloriesTarget = dailyCaloriesTarget,
        proteinTargetGrams = proteinTargetGrams,
        carbTargetGrams = carbTargetGrams,
        fatTargetGrams = fatTargetGrams,
        waterTargetMl = waterTargetMl,
        dailyStepTarget = dailyStepTarget,
        sleepTargetBedtime = LocalTime.parse(sleepTargetBedtime),
        sleepTargetWakeTime = LocalTime.parse(sleepTargetWakeTime),
        exerciseTargetDaysPerWeek = exerciseTargetDaysPerWeek,
        exerciseTargetDurationMinutes = exerciseTargetDurationMinutes,
        smokeDailyLimit = smokeDailyLimit,
        baselineWeightKg = baselineWeightKg,
        targetWeightKg = targetWeightKg,
        baselineShoulderCm = baselineShoulderCm,
        baselineWaistCm = baselineWaistCm,
        baselineHipCm = baselineHipCm,
    )
}

private fun ExportedWaterReminderSettings.toDomain(): WaterReminderSettings {
    return WaterReminderSettings(
        enabled = enabled,
        startTime = LocalTime.parse(startTime),
        endTime = LocalTime.parse(endTime),
        intervalMinutes = intervalMinutes,
    )
}

private fun ExportedMealEntry.toEntity(): MealEntryEntity {
    return MealEntryEntity(
        date = LocalDate.parse(date),
        mealType = mealType,
        name = name,
        calories = calories,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        proteinGrams = proteinGrams,
        createdAt = LocalDateTime.parse(createdAt),
    )
}

private fun ExportedHydrationEntry.toEntity(): HydrationEntryEntity {
    return HydrationEntryEntity(
        date = LocalDate.parse(date),
        amountMl = amountMl,
        createdAt = LocalDateTime.parse(createdAt),
    )
}

private fun ExportedSleepSession.toEntity(): SleepSessionEntity {
    return SleepSessionEntity(
        sessionDate = LocalDate.parse(sessionDate),
        startTime = LocalDateTime.parse(startTime),
        endTime = LocalDateTime.parse(endTime),
    )
}

private fun ExportedExerciseEntry.toEntity(): ExerciseEntryEntity {
    return ExerciseEntryEntity(
        date = LocalDate.parse(date),
        type = type,
        durationMinutes = durationMinutes,
        intensity = intensity,
    )
}

private fun ExportedSmokingEntry.toEntity(): SmokingEntryEntity {
    return SmokingEntryEntity(
        date = LocalDate.parse(date),
        count = count,
    )
}

private fun ExportedStepEntry.toEntity(): StepEntryEntity {
    return StepEntryEntity(
        date = LocalDate.parse(date),
        steps = steps,
        sensorBaseline = sensorBaseline,
        lastSensorValue = lastSensorValue,
        updatedAt = LocalDateTime.parse(updatedAt),
    )
}

private fun ExportedBodyMeasurementEntry.toEntity(): BodyMeasurementEntity {
    return BodyMeasurementEntity(
        date = LocalDate.parse(date),
        weightKg = weightKg,
        shoulderCm = shoulderCm,
        waistCm = waistCm,
        hipCm = hipCm,
        recordedAt = LocalDateTime.parse(recordedAt),
    )
}

private fun ExportedSupplementTemplate.toImport(): SupplementTemplateImport {
    return SupplementTemplateImport(
        oldId = id,
        entity = SupplementTemplateEntity(
            name = name.trim(),
            targetAmount = targetAmount,
            unitLabel = unitLabel.trim(),
            isActive = isActive,
            sortOrder = sortOrder,
        ),
    )
}

private fun ExportedSupplementDoseEntry.toEntity(): SupplementDoseEntryEntity {
    return SupplementDoseEntryEntity(
        templateId = templateId,
        date = LocalDate.parse(date),
        amount = amount,
        loggedAt = LocalDateTime.parse(loggedAt),
    )
}

private fun MealEntryEntity.importKey(): String {
    return listOf(date, mealType, name.trim(), calories, createdAt).joinToString("|")
}

private fun HydrationEntryEntity.importKey(): String {
    return listOf(date, amountMl, createdAt).joinToString("|")
}

private fun SupplementDoseEntryEntity.importKey(): String {
    return listOf(templateId, date, loggedAt).joinToString("|")
}

private fun String.normalizedTemplateName(): String {
    return trim().lowercase()
}
