package com.burak.healthapp.data.export

import androidx.room.withTransaction
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.CaffeineEntryEntity
import com.burak.healthapp.data.local.entity.CustomFoodEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
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
import com.burak.healthapp.domain.export.ExportedUserProfile
import com.burak.healthapp.domain.export.ExportedWaterReminderSettings
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.HealthDataImportException
import com.burak.healthapp.domain.export.ImportValidationError
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.HealthDataManagementRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class HealthDataManagementRepositoryImpl(
    private val database: HealthDatabase,
    private val settingsRepository: SettingsRepository,
) : HealthDataManagementRepository {
    override suspend fun importHealthData(model: HealthDataExportModel) {
        val prepared = try {
            model.toPreparedImport()
        } catch (exception: Exception) {
            throw HealthDataImportException(ImportValidationError.DecodeFailure, exception)
        }

        try {
            withContext(Dispatchers.IO) {
                database.withTransaction {
                    importMeals(prepared.meals)
                    importHydration(prepared.hydration)
                    importSleep(prepared.sleep)
                    importExercise(prepared.exercise)
                    importSmoking(prepared.smoking)
                    importSteps(prepared.steps)
                    importCaffeine(prepared.caffeine)
                    importBodyMeasurements(prepared.bodyMeasurements)
                    val templateIdMap = importSupplementTemplates(prepared.supplementTemplates)
                    importSupplementDoses(prepared.supplementDoses, templateIdMap)
                    importCustomFoods(prepared.customFoods)
                }
            }
        } catch (exception: Exception) {
            throw HealthDataImportException(ImportValidationError.DatabaseFailure, exception)
        }

        try {
            // Room and DataStore cannot be committed as one transaction here. If settings
            // persistence fails, health records remain imported and the caller must surface
            // this as a partial import instead of implying that nothing changed.
            settingsRepository.updateProfile(prepared.profile)
            settingsRepository.updateGoalSettings(prepared.goals)
            settingsRepository.updateWaterReminderSettings(prepared.waterReminderSettings)
            settingsRepository.updateThemeMode(prepared.themeMode)
        } catch (exception: Exception) {
            throw HealthDataImportException(ImportValidationError.PartialSettingsFailure, exception)
        }
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
                database.caffeineDao().deleteAll()
                database.bodyMeasurementDao().deleteAll()
                database.customFoodDao().deleteAll()
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

    private suspend fun importCaffeine(entries: List<CaffeineEntryEntity>) {
        val seenKeys = database.caffeineDao().getAll()
            .map { it.importKey() }
            .toMutableSet()
        entries.forEach { entry ->
            val key = entry.importKey()
            if (seenKeys.add(key)) {
                database.caffeineDao().upsert(entry.copy(id = 0))
            }
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

    private suspend fun importCustomFoods(entries: List<CustomFoodEntity>) {
        val plan = CustomFoodImportMergePlanner.plan(
            existing = database.customFoodDao().getAll().map(CustomFoodEntity::toImportRecord),
            imported = entries.map(CustomFoodEntity::toImportRecord),
        )

        plan.updates.forEach { update ->
            database.customFoodDao().upsert(update.toEntity())
        }
        plan.inserts.forEach { insert ->
            database.customFoodDao().upsert(insert.toEntity(id = 0))
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
    val caffeine: List<CaffeineEntryEntity>,
    val bodyMeasurements: List<BodyMeasurementEntity>,
    val supplementTemplates: List<SupplementTemplateImport>,
    val supplementDoses: List<SupplementDoseEntryEntity>,
    val customFoods: List<CustomFoodEntity>,
)

private data class SupplementTemplateImport(
    val oldId: Long,
    val entity: SupplementTemplateEntity,
)

private fun HealthDataExportModel.toPreparedImport(): PreparedHealthDataImport = PreparedHealthDataImport(
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
    caffeine = caffeineEntries.map(ExportedCaffeineEntry::toEntity),
    bodyMeasurements = bodyMeasurements.map(ExportedBodyMeasurementEntry::toEntity),
    supplementTemplates = supplementTemplates.map(ExportedSupplementTemplate::toImport),
    supplementDoses = supplementDoseEntries.map(ExportedSupplementDoseEntry::toEntity),
    customFoods = customFoods.map(ExportedCustomFood::toEntity),
)

private fun ExportedUserProfile.toDomain(): UserProfile = UserProfile(
    name = name.trim().ifBlank { "Misafir" },
    avatarInitials = avatarInitials.trim().ifBlank { "M" },
    heightCm = heightCm,
)

private fun ExportedGoalSettings.toDomain(): GoalSettings = GoalSettings(
    dailyCaloriesTarget = dailyCaloriesTarget,
    proteinTargetGrams = proteinTargetGrams,
    carbTargetGrams = carbTargetGrams,
    fatTargetGrams = fatTargetGrams,
    waterTargetMl = waterTargetMl,
    dailyStepTarget = dailyStepTarget,
    dailyCaffeineLimitMg = dailyCaffeineLimitMg,
    caffeineCutoffTime = LocalTime.parse(caffeineCutoffTime),
    caffeineSleepBufferHours = caffeineSleepBufferHours,
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

private fun ExportedWaterReminderSettings.toDomain(): WaterReminderSettings = WaterReminderSettings(
    enabled = enabled,
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime),
    intervalMinutes = intervalMinutes,
)

private fun ExportedMealEntry.toEntity(): MealEntryEntity = MealEntryEntity(
    date = LocalDate.parse(date),
    mealType = mealType,
    name = name,
    calories = calories,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    proteinGrams = proteinGrams,
    createdAt = LocalDateTime.parse(createdAt),
)

private fun ExportedHydrationEntry.toEntity(): HydrationEntryEntity = HydrationEntryEntity(
    date = LocalDate.parse(date),
    amountMl = amountMl,
    createdAt = LocalDateTime.parse(createdAt),
)

private fun ExportedSleepSession.toEntity(): SleepSessionEntity = SleepSessionEntity(
    sessionDate = LocalDate.parse(sessionDate),
    startTime = LocalDateTime.parse(startTime),
    endTime = LocalDateTime.parse(endTime),
)

private fun ExportedExerciseEntry.toEntity(): ExerciseEntryEntity = ExerciseEntryEntity(
    date = LocalDate.parse(date),
    type = type,
    durationMinutes = durationMinutes,
    intensity = intensity,
)

private fun ExportedSmokingEntry.toEntity(): SmokingEntryEntity = SmokingEntryEntity(
    date = LocalDate.parse(date),
    count = count,
)

private fun ExportedStepEntry.toEntity(): StepEntryEntity = StepEntryEntity(
    date = LocalDate.parse(date),
    steps = steps,
    sensorBaseline = sensorBaseline,
    lastSensorValue = lastSensorValue,
    updatedAt = LocalDateTime.parse(updatedAt),
)

private fun ExportedCaffeineEntry.toEntity(): CaffeineEntryEntity = CaffeineEntryEntity(
    date = LocalDate.parse(date),
    time = LocalTime.parse(time),
    drinkType = drinkType,
    size = size,
    estimatedMg = estimatedMg,
    customName = customName,
    createdAt = LocalDateTime.parse(createdAt),
)

private fun ExportedBodyMeasurementEntry.toEntity(): BodyMeasurementEntity = BodyMeasurementEntity(
    date = LocalDate.parse(date),
    weightKg = weightKg,
    shoulderCm = shoulderCm,
    waistCm = waistCm,
    hipCm = hipCm,
    recordedAt = LocalDateTime.parse(recordedAt),
)

private fun ExportedSupplementTemplate.toImport(): SupplementTemplateImport = SupplementTemplateImport(
    oldId = id,
    entity = SupplementTemplateEntity(
        name = name.trim(),
        targetAmount = targetAmount,
        unitLabel = unitLabel.trim(),
        isActive = isActive,
        sortOrder = sortOrder,
    ),
)

private fun ExportedSupplementDoseEntry.toEntity(): SupplementDoseEntryEntity = SupplementDoseEntryEntity(
    templateId = templateId,
    date = LocalDate.parse(date),
    amount = amount,
    loggedAt = LocalDateTime.parse(loggedAt),
)

private fun MealEntryEntity.importKey(): String = listOf(date, mealType, name.trim(), calories, createdAt).joinToString("|")

private fun HydrationEntryEntity.importKey(): String = listOf(date, amountMl, createdAt).joinToString("|")

private fun CaffeineEntryEntity.importKey(): String = listOf(date, time, drinkType, size, estimatedMg, createdAt).joinToString("|")

private fun SupplementDoseEntryEntity.importKey(): String = listOf(templateId, date, loggedAt).joinToString("|")

private fun String.normalizedTemplateName(): String = trim().lowercase()

private fun ExportedCustomFood.toEntity(): CustomFoodEntity = CustomFoodEntity(
    name = name.trim(),
    brand = brand?.trim()?.ifBlank { null },
    servingName = servingName.trim(),
    servingGrams = servingGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    fiberGrams = fiberGrams,
    sugarGrams = sugarGrams,
    sodiumMg = sodiumMg,
    isFavorite = isFavorite,
    createdAt = LocalDateTime.parse(createdAt),
    updatedAt = LocalDateTime.parse(updatedAt),
)

private fun CustomFoodEntity.toImportRecord(): CustomFoodImportRecord = CustomFoodImportRecord(
    id = id,
    name = name,
    brand = brand,
    servingName = servingName,
    servingGrams = servingGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    fiberGrams = fiberGrams,
    sugarGrams = sugarGrams,
    sodiumMg = sodiumMg,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun CustomFoodImportUpdate.toEntity(): CustomFoodEntity = imported.toEntity(
    id = existing.id,
    createdAt = existing.createdAt,
)

private fun CustomFoodImportRecord.toEntity(
    id: Long = this.id,
    createdAt: LocalDateTime = this.createdAt,
): CustomFoodEntity = CustomFoodEntity(
    id = id,
    name = name,
    brand = brand,
    servingName = servingName,
    servingGrams = servingGrams,
    calories = calories,
    proteinGrams = proteinGrams,
    carbsGrams = carbsGrams,
    fatGrams = fatGrams,
    fiberGrams = fiberGrams,
    sugarGrams = sugarGrams,
    sodiumMg = sodiumMg,
    isFavorite = isFavorite,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
