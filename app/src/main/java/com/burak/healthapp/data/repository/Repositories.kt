package com.burak.healthapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.burak.healthapp.data.local.BodyMeasurementDao
import com.burak.healthapp.data.local.BodyMeasurementEntity
import com.burak.healthapp.data.local.ExerciseDao
import com.burak.healthapp.data.local.ExerciseEntryEntity
import com.burak.healthapp.data.local.HydrationDao
import com.burak.healthapp.data.local.HydrationEntryEntity
import com.burak.healthapp.data.local.MealDao
import com.burak.healthapp.data.local.MealEntryEntity
import com.burak.healthapp.data.local.SleepDao
import com.burak.healthapp.data.local.SleepSessionEntity
import com.burak.healthapp.data.local.SmokingDao
import com.burak.healthapp.data.local.SmokingEntryEntity
import com.burak.healthapp.data.local.StepDao
import com.burak.healthapp.data.local.StepEntryEntity
import com.burak.healthapp.data.local.SupplementDoseDao
import com.burak.healthapp.data.local.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.SupplementTemplateDao
import com.burak.healthapp.data.local.SupplementTemplateEntity
import com.burak.healthapp.domain.calculation.averageCalories
import com.burak.healthapp.domain.calculation.averageProtein
import com.burak.healthapp.domain.calculation.averageSleepMinutes
import com.burak.healthapp.domain.calculation.averageSteps
import com.burak.healthapp.domain.calculation.averageWaterMl
import com.burak.healthapp.domain.calculation.buildCalendarWeekDays
import com.burak.healthapp.domain.calculation.buildInterpolatedWeightTrendPoints
import com.burak.healthapp.domain.calculation.buildMonthToDateDays
import com.burak.healthapp.domain.calculation.buildStepTrendPoints
import com.burak.healthapp.domain.calculation.buildWeeklyCalories
import com.burak.healthapp.domain.calculation.buildWeekToDateDays
import com.burak.healthapp.domain.calculation.clipWeightTrendDays
import com.burak.healthapp.domain.calculation.WeightMeasurementSample
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TodaySnapshot
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<SettingsState>

    fun observeSupplementTemplates(): Flow<List<SupplementTemplate>>

    suspend fun completeOnboarding(
        profile: UserProfile,
        goals: GoalSettings,
        initialMeasurement: BodyMeasurementEntry,
        supplements: List<String>,
    )

    suspend fun updateGoalSettings(goals: GoalSettings)

    suspend fun updateWaterReminderSettings(settings: WaterReminderSettings)

    suspend fun updateProfile(profile: UserProfile)

    suspend fun updateThemeMode(mode: ThemeMode)

    suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>)
}

interface DashboardRepository {
    fun observeToday(date: LocalDate = LocalDate.now()): Flow<TodaySnapshot>

    fun observeMealsForDate(date: LocalDate = LocalDate.now()): Flow<List<MealEntry>>

    fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?>

    fun observeWeightHistory(): Flow<List<BodyMeasurementEntry>>

    fun observeSleepSessionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSession>>

    fun observeStepsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntry>>

    suspend fun saveMealEntry(entry: MealEntry)

    suspend fun deleteMealEntry(id: Long)

    suspend fun deleteHydrationEntry(id: Long)

    suspend fun deleteSleepForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteExerciseForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteSmokingForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteSupplementDoseForDate(templateId: Long, date: LocalDate = LocalDate.now())

    suspend fun deleteStepsForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteBodyMeasurement(id: Long)

    suspend fun addHydration(amountMl: Int, date: LocalDate = LocalDate.now())

    suspend fun saveSleepSession(session: SleepSession)

    suspend fun saveExerciseEntry(entry: ExerciseEntry, date: LocalDate = LocalDate.now())

    suspend fun saveSmokingCount(count: Int, date: LocalDate = LocalDate.now())

    suspend fun incrementSmokingCount(date: LocalDate = LocalDate.now(), delta: Int = 1)

    suspend fun saveSupplementDoseEntries(
        entries: List<SupplementDoseEntry>,
        date: LocalDate = LocalDate.now(),
    )

    suspend fun saveBodyMeasurement(entry: BodyMeasurementEntry)

    suspend fun saveWeightMeasurement(weightKg: Float, date: LocalDate = LocalDate.now())

    suspend fun recordStepSensorValue(sensorValue: Int, date: LocalDate = LocalDate.now())
}

interface TrendsRepository {
    fun observeTrends(
        period: TrendsPeriod,
        endDate: LocalDate = LocalDate.now(),
    ): Flow<TrendsSnapshot>
}

private object SettingsKeys {
    val onboardingComplete = booleanPreferencesKey("onboarding_complete")
    val userName = stringPreferencesKey("user_name")
    val avatarInitials = stringPreferencesKey("avatar_initials")
    val heightCm = floatPreferencesKey("height_cm")
    val themeMode = stringPreferencesKey("theme_mode")
    val dailyCaloriesTarget = intPreferencesKey("daily_calories_target")
    val proteinTarget = intPreferencesKey("protein_target")
    val carbTarget = intPreferencesKey("carb_target")
    val fatTarget = intPreferencesKey("fat_target")
    val waterTarget = intPreferencesKey("water_target")
    val dailyStepTarget = intPreferencesKey("daily_step_target")
    val sleepTarget = intPreferencesKey("sleep_target")
    val sleepTargetBedtime = stringPreferencesKey("sleep_target_bedtime")
    val sleepTargetWakeTime = stringPreferencesKey("sleep_target_wake_time")
    val exerciseTargetDays = intPreferencesKey("exercise_target_days")
    val exerciseTargetDuration = intPreferencesKey("exercise_target_duration")
    val smokeDailyLimit = intPreferencesKey("smoke_daily_limit")
    val waterReminderEnabled = booleanPreferencesKey("water_reminder_enabled")
    val waterReminderStartTime = stringPreferencesKey("water_reminder_start_time")
    val waterReminderEndTime = stringPreferencesKey("water_reminder_end_time")
    val waterReminderIntervalMinutes = intPreferencesKey("water_reminder_interval_minutes")
    val baselineWeight = floatPreferencesKey("baseline_weight")
    val targetWeight = floatPreferencesKey("target_weight")
    val baselineShoulder = floatPreferencesKey("baseline_shoulder")
    val baselineWaist = floatPreferencesKey("baseline_waist")
    val baselineHip = floatPreferencesKey("baseline_hip")
}

class DefaultSettingsRepository(
    private val dataStore: DataStore<Preferences>,
    private val templateDao: SupplementTemplateDao,
    private val measurementDao: BodyMeasurementDao,
) : SettingsRepository {
    override val settings: Flow<SettingsState> = dataStore.data.map { preferences ->
        val legacySleepTarget = preferences[SettingsKeys.sleepTarget] ?: 480
        val fallbackSleepSchedule = deriveSleepScheduleFromLegacy(legacySleepTarget)
        val bedtime = preferences[SettingsKeys.sleepTargetBedtime]?.let(LocalTime::parse)
            ?: fallbackSleepSchedule.first
        val wakeTime = preferences[SettingsKeys.sleepTargetWakeTime]?.let(LocalTime::parse)
            ?: fallbackSleepSchedule.second

        SettingsState(
            onboardingCompleted = preferences[SettingsKeys.onboardingComplete] ?: false,
            userProfile = UserProfile(
                name = preferences[SettingsKeys.userName] ?: "Misafir",
                avatarInitials = preferences[SettingsKeys.avatarInitials] ?: "M",
                heightCm = preferences[SettingsKeys.heightCm],
            ),
            goalSettings = GoalSettings(
                dailyCaloriesTarget = preferences[SettingsKeys.dailyCaloriesTarget] ?: 2200,
                proteinTargetGrams = preferences[SettingsKeys.proteinTarget] ?: 160,
                carbTargetGrams = preferences[SettingsKeys.carbTarget] ?: 220,
                fatTargetGrams = preferences[SettingsKeys.fatTarget] ?: 70,
                waterTargetMl = preferences[SettingsKeys.waterTarget] ?: 2500,
                dailyStepTarget = preferences[SettingsKeys.dailyStepTarget] ?: 8000,
                sleepTargetBedtime = bedtime,
                sleepTargetWakeTime = wakeTime,
                exerciseTargetDaysPerWeek = preferences[SettingsKeys.exerciseTargetDays] ?: 4,
                exerciseTargetDurationMinutes = preferences[SettingsKeys.exerciseTargetDuration] ?: 45,
                smokeDailyLimit = preferences[SettingsKeys.smokeDailyLimit] ?: 0,
                baselineWeightKg = preferences[SettingsKeys.baselineWeight] ?: 78f,
                targetWeightKg = preferences[SettingsKeys.targetWeight] ?: 74f,
                baselineShoulderCm = preferences[SettingsKeys.baselineShoulder] ?: 118f,
                baselineWaistCm = preferences[SettingsKeys.baselineWaist] ?: 88f,
                baselineHipCm = preferences[SettingsKeys.baselineHip] ?: 99f,
            ),
            waterReminderSettings = WaterReminderSettings(
                enabled = preferences[SettingsKeys.waterReminderEnabled] ?: false,
                startTime = preferences[SettingsKeys.waterReminderStartTime]?.let(LocalTime::parse)
                    ?: LocalTime.of(9, 0),
                endTime = preferences[SettingsKeys.waterReminderEndTime]?.let(LocalTime::parse)
                    ?: LocalTime.of(21, 0),
                intervalMinutes = preferences[SettingsKeys.waterReminderIntervalMinutes] ?: 60,
            ),
            themeMode = ThemeMode.entries.firstOrNull { mode ->
                mode.name == preferences[SettingsKeys.themeMode]
            } ?: ThemeMode.SYSTEM,
        )
    }

    override fun observeSupplementTemplates(): Flow<List<SupplementTemplate>> {
        return templateDao.observeActive().map { templates ->
            templates.map(SupplementTemplateEntity::toDomain)
        }
    }

    override suspend fun completeOnboarding(
        profile: UserProfile,
        goals: GoalSettings,
        initialMeasurement: BodyMeasurementEntry,
        supplements: List<String>,
    ) {
        updateProfile(profile)
        updateGoalSettings(goals)
        measurementDao.upsert(initialMeasurement.toEntity())
        replaceSupplementTemplates(
            createSupplementTemplatesFromNames(
                names = supplements.ifEmpty { DEFAULT_SUPPLEMENT_NAMES },
            ),
        )
        dataStore.edit { preferences ->
            preferences[SettingsKeys.onboardingComplete] = true
        }
    }

    override suspend fun updateGoalSettings(goals: GoalSettings) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.dailyCaloriesTarget] = goals.dailyCaloriesTarget
            preferences[SettingsKeys.proteinTarget] = goals.proteinTargetGrams
            preferences[SettingsKeys.carbTarget] = goals.carbTargetGrams
            preferences[SettingsKeys.fatTarget] = goals.fatTargetGrams
            preferences[SettingsKeys.waterTarget] = goals.waterTargetMl
            preferences[SettingsKeys.dailyStepTarget] = goals.dailyStepTarget
            preferences[SettingsKeys.sleepTargetBedtime] = goals.sleepTargetBedtime.toString()
            preferences[SettingsKeys.sleepTargetWakeTime] = goals.sleepTargetWakeTime.toString()
            preferences[SettingsKeys.exerciseTargetDays] = goals.exerciseTargetDaysPerWeek
            preferences[SettingsKeys.exerciseTargetDuration] = goals.exerciseTargetDurationMinutes
            preferences[SettingsKeys.smokeDailyLimit] = goals.smokeDailyLimit
            preferences[SettingsKeys.baselineWeight] = goals.baselineWeightKg
            preferences[SettingsKeys.targetWeight] = goals.targetWeightKg
            preferences[SettingsKeys.baselineShoulder] = goals.baselineShoulderCm
            preferences[SettingsKeys.baselineWaist] = goals.baselineWaistCm
            preferences[SettingsKeys.baselineHip] = goals.baselineHipCm
        }
    }

    override suspend fun updateWaterReminderSettings(settings: WaterReminderSettings) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.waterReminderEnabled] = settings.enabled
            preferences[SettingsKeys.waterReminderStartTime] = settings.startTime.toString()
            preferences[SettingsKeys.waterReminderEndTime] = settings.endTime.toString()
            preferences[SettingsKeys.waterReminderIntervalMinutes] = settings.intervalMinutes.coerceAtLeast(15)
        }
    }

    override suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.userName] = profile.name
            preferences[SettingsKeys.avatarInitials] = profile.avatarInitials
            if (profile.heightCm != null) {
                preferences[SettingsKeys.heightCm] = profile.heightCm
            } else {
                preferences.remove(SettingsKeys.heightCm)
            }
        }
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.themeMode] = mode.name
        }
    }

    override suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>) {
        val sanitizedTemplates = templates
            .map { template ->
                template.copy(
                    name = template.name.trim(),
                    targetAmount = template.targetAmount.takeIf { it > 0f } ?: 1f,
                    unitLabel = template.unitLabel.trim().ifBlank { "doz" },
                )
            }
            .filter { it.name.isNotBlank() }
            .distinctBy { it.name.lowercase() }

        val current = templateDao.getAll()
        val currentById = current.associateBy { it.id }

        val updated = sanitizedTemplates.mapIndexed { index, template ->
            val existing = currentById[template.id]
            if (existing != null) {
                existing.copy(
                    name = template.name,
                    targetAmount = template.targetAmount,
                    unitLabel = template.unitLabel,
                    isActive = true,
                    sortOrder = index,
                )
            } else {
                SupplementTemplateEntity(
                    name = template.name,
                    targetAmount = template.targetAmount,
                    unitLabel = template.unitLabel,
                    isActive = true,
                    sortOrder = index,
                )
            }
        }

        templateDao.upsertAll(updated)

        val keptIds = sanitizedTemplates.mapNotNull { template ->
            currentById[template.id]?.id
        }.toSet()
        val deactivateIds = current
            .filter { template -> template.id !in keptIds }
            .map { it.id }

        if (deactivateIds.isNotEmpty()) {
            templateDao.deactivate(deactivateIds)
        }
    }
}

class DefaultDashboardRepository(
    private val settingsRepository: SettingsRepository,
    private val mealDao: MealDao,
    private val hydrationDao: HydrationDao,
    private val sleepDao: SleepDao,
    private val exerciseDao: ExerciseDao,
    private val smokingDao: SmokingDao,
    private val stepDao: StepDao,
    private val templateDao: SupplementTemplateDao,
    private val doseDao: SupplementDoseDao,
    private val measurementDao: BodyMeasurementDao,
) : DashboardRepository {
    override fun observeToday(date: LocalDate): Flow<TodaySnapshot> {
        val weekStartDate = buildWeekToDateDays(date).first()
        val baseSnapshot = combine(
            combine(
                settingsRepository.settings,
                mealDao.observeForDate(date),
                hydrationDao.observeForDate(date),
                sleepDao.observeForDate(date),
            ) { settings, meals, hydration, sleep ->
                Quadruple(settings, meals, hydration, sleep)
            },
            combine(
                exerciseDao.observeForDate(date),
                exerciseDao.observeBetween(weekStartDate, date),
                smokingDao.observeForDate(date),
                stepDao.observeForDate(date),
                stepDao.observeBetween(weekStartDate, date),
            ) { exercise, weekExercises, smoking, steps, weekSteps ->
                SecondaryTodaySnapshot(
                    exerciseEntryForDate = exercise?.toDomain(),
                    weekExerciseEntries = weekExercises.map(ExerciseEntryEntity::toDomain),
                    smokingEntryForDate = smoking?.toDomain(),
                    stepEntryForDate = steps?.toDomain(),
                    weekStepEntries = weekSteps.map(StepEntryEntity::toDomain),
                )
            },
        ) { primary, secondary ->
            val (settings, meals, hydration, sleep) = primary
            BaseTodaySnapshot(
                settings = settings,
                meals = meals.map(MealEntryEntity::toDomain),
                hydrationEntries = hydration.map(HydrationEntryEntity::toDomain),
                sleepSessionForDate = sleep?.toDomain(),
                exerciseEntryForDate = secondary.exerciseEntryForDate,
                weekExerciseEntries = secondary.weekExerciseEntries,
                smokingEntryForDate = secondary.smokingEntryForDate,
                stepEntryForDate = secondary.stepEntryForDate,
                weekStepEntries = secondary.weekStepEntries,
            )
        }

        return combine(
            baseSnapshot,
            templateDao.observeActive(),
            doseDao.observeForDate(date),
            measurementDao.observeForDate(date),
        ) { base, templates, doses, measurement ->
            TodaySnapshot(
                settings = base.settings,
                meals = base.meals,
                hydrationEntries = base.hydrationEntries,
                sleepSessionForDate = base.sleepSessionForDate,
                exerciseEntryForDate = base.exerciseEntryForDate,
                weekExerciseEntries = base.weekExerciseEntries,
                smokingEntryForDate = base.smokingEntryForDate,
                stepEntryForDate = base.stepEntryForDate,
                weekStepEntries = base.weekStepEntries,
                supplementTemplates = templates.map(SupplementTemplateEntity::toDomain),
                supplementDoseEntries = doses.map(SupplementDoseEntryEntity::toDomain),
                measurementForDate = measurement?.toDomain(),
            )
        }
    }

    override fun observeMealsForDate(date: LocalDate): Flow<List<MealEntry>> {
        return mealDao.observeForDate(date).map { entries ->
            entries.map(MealEntryEntity::toDomain)
        }
    }

    override fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?> {
        return measurementDao.observeLatest().map { measurement ->
            measurement?.toDomain()
        }
    }

    override fun observeWeightHistory(): Flow<List<BodyMeasurementEntry>> {
        return measurementDao.observeAll().map { measurements ->
            measurements.map(BodyMeasurementEntity::toDomain)
        }
    }

    override fun observeSleepSessionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSession>> {
        return sleepDao.observeBetween(startDate, endDate).map { sessions ->
            sessions.map(SleepSessionEntity::toDomain)
        }
    }

    override fun observeStepsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntry>> {
        return stepDao.observeBetween(startDate, endDate).map { entries ->
            entries.map(StepEntryEntity::toDomain)
        }
    }

    override suspend fun saveMealEntry(entry: MealEntry) {
        mealDao.upsert(entry.toEntity())
    }

    override suspend fun deleteMealEntry(id: Long) {
        mealDao.deleteById(id)
    }

    override suspend fun deleteHydrationEntry(id: Long) {
        hydrationDao.deleteById(id)
    }

    override suspend fun deleteSleepForDate(date: LocalDate) {
        sleepDao.deleteForDate(date)
    }

    override suspend fun deleteExerciseForDate(date: LocalDate) {
        exerciseDao.deleteForDate(date)
    }

    override suspend fun deleteSmokingForDate(date: LocalDate) {
        smokingDao.deleteForDate(date)
    }

    override suspend fun deleteSupplementDoseForDate(templateId: Long, date: LocalDate) {
        doseDao.deleteForTemplateAndDate(templateId, date)
    }

    override suspend fun deleteStepsForDate(date: LocalDate) {
        stepDao.deleteForDate(date)
    }

    override suspend fun deleteBodyMeasurement(id: Long) {
        measurementDao.deleteById(id)
    }

    override suspend fun addHydration(amountMl: Int, date: LocalDate) {
        if (amountMl <= 0) return
        hydrationDao.upsert(
            HydrationEntryEntity(
                date = date,
                amountMl = amountMl,
            ),
        )
    }

    override suspend fun saveSleepSession(session: SleepSession) {
        val existing = sleepDao.getForDate(session.sessionDate)
        sleepDao.upsert(
            session.copy(id = existing?.id ?: session.id).toEntity(),
        )
    }

    override suspend fun saveExerciseEntry(entry: ExerciseEntry, date: LocalDate) {
        val existing = exerciseDao.getForDate(date)
        exerciseDao.upsert(
            entry.copy(
                id = existing?.id ?: entry.id,
                date = date,
            ).toEntity(),
        )
    }

    override suspend fun saveSmokingCount(count: Int, date: LocalDate) {
        val existing = smokingDao.getForDate(date)
        smokingDao.upsert(
            SmokingEntryEntity(
                id = existing?.id ?: 0,
                date = date,
                count = count.coerceAtLeast(0),
            ),
        )
    }

    override suspend fun incrementSmokingCount(date: LocalDate, delta: Int) {
        val currentCount = smokingDao.getForDate(date)?.count ?: 0
        saveSmokingCount(
            count = currentCount + delta,
            date = date,
        )
    }

    override suspend fun saveSupplementDoseEntries(
        entries: List<SupplementDoseEntry>,
        date: LocalDate,
    ) {
        val sanitizedEntries = entries
            .filter { it.amount > 0f }
            .map { entry ->
                entry.copy(date = date).toEntity()
            }
        doseDao.replaceForDate(date, sanitizedEntries)
    }

    override suspend fun saveBodyMeasurement(entry: BodyMeasurementEntry) {
        val existing = measurementDao.getForDate(entry.date)
        measurementDao.upsert(
            entry.copy(
                id = existing?.id ?: entry.id,
                recordedAt = LocalDateTime.now(),
            ).toEntity(),
        )
    }

    override suspend fun saveWeightMeasurement(weightKg: Float, date: LocalDate) {
        val sameDateMeasurement = measurementDao.getForDate(date)
        val sourceMeasurement = sameDateMeasurement
            ?: measurementDao.getLatestOnOrBefore(date)
            ?: settingsRepository.settings.first().goalSettings.let { goals ->
                BodyMeasurementEntity(
                    date = date,
                    weightKg = goals.baselineWeightKg,
                    shoulderCm = goals.baselineShoulderCm,
                    waistCm = goals.baselineWaistCm,
                    hipCm = goals.baselineHipCm,
                )
            }

        measurementDao.upsert(
            BodyMeasurementEntity(
                id = sameDateMeasurement?.id ?: 0,
                date = date,
                weightKg = weightKg,
                shoulderCm = sourceMeasurement.shoulderCm,
                waistCm = sourceMeasurement.waistCm,
                hipCm = sourceMeasurement.hipCm,
                recordedAt = LocalDateTime.now(),
            ),
        )
    }

    override suspend fun recordStepSensorValue(sensorValue: Int, date: LocalDate) {
        if (sensorValue < 0) return
        val existing = stepDao.getForDate(date)
        val latest = stepDao.getLatest()
        val updatedAt = LocalDateTime.now()

        val updated = when {
            existing == null -> {
                val previousSensorValue = latest
                    ?.takeIf { it.date.isBefore(date) }
                    ?.lastSensorValue
                    ?.takeIf { previous -> sensorValue >= previous }
                StepEntryEntity(
                    date = date,
                    steps = previousSensorValue?.let { previous -> sensorValue - previous } ?: 0,
                    sensorBaseline = previousSensorValue ?: sensorValue,
                    lastSensorValue = sensorValue,
                    updatedAt = updatedAt,
                )
            }

            existing.lastSensorValue == null || sensorValue < existing.lastSensorValue -> {
                existing.copy(
                    sensorBaseline = sensorValue,
                    lastSensorValue = sensorValue,
                    updatedAt = updatedAt,
                )
            }

            else -> {
                existing.copy(
                    steps = (existing.steps + (sensorValue - existing.lastSensorValue)).coerceAtLeast(0),
                    lastSensorValue = sensorValue,
                    updatedAt = updatedAt,
                )
            }
        }

        stepDao.upsert(updated)
    }
}

class DefaultTrendsRepository(
    private val settingsRepository: SettingsRepository,
    private val mealDao: MealDao,
    private val hydrationDao: HydrationDao,
    private val sleepDao: SleepDao,
    private val stepDao: StepDao,
    private val measurementDao: BodyMeasurementDao,
) : TrendsRepository {
    override fun observeTrends(period: TrendsPeriod, endDate: LocalDate): Flow<TrendsSnapshot> {
        val dataDays = if (period == TrendsPeriod.WEEKLY) {
            buildWeekToDateDays(endDate)
        } else {
            buildMonthToDateDays(endDate)
        }
        val startDate = dataDays.first()
        val finalDate = endDate
        val weeklyDays = buildCalendarWeekDays(endDate)
        val weightMeasurements = combine(
            measurementDao.observeBetween(startDate, finalDate),
            measurementDao.observeLatestOnOrBefore(startDate),
            measurementDao.observeEarliestOnOrAfter(finalDate),
            measurementDao.observeEarliest(),
        ) { inWindow, beforeBoundary, afterBoundary, earliestMeasurement ->
            WeightChartContext(
                measurements = (listOfNotNull(beforeBoundary) + inWindow + listOfNotNull(afterBoundary))
                    .groupBy(BodyMeasurementEntity::date)
                    .mapNotNull { (_, entries) ->
                        entries.maxByOrNull(BodyMeasurementEntity::recordedAt)
                    }
                    .sortedBy(BodyMeasurementEntity::date),
                earliestMeasurementDate = earliestMeasurement?.date,
            )
        }

        val stepAndWeight = combine(
            stepDao.observeBetween(startDate, finalDate),
            weightMeasurements,
        ) { stepEntities, weightContext ->
            stepEntities to weightContext
        }

        return combine(
            settingsRepository.settings,
            mealDao.observeBetween(startDate, finalDate),
            hydrationDao.observeBetween(startDate, finalDate),
            sleepDao.observeBetween(startDate, finalDate),
            stepAndWeight,
        ) { settings, mealEntities, hydrationEntities, sleepEntities, stepWeightContext ->
            val (stepEntities, weightContext) = stepWeightContext
            val meals = mealEntities.map(MealEntryEntity::toDomain)
            val hydration = hydrationEntities.map(HydrationEntryEntity::toDomain)
            val sleeps = sleepEntities.map(SleepSessionEntity::toDomain)
            val steps = stepEntities.map(StepEntryEntity::toDomain)
            val weightMeasurementsForChart = weightContext.measurements.map { measurement ->
                WeightMeasurementSample(
                    date = measurement.date,
                    weightKg = measurement.weightKg,
                )
            }
            val clippedWeightDays = clipWeightTrendDays(
                days = dataDays,
                earliestMeasurementDate = weightContext.earliestMeasurementDate,
            )

            TrendsSnapshot(
                period = period,
                averageProteinGrams = averageProtein(meals, dataDays),
                averageSleepMinutes = averageSleepMinutes(sleeps, dataDays),
                averageWaterMl = averageWaterMl(hydration, dataDays),
                averageSteps = averageSteps(steps, dataDays),
                averageCalories = averageCalories(meals, dataDays),
                weeklyCalories = buildWeeklyCalories(
                    entries = meals,
                    days = weeklyDays,
                    targetCalories = settings.goalSettings.dailyCaloriesTarget,
                ),
                weightPoints = buildInterpolatedWeightTrendPoints(
                    days = clippedWeightDays,
                    measurements = weightMeasurementsForChart,
                ),
                stepPoints = buildStepTrendPoints(
                    entries = steps,
                    days = dataDays,
                ),
            )
        }
    }
}

private data class BaseTodaySnapshot(
    val settings: SettingsState,
    val meals: List<MealEntry>,
    val hydrationEntries: List<HydrationEntry>,
    val sleepSessionForDate: SleepSession?,
    val exerciseEntryForDate: ExerciseEntry?,
    val weekExerciseEntries: List<ExerciseEntry>,
    val smokingEntryForDate: SmokingEntry?,
    val stepEntryForDate: StepEntry?,
    val weekStepEntries: List<StepEntry>,
)

private data class SecondaryTodaySnapshot(
    val exerciseEntryForDate: ExerciseEntry?,
    val weekExerciseEntries: List<ExerciseEntry>,
    val smokingEntryForDate: SmokingEntry?,
    val stepEntryForDate: StepEntry?,
    val weekStepEntries: List<StepEntry>,
)

private data class WeightChartContext(
    val measurements: List<BodyMeasurementEntity>,
    val earliestMeasurementDate: LocalDate?,
)

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

private fun MealEntryEntity.toDomain(): MealEntry {
    return MealEntry(
        id = id,
        date = date,
        mealType = MealType.valueOf(mealType),
        name = name,
        calories = calories,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        proteinGrams = proteinGrams,
        createdAt = createdAt,
    )
}

private fun MealEntry.toEntity(): MealEntryEntity {
    return MealEntryEntity(
        id = id,
        date = date,
        mealType = mealType.name,
        name = name,
        calories = calories,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams,
        proteinGrams = proteinGrams,
        createdAt = createdAt,
    )
}

private fun HydrationEntryEntity.toDomain(): HydrationEntry {
    return HydrationEntry(
        id = id,
        date = date,
        amountMl = amountMl,
        createdAt = createdAt,
    )
}

private fun SleepSessionEntity.toDomain(): SleepSession {
    return SleepSession(
        id = id,
        startTime = startTime,
        endTime = endTime,
    )
}

private fun SleepSession.toEntity(): SleepSessionEntity {
    return SleepSessionEntity(
        id = id,
        sessionDate = sessionDate,
        startTime = startTime,
        endTime = endTime,
    )
}

private fun ExerciseEntryEntity.toDomain(): ExerciseEntry {
    return ExerciseEntry(
        id = id,
        date = date,
        type = ExerciseType.valueOf(type),
        durationMinutes = durationMinutes,
        intensity = ExerciseIntensity.valueOf(intensity),
    )
}

private fun ExerciseEntry.toEntity(): ExerciseEntryEntity {
    return ExerciseEntryEntity(
        id = id,
        date = date,
        type = type.name,
        durationMinutes = durationMinutes,
        intensity = intensity.name,
    )
}

private fun SmokingEntryEntity.toDomain(): SmokingEntry {
    return SmokingEntry(
        id = id,
        date = date,
        count = count,
    )
}

private fun StepEntryEntity.toDomain(): StepEntry {
    return StepEntry(
        id = id,
        date = date,
        steps = steps,
        sensorBaseline = sensorBaseline,
        lastSensorValue = lastSensorValue,
        updatedAt = updatedAt,
    )
}

private fun SupplementTemplateEntity.toDomain(): SupplementTemplate {
    return SupplementTemplate(
        id = id,
        name = name,
        targetAmount = targetAmount,
        unitLabel = unitLabel,
        isActive = isActive,
        sortOrder = sortOrder,
    )
}

private fun SupplementDoseEntryEntity.toDomain(): SupplementDoseEntry {
    return SupplementDoseEntry(
        id = id,
        templateId = templateId,
        date = date,
        amount = amount,
        loggedAt = loggedAt,
    )
}

private fun SupplementDoseEntry.toEntity(): SupplementDoseEntryEntity {
    return SupplementDoseEntryEntity(
        id = id,
        templateId = templateId,
        date = date,
        amount = amount,
        loggedAt = loggedAt,
    )
}

private fun BodyMeasurementEntity.toDomain(): BodyMeasurementEntry {
    return BodyMeasurementEntry(
        id = id,
        date = date,
        weightKg = weightKg,
        shoulderCm = shoulderCm,
        waistCm = waistCm,
        hipCm = hipCm,
        recordedAt = recordedAt,
    )
}

private fun BodyMeasurementEntry.toEntity(): BodyMeasurementEntity {
    return BodyMeasurementEntity(
        id = id,
        date = date,
        weightKg = weightKg,
        shoulderCm = shoulderCm,
        waistCm = waistCm,
        hipCm = hipCm,
        recordedAt = recordedAt,
    )
}

private val DEFAULT_SUPPLEMENT_NAMES = listOf(
    "B12",
    "D3 Vitamini",
    "Demir",
    "Omega 3",
    "Magnezyum",
)

private fun createSupplementTemplatesFromNames(names: List<String>): List<SupplementTemplate> {
    return names.mapIndexed { index, name ->
        val trimmedName = name.trim()
        val preset = DEFAULT_SUPPLEMENT_PRESETS[trimmedName.lowercase()]
        SupplementTemplate(
            name = trimmedName,
            targetAmount = preset?.first ?: 1f,
            unitLabel = preset?.second ?: "doz",
            sortOrder = index,
        )
    }
}

private val DEFAULT_SUPPLEMENT_PRESETS = mapOf(
    "b12" to (500f to "mcg"),
    "d3 vitamini" to (25f to "mcg"),
    "demir" to (18f to "mg"),
    "omega 3" to (1000f to "mg"),
    "magnezyum" to (200f to "mg"),
)

private fun deriveSleepScheduleFromLegacy(totalMinutes: Int): Pair<LocalTime, LocalTime> {
    val bedtime = LocalTime.of(23, 0)
    val wakeTime = bedtime.plusMinutes(totalMinutes.coerceAtLeast(0).toLong())
    return bedtime to wakeTime
}
