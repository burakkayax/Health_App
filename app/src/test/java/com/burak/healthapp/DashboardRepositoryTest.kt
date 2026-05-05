package com.burak.healthapp

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
import com.burak.healthapp.data.repository.DashboardRepositoryImpl
import com.burak.healthapp.data.repository.TrendsRepositoryImpl
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class DashboardRepositoryTest {
    @Test
    fun observeMealsForDate_requestsSelectedDateAndMapsEntities() = runTest {
        val targetDate = LocalDate.of(2026, 4, 19)
        val mealDao = FakeMealDao(
            initialEntries = listOf(
                MealEntryEntity(
                    id = 7,
                    date = targetDate,
                    mealType = MealType.LUNCH.name,
                    name = "Tavuk Bowl",
                    calories = 620,
                    carbsGrams = 48,
                    fatGrams = 18,
                    proteinGrams = 42,
                    createdAt = LocalDateTime.of(2026, 4, 19, 12, 30),
                ),
                MealEntryEntity(
                    id = 8,
                    date = targetDate.minusDays(1),
                    mealType = MealType.BREAKFAST.name,
                    name = "Yulaf",
                    calories = 330,
                    carbsGrams = 40,
                    fatGrams = 8,
                    proteinGrams = 14,
                    createdAt = LocalDateTime.of(2026, 4, 18, 8, 0),
                ),
            ),
        )
        val repository = createRepository(
            mealDao = mealDao,
        )

        val meals = repository.observeMealsForDate(targetDate).first()

        assertEquals(targetDate, mealDao.requestedDate)
        assertEquals(1, meals.size)
        assertEquals("Tavuk Bowl", meals.first().name)
    }

    @Test
    fun observeToday_returnsExactDateMeasurementAndSleep() = runTest {
        val targetDate = LocalDate.of(2026, 4, 19)
        val repository = createRepository(
            mealDao = FakeMealDao(
                initialEntries = listOf(
                    MealEntryEntity(
                        id = 1,
                        date = targetDate,
                        mealType = MealType.BREAKFAST.name,
                        name = "Omlet",
                        calories = 400,
                        carbsGrams = 12,
                        fatGrams = 24,
                        proteinGrams = 28,
                    ),
                ),
            ),
            hydrationDao = FakeHydrationDao(
                initialEntries = listOf(
                    HydrationEntryEntity(date = targetDate, amountMl = 500),
                    HydrationEntryEntity(date = targetDate.minusDays(1), amountMl = 800),
                ),
            ),
            sleepDao = FakeSleepDao(
                initialEntries = listOf(
                    SleepSessionEntity(
                        id = 4,
                        sessionDate = targetDate,
                        startTime = targetDate.minusDays(1).atTime(23, 30),
                        endTime = targetDate.atTime(7, 30),
                    ),
                    SleepSessionEntity(
                        id = 5,
                        sessionDate = targetDate.minusDays(1),
                        startTime = targetDate.minusDays(2).atTime(23, 0),
                        endTime = targetDate.minusDays(1).atTime(7, 0),
                    ),
                ),
            ),
            templateDao = FakeSupplementTemplateDao(
                initialTemplates = listOf(
                    SupplementTemplateEntity(id = 3, name = "B12", targetAmount = 500f, unitLabel = "mcg"),
                ),
            ),
            doseDao = FakeSupplementDoseDao(
                initialEntries = listOf(
                    SupplementDoseEntryEntity(id = 9, templateId = 3, date = targetDate, amount = 500f),
                ),
            ),
            measurementDao = FakeBodyMeasurementDao(
                initialMeasurements = listOf(
                    BodyMeasurementEntity(
                        id = 2,
                        date = targetDate,
                        weightKg = 76.8f,
                        shoulderCm = 118f,
                        waistCm = 86f,
                        hipCm = 98f,
                        recordedAt = targetDate.atTime(9, 0),
                    ),
                    BodyMeasurementEntity(
                        id = 3,
                        date = targetDate.plusDays(1),
                        weightKg = 75.9f,
                        shoulderCm = 121f,
                        waistCm = 84f,
                        hipCm = 97f,
                        recordedAt = targetDate.plusDays(1).atTime(9, 0),
                    ),
                ),
            ),
        )

        val snapshot = repository.observeToday(targetDate).first()

        assertEquals(targetDate, snapshot.sleepSessionForDate?.sessionDate)
        assertEquals(targetDate, snapshot.measurementForDate?.date)
        assertEquals(1, snapshot.meals.size)
        assertEquals(1, snapshot.supplementDoseEntries.size)
    }

    @Test
    fun saveWeightMeasurement_reusesNearestPastDimensions_notFutureMeasurement() = runTest {
        val date = LocalDate.of(2026, 4, 19)
        val measurementDao = FakeBodyMeasurementDao(
            initialMeasurements = listOf(
                BodyMeasurementEntity(
                    id = 4,
                    date = date.minusDays(1),
                    weightKg = 78f,
                    shoulderCm = 118f,
                    waistCm = 88f,
                    hipCm = 99f,
                    recordedAt = LocalDateTime.of(2026, 4, 18, 9, 0),
                ),
                BodyMeasurementEntity(
                    id = 5,
                    date = date.plusDays(1),
                    weightKg = 75f,
                    shoulderCm = 130f,
                    waistCm = 70f,
                    hipCm = 90f,
                    recordedAt = LocalDateTime.of(2026, 4, 20, 9, 0),
                ),
            ),
        )
        val repository = createRepository(
            measurementDao = measurementDao,
        )

        repository.saveWeightMeasurement(76.5f, date)

        val latestForDate = measurementDao.getForDate(date)
        assertEquals(date, latestForDate?.date)
        assertEquals(76.5f, latestForDate?.weightKg)
        assertEquals(118f, latestForDate?.shoulderCm)
        assertEquals(88f, latestForDate?.waistCm)
        assertEquals(99f, latestForDate?.hipCm)
    }

    @Test
    fun observeWeightHistory_returnsChronologicalMeasurements() = runTest {
        val repository = createRepository(
            measurementDao = FakeBodyMeasurementDao(
                initialMeasurements = listOf(
                    BodyMeasurementEntity(
                        id = 2,
                        date = LocalDate.of(2026, 4, 19),
                        weightKg = 77.4f,
                        shoulderCm = 119f,
                        waistCm = 86f,
                        hipCm = 98f,
                        recordedAt = LocalDateTime.of(2026, 4, 19, 9, 0),
                    ),
                    BodyMeasurementEntity(
                        id = 1,
                        date = LocalDate.of(2026, 4, 18),
                        weightKg = 77.8f,
                        shoulderCm = 119f,
                        waistCm = 87f,
                        hipCm = 98f,
                        recordedAt = LocalDateTime.of(2026, 4, 18, 9, 0),
                    ),
                ),
            ),
        )

        val history = repository.observeWeightHistory().first()

        assertEquals(listOf(1L, 2L), history.map { it.id })
    }

    @Test
    fun deleteBodyMeasurement_removesMeasurement() = runTest {
        val measurementDao = FakeBodyMeasurementDao(
            initialMeasurements = listOf(
                BodyMeasurementEntity(
                    id = 1,
                    date = LocalDate.of(2026, 4, 18),
                    weightKg = 77.8f,
                    shoulderCm = 119f,
                    waistCm = 87f,
                    hipCm = 98f,
                    recordedAt = LocalDateTime.of(2026, 4, 18, 9, 0),
                ),
                BodyMeasurementEntity(
                    id = 2,
                    date = LocalDate.of(2026, 4, 19),
                    weightKg = 77.4f,
                    shoulderCm = 119f,
                    waistCm = 86f,
                    hipCm = 98f,
                    recordedAt = LocalDateTime.of(2026, 4, 19, 9, 0),
                ),
            ),
        )
        val repository = createRepository(measurementDao = measurementDao)

        repository.deleteBodyMeasurement(1)

        val history = repository.observeWeightHistory().first()
        assertEquals(listOf(2L), history.map { it.id })
    }

    @Test
    fun saveSupplementDoseEntries_replacesExistingDateEntries() = runTest {
        val date = LocalDate.of(2026, 4, 19)
        val doseDao = FakeSupplementDoseDao(
            initialEntries = listOf(
                SupplementDoseEntryEntity(
                    id = 1,
                    templateId = 1,
                    date = date,
                    amount = 100f,
                    loggedAt = LocalDateTime.of(2026, 4, 19, 9, 0),
                ),
            ),
        )
        val repository = createRepository(doseDao = doseDao)

        repository.saveSupplementDoseEntries(
            entries = listOf(
                SupplementDoseEntry(
                    templateId = 1,
                    date = date,
                    amount = 150f,
                ),
            ),
            date = date,
        )

        val entries = doseDao.observeForDate(date).first()
        assertEquals(1, entries.size)
        assertEquals(150f, entries.first().amount)
    }

    @Test
    fun recordStepSensorValue_accumulatesAndSurvivesSensorReset() = runTest {
        val date = LocalDate.of(2026, 4, 19)
        val stepDao = FakeStepDao()
        val repository = createRepository(stepDao = stepDao)

        repository.recordStepSensorValue(sensorValue = 100, date = date)
        repository.recordStepSensorValue(sensorValue = 150, date = date)
        repository.recordStepSensorValue(sensorValue = 20, date = date)
        repository.recordStepSensorValue(sensorValue = 45, date = date)

        val entry = stepDao.getForDate(date)
        assertEquals(75, entry?.steps)
        assertEquals(20, entry?.sensorBaseline)
        assertEquals(45, entry?.lastSensorValue)
    }

    @Test
    fun deleteHydrationEntry_removesSingleWaterEntry() = runTest {
        val date = LocalDate.of(2026, 4, 19)
        val hydrationDao = FakeHydrationDao(
            initialEntries = listOf(
                HydrationEntryEntity(id = 1, date = date, amountMl = 200),
                HydrationEntryEntity(id = 2, date = date, amountMl = 500),
            ),
        )
        val repository = createRepository(hydrationDao = hydrationDao)

        repository.deleteHydrationEntry(1)

        val entries = hydrationDao.observeForDate(date).first()
        assertEquals(listOf(2L), entries.map { it.id })
    }

    @Test
    fun observeTrends_includesStepAverageAndTrendPoints() = runTest {
        val anchorDate = LocalDate.of(2026, 4, 22)
        val repository = TrendsRepositoryImpl(
            settingsRepository = FakeSettingsRepository(),
            mealDao = FakeMealDao(),
            hydrationDao = FakeHydrationDao(),
            sleepDao = FakeSleepDao(),
            stepDao = FakeStepDao(
                initialEntries = listOf(
                    StepEntryEntity(date = LocalDate.of(2026, 4, 20), steps = 4000, sensorBaseline = 100, lastSensorValue = 4100),
                    StepEntryEntity(date = LocalDate.of(2026, 4, 22), steps = 8000, sensorBaseline = 4100, lastSensorValue = 12100),
                ),
            ),
            measurementDao = FakeBodyMeasurementDao(),
            caffeineDao = FakeCaffeineDao(),
            smokingDao = FakeSmokingDao(),
            exerciseDao = FakeExerciseDao(),
        )

        val snapshot = repository.observeTrends(
            period = TrendsPeriod.WEEKLY,
            endDate = anchorDate,
        ).first()

        assertEquals(6000f, snapshot.averageSteps, 0.001f)
        assertEquals(listOf(0f, 0f, 0f, 0f, 4000f, 0f, 8000f), snapshot.stepPoints.map { it.value })
    }

    @Test
    fun observeTrends_interpolatesWeightPointsWithoutZeroValues() = runTest {
        val anchorDate = LocalDate.of(2026, 4, 22)
        val repository = TrendsRepositoryImpl(
            settingsRepository = FakeSettingsRepository(),
            mealDao = FakeMealDao(),
            hydrationDao = FakeHydrationDao(),
            sleepDao = FakeSleepDao(),
            stepDao = FakeStepDao(),
            caffeineDao = FakeCaffeineDao(),
            smokingDao = FakeSmokingDao(),
            exerciseDao = FakeExerciseDao(),
            measurementDao = FakeBodyMeasurementDao(
                initialMeasurements = listOf(
                    BodyMeasurementEntity(
                        id = 1,
                        date = LocalDate.of(2026, 4, 19),
                        weightKg = 70f,
                        shoulderCm = 118f,
                        waistCm = 88f,
                        hipCm = 99f,
                        recordedAt = LocalDateTime.of(2026, 4, 19, 8, 0),
                    ),
                    BodyMeasurementEntity(
                        id = 2,
                        date = LocalDate.of(2026, 4, 22),
                        weightKg = 73f,
                        shoulderCm = 118f,
                        waistCm = 88f,
                        hipCm = 99f,
                        recordedAt = LocalDateTime.of(2026, 4, 22, 8, 0),
                    ),
                    BodyMeasurementEntity(
                        id = 3,
                        date = LocalDate.of(2026, 4, 27),
                        weightKg = 78f,
                        shoulderCm = 118f,
                        waistCm = 88f,
                        hipCm = 99f,
                        recordedAt = LocalDateTime.of(2026, 4, 27, 8, 0),
                    ),
                ),
            ),
        )

        val snapshot = repository.observeTrends(
            period = TrendsPeriod.WEEKLY,
            endDate = anchorDate,
        ).first()

        assertEquals(listOf(70f, 71f, 72f, 73f), snapshot.weightPoints.map { it.value })
    }

    @Test
    fun observeTrends_clipsWeightPointsUntilFirstMeasurementInWindow() = runTest {
        val anchorDate = LocalDate.of(2026, 4, 22)
        val repository = TrendsRepositoryImpl(
            settingsRepository = FakeSettingsRepository(),
            mealDao = FakeMealDao(),
            hydrationDao = FakeHydrationDao(),
            sleepDao = FakeSleepDao(),
            stepDao = FakeStepDao(),
            caffeineDao = FakeCaffeineDao(),
            smokingDao = FakeSmokingDao(),
            exerciseDao = FakeExerciseDao(),
            measurementDao = FakeBodyMeasurementDao(
                initialMeasurements = listOf(
                    BodyMeasurementEntity(
                        id = 10,
                        date = LocalDate.of(2026, 4, 22),
                        weightKg = 73f,
                        shoulderCm = 118f,
                        waistCm = 88f,
                        hipCm = 99f,
                        recordedAt = LocalDateTime.of(2026, 4, 22, 8, 0),
                    ),
                ),
            ),
        )

        val snapshot = repository.observeTrends(
            period = TrendsPeriod.WEEKLY,
            endDate = anchorDate,
        ).first()

        assertEquals(1, snapshot.weightPoints.size)
        assertEquals(73f, snapshot.weightPoints.single().value)
    }

    @Test
    fun observeTrends_usesMonthToDateForMonthlyAggregates() = runTest {
        val repository = TrendsRepositoryImpl(
            settingsRepository = FakeSettingsRepository(),
            mealDao = FakeMealDao(
                initialEntries = listOf(
                    MealEntryEntity(
                        id = 20,
                        date = LocalDate.of(2026, 4, 1),
                        mealType = MealType.BREAKFAST.name,
                        name = "Yulaf",
                        calories = 1800,
                        carbsGrams = 120,
                        fatGrams = 30,
                        proteinGrams = 80,
                    ),
                    MealEntryEntity(
                        id = 21,
                        date = LocalDate.of(2026, 4, 19),
                        mealType = MealType.DINNER.name,
                        name = "Somon",
                        calories = 2200,
                        carbsGrams = 40,
                        fatGrams = 60,
                        proteinGrams = 100,
                    ),
                ),
            ),
            hydrationDao = FakeHydrationDao(
                initialEntries = listOf(
                    HydrationEntryEntity(date = LocalDate.of(2026, 4, 1), amountMl = 2000),
                    HydrationEntryEntity(date = LocalDate.of(2026, 4, 19), amountMl = 1000),
                ),
            ),
            sleepDao = FakeSleepDao(
                initialEntries = listOf(
                    SleepSessionEntity(
                        id = 30,
                        sessionDate = LocalDate.of(2026, 4, 1),
                        startTime = LocalDate.of(2026, 3, 31).atTime(23, 0),
                        endTime = LocalDate.of(2026, 4, 1).atTime(7, 0),
                    ),
                    SleepSessionEntity(
                        id = 31,
                        sessionDate = LocalDate.of(2026, 4, 19),
                        startTime = LocalDate.of(2026, 4, 18).atTime(23, 30),
                        endTime = LocalDate.of(2026, 4, 19).atTime(7, 30),
                    ),
                ),
            ),
            stepDao = FakeStepDao(),
            caffeineDao = FakeCaffeineDao(),
            smokingDao = FakeSmokingDao(),
            exerciseDao = FakeExerciseDao(),
            measurementDao = FakeBodyMeasurementDao(),
        )

        val snapshot = repository.observeTrends(
            period = TrendsPeriod.MONTHLY,
            endDate = LocalDate.of(2026, 4, 19),
        ).first()

        assertEquals(90f, snapshot.averageProteinGrams, 0.001f)
        assertEquals(1500f, snapshot.averageWaterMl, 0.001f)
        assertEquals(480f, snapshot.averageSleepMinutes, 0.001f)
        assertEquals(2000f, snapshot.averageCalories, 0.001f)
    }

    private fun createRepository(
        mealDao: FakeMealDao = FakeMealDao(),
        hydrationDao: FakeHydrationDao = FakeHydrationDao(),
        sleepDao: FakeSleepDao = FakeSleepDao(),
        exerciseDao: FakeExerciseDao = FakeExerciseDao(),
        smokingDao: FakeSmokingDao = FakeSmokingDao(),
        stepDao: FakeStepDao = FakeStepDao(),
        caffeineDao: FakeCaffeineDao = FakeCaffeineDao(),
        templateDao: FakeSupplementTemplateDao = FakeSupplementTemplateDao(),
        doseDao: FakeSupplementDoseDao = FakeSupplementDoseDao(),
        measurementDao: FakeBodyMeasurementDao = FakeBodyMeasurementDao(),
    ): DashboardRepositoryImpl = DashboardRepositoryImpl(
        settingsRepository = FakeSettingsRepository(),
        mealDao = mealDao,
        hydrationDao = hydrationDao,
        sleepDao = sleepDao,
        exerciseDao = exerciseDao,
        smokingDao = smokingDao,
        stepDao = stepDao,
        caffeineDao = caffeineDao,
        templateDao = templateDao,
        doseDao = doseDao,
        measurementDao = measurementDao,
    )
}

private class FakeSettingsRepository : SettingsRepository {
    override val settings: Flow<SettingsState> = flowOf(
        SettingsState(
            goalSettings = GoalSettings(),
            themeMode = ThemeMode.SYSTEM,
        ),
    )

    override fun observeSupplementTemplates(): Flow<List<SupplementTemplate>> = flowOf(emptyList())

    override suspend fun completeOnboarding(
        profile: UserProfile,
        goals: GoalSettings,
        initialMeasurement: BodyMeasurementEntry,
        supplements: List<String>,
        useDefaultSupplementsWhenEmpty: Boolean,
        dashboardCards: List<com.burak.healthapp.domain.model.DashboardCardConfig>?,
        waterReminderSettings: com.burak.healthapp.domain.model.WaterReminderSettings?,
        stepTrackingEnabled: Boolean?,
    ) = Unit

    override suspend fun updateGoalSettings(goals: GoalSettings) = Unit

    override suspend fun updateWaterReminderSettings(settings: WaterReminderSettings) = Unit

    override suspend fun updateWaterReminderSnoozedDate(date: LocalDate?) = Unit

    override suspend fun updateStepTrackingEnabled(enabled: Boolean) = Unit

    override suspend fun updateDashboardCardVisibility(type: DashboardCardType, isVisible: Boolean) = Unit

    override suspend fun moveDashboardCard(type: DashboardCardType, newIndex: Int) = Unit

    override suspend fun resetDashboardCardsToDefault() = Unit

    override suspend fun updateProfile(profile: UserProfile) = Unit

    override suspend fun updateThemeMode(mode: ThemeMode) = Unit

    override suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>) = Unit
}

private class FakeMealDao(
    initialEntries: List<MealEntryEntity> = emptyList(),
) : MealDao {
    private val entries = MutableStateFlow(initialEntries)
    var requestedDate: LocalDate? = null

    override suspend fun getAll(): List<MealEntryEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<List<MealEntryEntity>> {
        requestedDate = date
        return entries.map { current -> current.filter { it.date == date } }
    }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<MealEntryEntity>> = entries.map { current -> current.filter { it.date in startDate..endDate } }

    override suspend fun upsert(entry: MealEntryEntity) {
        entries.value = entries.value + entry
    }

    override suspend fun deleteById(id: Long) {
        entries.value = entries.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }
}

private class FakeHydrationDao(
    initialEntries: List<HydrationEntryEntity> = emptyList(),
) : HydrationDao {
    private val entries = MutableStateFlow(initialEntries)

    override suspend fun getAll(): List<HydrationEntryEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<List<HydrationEntryEntity>> = entries.map { current -> current.filter { it.date == date } }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<HydrationEntryEntity>> = entries.map { current -> current.filter { it.date in startDate..endDate } }

    override suspend fun upsert(entry: HydrationEntryEntity) {
        entries.value = entries.value + entry
    }

    override suspend fun deleteById(id: Long) {
        entries.value = entries.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }
}

private class FakeSleepDao(
    initialEntries: List<SleepSessionEntity> = emptyList(),
) : SleepDao {
    private val entries = MutableStateFlow(initialEntries)

    override suspend fun getAll(): List<SleepSessionEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<SleepSessionEntity?> = entries.map { current ->
        current.filter { it.sessionDate == date }.maxByOrNull { it.endTime }
    }

    override fun observeLatest(): Flow<SleepSessionEntity?> = entries.map { current -> current.maxByOrNull { it.endTime } }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSessionEntity>> = entries.map { current -> current.filter { it.sessionDate in startDate..endDate } }

    override suspend fun getForDate(date: LocalDate): SleepSessionEntity? = entries.value.filter { it.sessionDate == date }.maxByOrNull { it.endTime }

    override suspend fun deleteForDate(date: LocalDate) {
        entries.value = entries.value.filterNot { it.sessionDate == date }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }

    override suspend fun upsert(session: SleepSessionEntity) {
        entries.value = entries.value
            .filterNot { it.id == session.id && session.id != 0L }
            .plus(
                if (session.id == 0L) {
                    session.copy(id = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1)
                } else {
                    session
                },
            )
    }
}

private class FakeExerciseDao(
    initialEntries: List<ExerciseEntryEntity> = emptyList(),
) : ExerciseDao {
    private val entries = MutableStateFlow(initialEntries)

    override suspend fun getAll(): List<ExerciseEntryEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<ExerciseEntryEntity?> = entries.map { current -> current.firstOrNull { it.date == date } }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<ExerciseEntryEntity>> = entries.map { current -> current.filter { it.date in startDate..endDate } }

    override suspend fun getForDate(date: LocalDate): ExerciseEntryEntity? = entries.value.firstOrNull { it.date == date }

    override suspend fun upsert(entry: ExerciseEntryEntity) {
        entries.value = entries.value
            .filterNot { it.date == entry.date || (entry.id != 0L && it.id == entry.id) }
            .plus(
                if (entry.id == 0L) {
                    entry.copy(id = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1)
                } else {
                    entry
                },
            )
    }

    override suspend fun deleteForDate(date: LocalDate) {
        entries.value = entries.value.filterNot { it.date == date }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }
}

private class FakeSmokingDao(
    initialEntries: List<SmokingEntryEntity> = emptyList(),
) : SmokingDao {
    private val entries = MutableStateFlow(initialEntries)

    override suspend fun getAll(): List<SmokingEntryEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<SmokingEntryEntity?> = entries.map { current -> current.firstOrNull { it.date == date } }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SmokingEntryEntity>> = entries.map { current -> current.filter { it.date in startDate..endDate } }

    override suspend fun getForDate(date: LocalDate): SmokingEntryEntity? = entries.value.firstOrNull { it.date == date }

    override suspend fun upsert(entry: SmokingEntryEntity) {
        entries.value = entries.value
            .filterNot { it.date == entry.date || (entry.id != 0L && it.id == entry.id) }
            .plus(
                if (entry.id == 0L) {
                    entry.copy(id = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1)
                } else {
                    entry
                },
            )
    }

    override suspend fun deleteForDate(date: LocalDate) {
        entries.value = entries.value.filterNot { it.date == date }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }
}

private class FakeStepDao(
    initialEntries: List<StepEntryEntity> = emptyList(),
) : StepDao {
    private val entries = MutableStateFlow(initialEntries)

    override suspend fun getAll(): List<StepEntryEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<StepEntryEntity?> = entries.map { current -> current.firstOrNull { it.date == date } }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntryEntity>> = entries.map { current -> current.filter { it.date in startDate..endDate } }

    override suspend fun getForDate(date: LocalDate): StepEntryEntity? = entries.value.firstOrNull { it.date == date }

    override suspend fun getLatest(): StepEntryEntity? = entries.value.maxWithOrNull(compareBy<StepEntryEntity> { it.date }.thenBy { it.updatedAt })

    override suspend fun deleteForDate(date: LocalDate) {
        entries.value = entries.value.filterNot { it.date == date }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }

    override suspend fun upsert(entry: StepEntryEntity) {
        entries.value = entries.value
            .filterNot { it.date == entry.date || (entry.id != 0L && it.id == entry.id) }
            .plus(
                if (entry.id == 0L) {
                    entry.copy(id = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1)
                } else {
                    entry
                },
            )
    }
}

private class FakeCaffeineDao(
    initialEntries: List<CaffeineEntryEntity> = emptyList(),
) : CaffeineDao {
    private val entries = MutableStateFlow(initialEntries)

    override suspend fun getAll(): List<CaffeineEntryEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<List<CaffeineEntryEntity>> = entries.map { current -> current.filter { it.date == date } }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<CaffeineEntryEntity>> = entries.map { current -> current.filter { it.date in startDate..endDate } }

    override suspend fun upsert(entry: CaffeineEntryEntity) {
        entries.value = entries.value
            .filterNot { entry.id != 0L && it.id == entry.id }
            .plus(
                if (entry.id == 0L) {
                    entry.copy(id = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1)
                } else {
                    entry
                },
            )
    }

    override suspend fun deleteById(id: Long) {
        entries.value = entries.value.filterNot { it.id == id }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }
}

private class FakeSupplementTemplateDao(
    initialTemplates: List<SupplementTemplateEntity> = emptyList(),
) : SupplementTemplateDao {
    private val templates = MutableStateFlow(initialTemplates)

    override fun observeActive(): Flow<List<SupplementTemplateEntity>> = templates

    override suspend fun getAll(): List<SupplementTemplateEntity> = templates.value

    override suspend fun insert(template: SupplementTemplateEntity): Long {
        val inserted = template.copy(id = (templates.value.maxOfOrNull { it.id } ?: 0L) + 1)
        templates.value = templates.value + inserted
        return inserted.id
    }

    override suspend fun upsertAll(templates: List<SupplementTemplateEntity>) {
        this.templates.value = templates
    }

    override suspend fun deactivate(ids: List<Long>) {
        templates.value = templates.value.map { template ->
            if (template.id in ids) template.copy(isActive = false) else template
        }
    }

    override suspend fun deleteAll() {
        templates.value = emptyList()
    }
}

private class FakeSupplementDoseDao(
    initialEntries: List<SupplementDoseEntryEntity> = emptyList(),
) : SupplementDoseDao {
    private val entries = MutableStateFlow(initialEntries)

    override suspend fun getAll(): List<SupplementDoseEntryEntity> = entries.value

    override fun observeForDate(date: LocalDate): Flow<List<SupplementDoseEntryEntity>> = entries.map { current -> current.filter { it.date == date } }

    override suspend fun upsertAll(entries: List<SupplementDoseEntryEntity>) {
        this.entries.value = this.entries.value + entries
    }

    override suspend fun deleteForDate(date: LocalDate) {
        entries.value = entries.value.filterNot { it.date == date }
    }

    override suspend fun deleteForTemplateAndDate(templateId: Long, date: LocalDate) {
        entries.value = entries.value.filterNot { it.templateId == templateId && it.date == date }
    }

    override suspend fun deleteAll() {
        entries.value = emptyList()
    }
}

private class FakeBodyMeasurementDao(
    initialMeasurements: List<BodyMeasurementEntity> = emptyList(),
) : BodyMeasurementDao {
    private val measurements = MutableStateFlow(initialMeasurements)

    override suspend fun getAll(): List<BodyMeasurementEntity> = measurements.value

    override fun observeForDate(date: LocalDate): Flow<BodyMeasurementEntity?> = measurements.map { current ->
        current.filter { it.date == date }.maxByOrNull { it.recordedAt }
    }

    override fun observeLatest(): Flow<BodyMeasurementEntity?> = measurements.map { current -> current.maxByOrNull { it.recordedAt } }

    override fun observeAll(): Flow<List<BodyMeasurementEntity>> = measurements.map { current ->
        current.sortedWith(compareBy<BodyMeasurementEntity> { it.date }.thenBy { it.recordedAt })
    }

    override fun observeEarliest(): Flow<BodyMeasurementEntity?> = measurements.map { current ->
        current.minWithOrNull(compareBy<BodyMeasurementEntity> { it.date }.thenBy { it.recordedAt })
    }

    override suspend fun getLatest(): BodyMeasurementEntity? = measurements.value.maxByOrNull { it.recordedAt }

    override suspend fun getForDate(date: LocalDate): BodyMeasurementEntity? = measurements.value.filter { it.date == date }.maxByOrNull { it.recordedAt }

    override suspend fun getLatestOnOrBefore(date: LocalDate): BodyMeasurementEntity? = measurements.value
        .filter { it.date <= date }
        .maxWithOrNull(compareBy<BodyMeasurementEntity> { it.date }.thenBy { it.recordedAt })

    override fun observeLatestOnOrBefore(date: LocalDate): Flow<BodyMeasurementEntity?> = measurements.map { current ->
        current
            .filter { it.date <= date }
            .maxWithOrNull(compareBy<BodyMeasurementEntity> { it.date }.thenBy { it.recordedAt })
    }

    override fun observeEarliestOnOrAfter(date: LocalDate): Flow<BodyMeasurementEntity?> = measurements.map { current ->
        current
            .filter { it.date >= date }
            .minWithOrNull(compareBy<BodyMeasurementEntity> { it.date }.thenByDescending { it.recordedAt })
    }

    override fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<BodyMeasurementEntity>> = measurements.map { current -> current.filter { it.date in startDate..endDate } }

    override suspend fun deleteById(id: Long) {
        measurements.value = measurements.value.filterNot { it.id == id }
    }

    override suspend fun deleteForDate(date: LocalDate) {
        measurements.value = measurements.value.filterNot { it.date == date }
    }

    override suspend fun deleteAll() {
        measurements.value = emptyList()
    }

    override suspend fun upsert(measurement: BodyMeasurementEntity) {
        measurements.value = measurements.value
            .filterNot { it.id == measurement.id && measurement.id != 0L }
            .plus(
                if (measurement.id == 0L) {
                    measurement.copy(id = (measurements.value.maxOfOrNull { it.id } ?: 0L) + 1)
                } else {
                    measurement
                },
            )
    }
}
