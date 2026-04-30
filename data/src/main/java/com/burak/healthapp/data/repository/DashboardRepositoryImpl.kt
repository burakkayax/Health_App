package com.burak.healthapp.data.repository

import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.CaffeineDao
import com.burak.healthapp.data.local.dao.ExerciseDao
import com.burak.healthapp.data.local.dao.HydrationDao
import com.burak.healthapp.data.local.dao.MealDao
import com.burak.healthapp.data.local.dao.SleepDao
import com.burak.healthapp.data.local.dao.SmokingDao
import com.burak.healthapp.data.local.dao.StepDao
import com.burak.healthapp.data.local.dao.SupplementDoseDao
import com.burak.healthapp.data.local.dao.SupplementTemplateDao
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.CaffeineEntryEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
import com.burak.healthapp.data.local.mapper.toDomain
import com.burak.healthapp.data.local.mapper.toEntity
import com.burak.healthapp.domain.calculation.buildWeekToDateDays
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.TodaySnapshot
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime

class DashboardRepositoryImpl(
    private val settingsRepository: SettingsRepository,
    private val mealDao: MealDao,
    private val hydrationDao: HydrationDao,
    private val sleepDao: SleepDao,
    private val exerciseDao: ExerciseDao,
    private val smokingDao: SmokingDao,
    private val stepDao: StepDao,
    private val caffeineDao: CaffeineDao,
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
            caffeineDao.observeForDate(date),
        ) { base, templates, doses, measurement, caffeine ->
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
                caffeineEntries = caffeine.map(CaffeineEntryEntity::toDomain),
                supplementTemplates = templates.map(SupplementTemplateEntity::toDomain),
                supplementDoseEntries = doses.map(SupplementDoseEntryEntity::toDomain),
                measurementForDate = measurement?.toDomain(),
            )
        }
    }

    override fun observeMealsForDate(date: LocalDate): Flow<List<MealEntry>> = mealDao.observeForDate(date).map { entries ->
        entries.map(MealEntryEntity::toDomain)
    }

    override fun observeHydrationBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<HydrationEntry>> = hydrationDao.observeBetween(startDate, endDate).map { entries ->
        entries.map(HydrationEntryEntity::toDomain)
    }

    override fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?> = measurementDao.observeLatest().map { measurement ->
        measurement?.toDomain()
    }

    override fun observeWeightHistory(): Flow<List<BodyMeasurementEntry>> = measurementDao.observeAll().map { measurements ->
        measurements.map(BodyMeasurementEntity::toDomain)
    }

    override fun observeSleepSessionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSession>> = sleepDao.observeBetween(startDate, endDate).map { sessions ->
        sessions.map(SleepSessionEntity::toDomain)
    }

    override fun observeStepsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntry>> = stepDao.observeBetween(startDate, endDate).map { entries ->
        entries.map(StepEntryEntity::toDomain)
    }

    override fun observeCaffeineForDate(date: LocalDate): Flow<List<CaffeineEntry>> = caffeineDao.observeForDate(date).map { entries ->
        entries.map(CaffeineEntryEntity::toDomain)
    }

    override fun observeCaffeineBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<CaffeineEntry>> = caffeineDao.observeBetween(startDate, endDate).map { entries ->
        entries.map(CaffeineEntryEntity::toDomain)
    }

    override fun observeSmokingBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SmokingEntry>> = smokingDao.observeBetween(startDate, endDate).map { entries ->
        entries.map(SmokingEntryEntity::toDomain)
    }

    override fun observeExerciseBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<ExerciseEntry>> = exerciseDao.observeBetween(startDate, endDate).map { entries ->
        entries.map(ExerciseEntryEntity::toDomain)
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

    override suspend fun addCaffeine(entry: CaffeineEntry) {
        if (entry.estimatedMg <= 0) return
        caffeineDao.upsert(entry.toEntity())
    }

    override suspend fun deleteCaffeine(id: Long) {
        caffeineDao.deleteById(id)
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

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)
