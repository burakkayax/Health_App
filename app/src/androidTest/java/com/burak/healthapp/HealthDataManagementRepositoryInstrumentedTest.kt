package com.burak.healthapp

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.data.export.HealthDataManagementRepositoryImpl
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementCheckEntity
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
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HealthDataManagementRepositoryInstrumentedTest {
    private lateinit var database: HealthDatabase
    private lateinit var settingsRepository: RecordingManagementSettingsRepository
    private lateinit var repository: HealthDataManagementRepositoryImpl

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsRepository = RecordingManagementSettingsRepository()
        repository = HealthDataManagementRepositoryImpl(database, settingsRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun importHealthData_deduplicatesMultiEntriesReplacesDateEntriesAndMapsTemplates() = runBlocking {
        val date = LocalDate.of(2026, 4, 27)
        database.exerciseDao().upsert(
            ExerciseEntryEntity(
                date = date,
                type = "Eski",
                durationMinutes = 10,
                intensity = "LOW",
            ),
        )
        database.smokingDao().upsert(SmokingEntryEntity(date = date, count = 8))
        database.stepDao().upsert(
            StepEntryEntity(
                date = date,
                steps = 100,
                sensorBaseline = 10,
                lastSensorValue = 110,
                updatedAt = date.atTime(8, 0),
            ),
        )
        database.bodyMeasurementDao().upsert(
            BodyMeasurementEntity(
                date = date,
                weightKg = 80f,
                shoulderCm = 118f,
                waistCm = 90f,
                hipCm = 99f,
                recordedAt = date.atTime(8, 0),
            ),
        )
        database.sleepDao().upsert(
            SleepSessionEntity(
                sessionDate = date,
                startTime = date.minusDays(1).atTime(23, 0),
                endTime = date.atTime(6, 0),
            ),
        )

        repository.importHealthData(fullImportModel())
        repository.importHealthData(fullImportModel())

        assertEquals(1, database.mealDao().getAll().size)
        assertEquals(1, database.hydrationDao().getAll().size)
        assertEquals("Koşu", database.exerciseDao().getAll().single().type)
        assertEquals(1, database.exerciseDao().getAll().size)
        assertEquals(2, database.smokingDao().getAll().single().count)
        assertEquals(9_500, database.stepDao().getAll().single().steps)
        assertEquals(76.2f, database.bodyMeasurementDao().getAll().single().weightKg, 0.001f)
        assertEquals(LocalDateTime.parse("2026-04-27T07:30:00"), database.sleepDao().getAll().single().endTime)

        val template = database.supplementTemplateDao().getAll().single()
        val dose = database.supplementDoseDao().getAll().single()
        assertEquals("D3", template.name)
        assertEquals(template.id, dose.templateId)
        assertTrue(dose.templateId != 42L)

        assertEquals("Ada", settingsRepository.current.userProfile.name)
        assertEquals(12_000, settingsRepository.current.goalSettings.dailyStepTarget)
        assertEquals(ThemeMode.DARK, settingsRepository.current.themeMode)
    }

    @Test
    fun importHealthData_withInvalidModelLeavesRoomDataUntouched() = runBlocking {
        try {
            repository.importHealthData(fullImportModel().copy(meals = listOf(fullImportModel().meals.single().copy(date = "bad-date"))))
        } catch (_: Exception) {
            // Expected: parsing fails before Room writes begin.
        }

        assertEquals(emptyList<MealEntryEntity>(), database.mealDao().getAll())
        assertEquals(emptyList<HydrationEntryEntity>(), database.hydrationDao().getAll())
        assertEquals("Misafir", settingsRepository.current.userProfile.name)
    }

    @Test
    fun deleteAllHealthData_deletesHealthRecordsAndPreservesSettings() = runBlocking {
        val date = LocalDate.of(2026, 4, 27)
        database.mealDao().upsert(
            MealEntryEntity(
                date = date,
                mealType = MealType.BREAKFAST.name,
                name = "Yulaf",
                calories = 300,
                carbsGrams = 40,
                fatGrams = 8,
                proteinGrams = 20,
                createdAt = date.atTime(8, 0),
            ),
        )
        database.hydrationDao().upsert(HydrationEntryEntity(date = date, amountMl = 250, createdAt = date.atTime(9, 0)))
        database.sleepDao().upsert(
            SleepSessionEntity(
                sessionDate = date,
                startTime = date.minusDays(1).atTime(23, 0),
                endTime = date.atTime(7, 0),
            ),
        )
        database.exerciseDao().upsert(ExerciseEntryEntity(date = date, type = "Koşu", durationMinutes = 45, intensity = "MEDIUM"))
        database.smokingDao().upsert(SmokingEntryEntity(date = date, count = 2))
        database.stepDao().upsert(StepEntryEntity(date = date, steps = 8_000, sensorBaseline = 0, lastSensorValue = 8_000))
        database.bodyMeasurementDao().upsert(
            BodyMeasurementEntity(date = date, weightKg = 77f, shoulderCm = 118f, waistCm = 88f, hipCm = 99f),
        )
        val templateId = database.supplementTemplateDao().insert(
            SupplementTemplateEntity(name = "D3", targetAmount = 25f, unitLabel = "mcg"),
        )
        database.supplementDoseDao().upsertAll(
            listOf(SupplementDoseEntryEntity(templateId = templateId, date = date, amount = 25f)),
        )
        database.supplementCheckDao().upsert(
            SupplementCheckEntity(templateId = templateId, date = date, isChecked = true),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<MealEntryEntity>(), database.mealDao().getAll())
        assertEquals(emptyList<HydrationEntryEntity>(), database.hydrationDao().getAll())
        assertEquals(emptyList<SleepSessionEntity>(), database.sleepDao().getAll())
        assertEquals(emptyList<ExerciseEntryEntity>(), database.exerciseDao().getAll())
        assertEquals(emptyList<SmokingEntryEntity>(), database.smokingDao().getAll())
        assertEquals(emptyList<StepEntryEntity>(), database.stepDao().getAll())
        assertEquals(emptyList<BodyMeasurementEntity>(), database.bodyMeasurementDao().getAll())
        assertEquals(emptyList<SupplementTemplateEntity>(), database.supplementTemplateDao().getAll())
        assertEquals(emptyList<SupplementDoseEntryEntity>(), database.supplementDoseDao().getAll())
        assertEquals(0, supplementCheckCount())
        assertEquals("Misafir", settingsRepository.current.userProfile.name)
        assertEquals(ThemeMode.SYSTEM, settingsRepository.current.themeMode)
        assertEquals(GoalSettings(), settingsRepository.current.goalSettings)
    }

    private fun supplementCheckCount(): Int {
        val cursor = database.openHelper.writableDatabase.query("SELECT COUNT(*) FROM supplement_checks")
        return cursor.use {
            it.moveToFirst()
            it.getInt(0)
        }
    }
}

private class RecordingManagementSettingsRepository : SettingsRepository {
    private val state = MutableStateFlow(SettingsState(onboardingCompleted = true))

    val current: SettingsState
        get() = state.value

    override val settings: Flow<SettingsState> = state

    override fun observeSupplementTemplates(): Flow<List<SupplementTemplate>> = flowOf(emptyList())

    override suspend fun completeOnboarding(
        profile: UserProfile,
        goals: GoalSettings,
        initialMeasurement: BodyMeasurementEntry,
        supplements: List<String>,
    ) = Unit

    override suspend fun updateGoalSettings(goals: GoalSettings) {
        state.value = state.value.copy(goalSettings = goals)
    }

    override suspend fun updateWaterReminderSettings(settings: WaterReminderSettings) {
        state.value = state.value.copy(waterReminderSettings = settings)
    }

    override suspend fun updateWaterReminderSnoozedDate(date: LocalDate?) {
        state.value = state.value.copy(waterReminderSnoozedDate = date)
    }

    override suspend fun updateStepTrackingEnabled(enabled: Boolean) {
        state.value = state.value.copy(stepTrackingEnabled = enabled)
    }

    override suspend fun updateProfile(profile: UserProfile) {
        state.value = state.value.copy(userProfile = profile)
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>) = Unit
}

private fun fullImportModel(): HealthDataExportModel {
    val goals = GoalSettings(dailyStepTarget = 12_000)
    val date = "2026-04-27"
    return HealthDataExportModel(
        exportedAt = "2026-04-27T10:15:30Z",
        appVersion = "1.0-test",
        profile = ExportedUserProfile(name = "Ada", avatarInitials = "A", heightCm = 170f),
        goals = ExportedGoalSettings(
            dailyCaloriesTarget = goals.dailyCaloriesTarget,
            proteinTargetGrams = goals.proteinTargetGrams,
            carbTargetGrams = goals.carbTargetGrams,
            fatTargetGrams = goals.fatTargetGrams,
            waterTargetMl = goals.waterTargetMl,
            dailyStepTarget = goals.dailyStepTarget,
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
        meals = listOf(
            ExportedMealEntry(
                id = 1,
                date = date,
                mealType = MealType.BREAKFAST.name,
                name = "Yulaf",
                calories = 300,
                carbsGrams = 40,
                fatGrams = 8,
                proteinGrams = 20,
                createdAt = "2026-04-27T08:00:00",
            ),
        ),
        hydration = listOf(
            ExportedHydrationEntry(id = 2, date = date, amountMl = 500, createdAt = "2026-04-27T09:00:00"),
        ),
        sleep = listOf(
            ExportedSleepSession(
                id = 3,
                sessionDate = date,
                startTime = "2026-04-26T23:30:00",
                endTime = "2026-04-27T07:30:00",
            ),
        ),
        exercise = listOf(
            ExportedExerciseEntry(id = 4, date = date, type = "Koşu", durationMinutes = 45, intensity = "MEDIUM"),
        ),
        smoking = listOf(ExportedSmokingEntry(id = 5, date = date, count = 2)),
        steps = listOf(
            ExportedStepEntry(
                id = 6,
                date = date,
                steps = 9_500,
                sensorBaseline = 100,
                lastSensorValue = 9_600,
                updatedAt = "2026-04-27T20:00:00",
            ),
        ),
        bodyMeasurements = listOf(
            ExportedBodyMeasurementEntry(
                id = 7,
                date = date,
                weightKg = 76.2f,
                shoulderCm = 118f,
                waistCm = 87f,
                hipCm = 98f,
                recordedAt = "2026-04-27T08:30:00",
            ),
        ),
        supplementTemplates = listOf(
            ExportedSupplementTemplate(
                id = 42,
                name = "D3",
                targetAmount = 25f,
                unitLabel = "mcg",
                isActive = true,
                sortOrder = 0,
            ),
        ),
        supplementDoseEntries = listOf(
            ExportedSupplementDoseEntry(
                id = 8,
                templateId = 42,
                date = date,
                amount = 25f,
                loggedAt = "2026-04-27T09:30:00",
            ),
        ),
    )
}
