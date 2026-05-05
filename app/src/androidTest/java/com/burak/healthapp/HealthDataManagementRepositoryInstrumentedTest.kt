package com.burak.healthapp

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.burak.healthapp.core.database.HealthDatabase
import com.burak.healthapp.data.export.HealthDataManagementRepositoryImpl
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.CaffeineEntryEntity
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
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
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
        } catch (e: HealthDataImportException) {
            assertEquals(ImportValidationError.DecodeFailure, e.error)
        }

        assertEquals(emptyList<MealEntryEntity>(), database.mealDao().getAll())
        assertEquals(emptyList<HydrationEntryEntity>(), database.hydrationDao().getAll())
        assertEquals("Misafir", settingsRepository.current.userProfile.name)
    }

    @Test
    fun importHealthData_decodeFailure_doesNotWriteRoomOrSettings() = runBlocking {
        try {
            repository.importHealthData(fullImportModel().copy(meals = listOf(fullImportModel().meals.single().copy(date = "bad-date"))))
        } catch (e: HealthDataImportException) {
            assertEquals(ImportValidationError.DecodeFailure, e.error)
        }

        assertEquals(emptyList<MealEntryEntity>(), database.mealDao().getAll())
        assertEquals(emptyList<HydrationEntryEntity>(), database.hydrationDao().getAll())
        assertEquals("Misafir", settingsRepository.current.userProfile.name)
    }

    @Test
    fun importHealthData_databaseFailure_doesNotApplySettings() = runBlocking {
        database.close()

        try {
            repository.importHealthData(fullImportModel())
        } catch (e: HealthDataImportException) {
            assertEquals(ImportValidationError.DatabaseFailure, e.error)
        }

        assertEquals("Misafir", settingsRepository.current.userProfile.name)
        assertEquals(GoalSettings(), settingsRepository.current.goalSettings)
    }

    @Test
    fun importHealthData_withSettingsFailureResultsInPartialImport() = runBlocking {
        val failingSettingsRepo = object : RecordingManagementSettingsRepository() {
            override suspend fun updateGoalSettings(goals: GoalSettings): Unit = throw RuntimeException("Simulated Settings Failure")
        }
        val failingRepository = HealthDataManagementRepositoryImpl(database, failingSettingsRepo)

        try {
            failingRepository.importHealthData(fullImportModel())
        } catch (e: HealthDataImportException) {
            assertEquals(ImportValidationError.PartialSettingsFailure, e.error)
        }

        // Database records are imported
        assertEquals(1, database.mealDao().getAll().size)
        assertEquals(1, database.hydrationDao().getAll().size)
        // But settings failed (or partially succeeded before the throw)
    }

    @Test
    fun importHealthData_settingsFailureAfterRoomSuccess_reportsPartialImport() = runBlocking {
        val failingRepository = HealthDataManagementRepositoryImpl(
            database = database,
            settingsRepository = object : RecordingManagementSettingsRepository() {
                override suspend fun updateGoalSettings(goals: GoalSettings): Unit = throw RuntimeException("Simulated Settings Failure")
            },
        )

        try {
            failingRepository.importHealthData(fullImportModel())
        } catch (e: HealthDataImportException) {
            assertEquals(ImportValidationError.PartialSettingsFailure, e.error)
        }
    }

    @Test
    fun importHealthData_settingsFailureAfterRoomSuccess_keepsImportedRecords() = runBlocking {
        val failingRepository = HealthDataManagementRepositoryImpl(
            database = database,
            settingsRepository = object : RecordingManagementSettingsRepository() {
                override suspend fun updateGoalSettings(goals: GoalSettings): Unit = throw RuntimeException("Simulated Settings Failure")
            },
        )

        try {
            failingRepository.importHealthData(fullImportModel())
        } catch (_: HealthDataImportException) {
            // Expected: settings failed after the Room transaction committed.
        }

        assertEquals(1, database.mealDao().getAll().size)
        assertEquals(1, database.hydrationDao().getAll().size)
    }

    @Test
    fun importHealthData_retryAfterPartialSettingsFailure_doesNotDuplicateRecords() = runBlocking {
        val oneTimeFailingSettingsRepository = object : RecordingManagementSettingsRepository() {
            var shouldFailGoalUpdate = true

            override suspend fun updateGoalSettings(goals: GoalSettings) {
                if (shouldFailGoalUpdate) {
                    shouldFailGoalUpdate = false
                    throw RuntimeException("Simulated Settings Failure")
                }
                super.updateGoalSettings(goals)
            }
        }
        val retryRepository = HealthDataManagementRepositoryImpl(database, oneTimeFailingSettingsRepository)

        try {
            retryRepository.importHealthData(fullImportModel())
        } catch (e: HealthDataImportException) {
            assertEquals(ImportValidationError.PartialSettingsFailure, e.error)
        }
        retryRepository.importHealthData(fullImportModel())

        assertEquals(1, database.mealDao().getAll().size)
        assertEquals(1, database.hydrationDao().getAll().size)
        assertEquals(1, database.supplementTemplateDao().getAll().size)
        assertEquals(1, database.supplementDoseDao().getAll().size)
        assertEquals(12_000, oneTimeFailingSettingsRepository.current.goalSettings.dailyStepTarget)
    }

    @Test
    fun importHealthData_success_appliesRoomAndSettings() = runBlocking {
        repository.importHealthData(fullImportModel())

        assertEquals(1, database.mealDao().getAll().size)
        assertEquals(1, database.hydrationDao().getAll().size)
        assertEquals("Ada", settingsRepository.current.userProfile.name)
        assertEquals(12_000, settingsRepository.current.goalSettings.dailyStepTarget)
        assertEquals(ThemeMode.DARK, settingsRepository.current.themeMode)
    }

    @Test
    fun importCustomFoods_doesNotDuplicateSameExportImportedTwice() = runBlocking {
        val model = fullImportModel().copy(
            customFoods = listOf(
                ExportedCustomFood(
                    id = 1,
                    name = "Sütlü kahve",
                    brand = "Cafe",
                    servingName = "fincan",
                    servingGrams = 200f,
                    calories = 100,
                    proteinGrams = 4,
                    carbsGrams = 10,
                    fatGrams = 4,
                    isFavorite = false,
                    createdAt = "2026-04-27T10:00:00",
                    updatedAt = "2026-04-27T10:00:00",
                ),
            ),
        )

        repository.importHealthData(model)
        repository.importHealthData(model)

        assertEquals(1, database.customFoodDao().getAll().size)
    }

    @Test
    fun importCustomFoods_deduplicatesTurkishNormalizedNames() = runBlocking {
        val date = LocalDateTime.parse("2026-04-27T10:00:00")
        database.customFoodDao().upsert(
            com.burak.healthapp.data.local.entity.CustomFoodEntity(
                name = "Sütlü kahve",
                brand = "Cafe",
                servingName = "fincan",
                servingGrams = 200f,
                calories = 100,
                proteinGrams = 4,
                carbsGrams = 10,
                fatGrams = 4,
                createdAt = date,
                updatedAt = date,
            ),
        )

        val model = fullImportModel().copy(
            customFoods = listOf(
                ExportedCustomFood(
                    id = 1,
                    name = "Sutlu kahve", // missing turkish chars
                    brand = "cafe", // different case
                    servingName = "fincan",
                    servingGrams = 200f,
                    calories = 100,
                    proteinGrams = 4,
                    carbsGrams = 10,
                    fatGrams = 4,
                    isFavorite = true, // different favorite status, should be updated or ignored
                    createdAt = "2026-04-27T10:00:00",
                    updatedAt = "2026-04-27T10:00:00", // same date, keep existing or ignore
                ),
            ),
        )

        repository.importHealthData(model)

        assertEquals(1, database.customFoodDao().getAll().size)
    }

    @Test
    fun importCustomFoods_updatesExistingWhenImportedIsNewer() = runBlocking {
        val oldDate = LocalDateTime.parse("2026-04-27T10:00:00")
        database.customFoodDao().upsert(
            com.burak.healthapp.data.local.entity.CustomFoodEntity(
                name = "Yulaf",
                brand = null,
                servingName = "kase",
                servingGrams = 100f,
                calories = 300,
                proteinGrams = 10,
                carbsGrams = 50,
                fatGrams = 5,
                createdAt = oldDate,
                updatedAt = oldDate,
            ),
        )

        val model = fullImportModel().copy(
            customFoods = listOf(
                ExportedCustomFood(
                    id = 1,
                    name = "Yulaf",
                    brand = null,
                    servingName = "kase",
                    servingGrams = 100f,
                    calories = 350, // updated calories
                    proteinGrams = 10,
                    carbsGrams = 50,
                    fatGrams = 5,
                    isFavorite = true, // updated favorite
                    createdAt = "2026-04-27T10:00:00",
                    updatedAt = "2026-04-27T11:00:00", // newer
                ),
            ),
        )

        repository.importHealthData(model)

        val foods = database.customFoodDao().getAll()
        assertEquals(1, foods.size)
        assertEquals(350, foods.first().calories)
        assertTrue(foods.first().isFavorite)
    }

    @Test
    fun importCustomFoods_keepsExistingWhenImportedIsOlder() = runBlocking {
        val newDate = LocalDateTime.parse("2026-04-27T11:00:00")
        database.customFoodDao().upsert(
            com.burak.healthapp.data.local.entity.CustomFoodEntity(
                name = "Yulaf",
                brand = null,
                servingName = "kase",
                servingGrams = 100f,
                calories = 350,
                proteinGrams = 10,
                carbsGrams = 50,
                fatGrams = 5,
                isFavorite = true,
                createdAt = LocalDateTime.parse("2026-04-27T10:00:00"),
                updatedAt = newDate,
            ),
        )

        val model = fullImportModel().copy(
            customFoods = listOf(
                ExportedCustomFood(
                    id = 1,
                    name = "Yulaf",
                    brand = null,
                    servingName = "kase",
                    servingGrams = 100f,
                    calories = 300, // older calories
                    proteinGrams = 10,
                    carbsGrams = 50,
                    fatGrams = 5,
                    isFavorite = false,
                    createdAt = "2026-04-27T10:00:00",
                    updatedAt = "2026-04-27T10:00:00", // older
                ),
            ),
        )

        repository.importHealthData(model)

        val foods = database.customFoodDao().getAll()
        assertEquals(1, foods.size)
        assertEquals(350, foods.first().calories) // Should keep newer
        assertTrue(foods.first().isFavorite)
    }

    @Test
    fun importCustomFoods_allowsDuplicateWhenServingNameDiffers() = runBlocking {
        val date = LocalDateTime.parse("2026-04-27T10:00:00")
        database.customFoodDao().upsert(
            com.burak.healthapp.data.local.entity.CustomFoodEntity(
                name = "Yulaf",
                brand = null,
                servingName = "kase",
                servingGrams = 100f,
                calories = 300,
                proteinGrams = 10,
                carbsGrams = 50,
                fatGrams = 5,
                createdAt = date,
                updatedAt = date,
            ),
        )

        val model = fullImportModel().copy(
            customFoods = listOf(
                ExportedCustomFood(
                    id = 1,
                    name = "Yulaf",
                    brand = null,
                    servingName = "porsiyon", // different serving name
                    servingGrams = 100f,
                    calories = 300,
                    proteinGrams = 10,
                    carbsGrams = 50,
                    fatGrams = 5,
                    isFavorite = false,
                    createdAt = "2026-04-27T10:00:00",
                    updatedAt = "2026-04-27T10:00:00",
                ),
            ),
        )

        repository.importHealthData(model)

        val foods = database.customFoodDao().getAll()
        assertEquals(2, foods.size)
    }

    @Test
    fun importCustomFoods_allowsDuplicateWhenServingGramsDiffers() = runBlocking {
        val date = LocalDateTime.parse("2026-04-27T10:00:00")
        database.customFoodDao().upsert(
            com.burak.healthapp.data.local.entity.CustomFoodEntity(
                name = "Yulaf",
                brand = null,
                servingName = "kase",
                servingGrams = 100f,
                calories = 300,
                proteinGrams = 10,
                carbsGrams = 50,
                fatGrams = 5,
                createdAt = date,
                updatedAt = date,
            ),
        )

        val model = fullImportModel().copy(
            customFoods = listOf(
                ExportedCustomFood(
                    id = 1,
                    name = "Yulaf",
                    brand = null,
                    servingName = "kase",
                    servingGrams = 50f, // different serving grams
                    calories = 150,
                    proteinGrams = 5,
                    carbsGrams = 25,
                    fatGrams = 2,
                    isFavorite = false,
                    createdAt = "2026-04-27T10:00:00",
                    updatedAt = "2026-04-27T10:00:00",
                ),
            ),
        )

        repository.importHealthData(model)

        val foods = database.customFoodDao().getAll()
        assertEquals(2, foods.size)
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
        database.caffeineDao().upsert(
            CaffeineEntryEntity(
                date = date,
                time = java.time.LocalTime.of(9, 0),
                drinkType = "COFFEE",
                size = "MEDIUM",
                estimatedMg = 120,
                customName = null,
                createdAt = date.atTime(9, 0),
            ),
        )
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
        database.customFoodDao().upsert(
            com.burak.healthapp.data.local.entity.CustomFoodEntity(
                name = "Yulaf",
                servingName = "kase",
                servingGrams = 100f,
                calories = 350,
                proteinGrams = 10,
                carbsGrams = 50,
                fatGrams = 5,
                createdAt = date.atTime(10, 0),
                updatedAt = date.atTime(10, 0),
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<MealEntryEntity>(), database.mealDao().getAll())
        assertEquals(emptyList<HydrationEntryEntity>(), database.hydrationDao().getAll())
        assertEquals(emptyList<SleepSessionEntity>(), database.sleepDao().getAll())
        assertEquals(emptyList<ExerciseEntryEntity>(), database.exerciseDao().getAll())
        assertEquals(emptyList<SmokingEntryEntity>(), database.smokingDao().getAll())
        assertEquals(emptyList<StepEntryEntity>(), database.stepDao().getAll())
        assertEquals(emptyList<CaffeineEntryEntity>(), database.caffeineDao().getAll())
        assertEquals(emptyList<BodyMeasurementEntity>(), database.bodyMeasurementDao().getAll())
        assertEquals(emptyList<SupplementTemplateEntity>(), database.supplementTemplateDao().getAll())
        assertEquals(emptyList<SupplementDoseEntryEntity>(), database.supplementDoseDao().getAll())
        assertEquals(0, supplementCheckCount())
        assertEquals(0, database.customFoodDao().getAll().size)
        assertEquals("Misafir", settingsRepository.current.userProfile.name)
        assertEquals(ThemeMode.SYSTEM, settingsRepository.current.themeMode)
        assertEquals(GoalSettings(), settingsRepository.current.goalSettings)
    }

    @Test
    fun deleteAll_isIdempotentWhenCalledTwice() = runBlocking {
        database.hydrationDao().upsert(
            HydrationEntryEntity(
                date = LocalDate.of(2026, 4, 27),
                amountMl = 250,
                createdAt = LocalDateTime.parse("2026-04-27T09:00:00"),
            ),
        )

        repository.deleteAllHealthData()
        repository.deleteAllHealthData()

        assertEquals(emptyList<HydrationEntryEntity>(), database.hydrationDao().getAll())
        assertEquals("Misafir", settingsRepository.current.userProfile.name)
    }

    @Test
    fun deleteAll_removesHydrationEntries() = runBlocking {
        database.hydrationDao().upsert(
            HydrationEntryEntity(date = TEST_DATE, amountMl = 250, createdAt = TEST_DATE.atTime(9, 0)),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<HydrationEntryEntity>(), database.hydrationDao().getAll())
    }

    @Test
    fun deleteAll_removesSleepEntries() = runBlocking {
        database.sleepDao().upsert(
            SleepSessionEntity(
                sessionDate = TEST_DATE,
                startTime = TEST_DATE.minusDays(1).atTime(23, 0),
                endTime = TEST_DATE.atTime(7, 0),
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<SleepSessionEntity>(), database.sleepDao().getAll())
    }

    @Test
    fun deleteAll_removesMealEntries() = runBlocking {
        database.mealDao().upsert(
            MealEntryEntity(
                date = TEST_DATE,
                mealType = MealType.BREAKFAST.name,
                name = "Yulaf",
                calories = 300,
                carbsGrams = 40,
                fatGrams = 8,
                proteinGrams = 20,
                createdAt = TEST_DATE.atTime(8, 0),
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<MealEntryEntity>(), database.mealDao().getAll())
    }

    @Test
    fun deleteAll_removesCaffeineEntries() = runBlocking {
        database.caffeineDao().upsert(
            CaffeineEntryEntity(
                date = TEST_DATE,
                time = java.time.LocalTime.of(9, 0),
                drinkType = "COFFEE",
                size = "MEDIUM",
                estimatedMg = 120,
                customName = null,
                createdAt = TEST_DATE.atTime(9, 0),
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<CaffeineEntryEntity>(), database.caffeineDao().getAll())
    }

    @Test
    fun deleteAll_removesSmokingEntries() = runBlocking {
        database.smokingDao().upsert(SmokingEntryEntity(date = TEST_DATE, count = 2))

        repository.deleteAllHealthData()

        assertEquals(emptyList<SmokingEntryEntity>(), database.smokingDao().getAll())
    }

    @Test
    fun deleteAll_removesExerciseEntries() = runBlocking {
        database.exerciseDao().upsert(
            ExerciseEntryEntity(
                date = TEST_DATE,
                type = "Koşu",
                durationMinutes = 45,
                intensity = "MEDIUM",
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<ExerciseEntryEntity>(), database.exerciseDao().getAll())
    }

    @Test
    fun deleteAll_removesStepEntries() = runBlocking {
        database.stepDao().upsert(
            StepEntryEntity(
                date = TEST_DATE,
                steps = 8_000,
                sensorBaseline = 0,
                lastSensorValue = 8_000,
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<StepEntryEntity>(), database.stepDao().getAll())
    }

    @Test
    fun deleteAll_removesBodyMeasurements_ifCurrentContractSaysHealthData() = runBlocking {
        database.bodyMeasurementDao().upsert(
            BodyMeasurementEntity(
                date = TEST_DATE,
                weightKg = 77f,
                shoulderCm = 118f,
                waistCm = 88f,
                hipCm = 99f,
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<BodyMeasurementEntity>(), database.bodyMeasurementDao().getAll())
    }

    @Test
    fun deleteAll_removesSupplementDoses_ifCurrentContractSaysHealthData() = runBlocking {
        val templateId = database.supplementTemplateDao().insert(
            SupplementTemplateEntity(name = "D3", targetAmount = 25f, unitLabel = "mcg"),
        )
        database.supplementDoseDao().upsertAll(
            listOf(SupplementDoseEntryEntity(templateId = templateId, date = TEST_DATE, amount = 25f)),
        )

        repository.deleteAllHealthData()

        assertEquals(emptyList<SupplementDoseEntryEntity>(), database.supplementDoseDao().getAll())
    }

    @Test
    fun deleteAll_preservesProfileGoalsThemeOnboardingSettings_ifCurrentContractSaysHealthDataOnly() = runBlocking {
        settingsRepository.updateProfile(UserProfile(name = "Ada", avatarInitials = "A", heightCm = 170f))
        settingsRepository.updateGoalSettings(GoalSettings(dailyStepTarget = 12_000))
        settingsRepository.updateThemeMode(ThemeMode.DARK)

        repository.deleteAllHealthData()

        assertEquals("Ada", settingsRepository.current.userProfile.name)
        assertEquals(12_000, settingsRepository.current.goalSettings.dailyStepTarget)
        assertEquals(ThemeMode.DARK, settingsRepository.current.themeMode)
        assertTrue(settingsRepository.current.onboardingCompleted)
    }

    @Test
    fun deleteAll_customFoods_behaviorMatchesDocumentedContract() = runBlocking {
        database.customFoodDao().upsert(
            com.burak.healthapp.data.local.entity.CustomFoodEntity(
                name = "Yulaf",
                servingName = "kase",
                servingGrams = 100f,
                calories = 350,
                proteinGrams = 10,
                carbsGrams = 50,
                fatGrams = 5,
                createdAt = TEST_DATE.atTime(10, 0),
                updatedAt = TEST_DATE.atTime(10, 0),
            ),
        )

        repository.deleteAllHealthData()

        assertEquals(0, database.customFoodDao().getAll().size)
    }

    private fun supplementCheckCount(): Int {
        val cursor = database.openHelper.writableDatabase.query("SELECT COUNT(*) FROM supplement_checks")
        return cursor.use {
            it.moveToFirst()
            it.getInt(0)
        }
    }

    private companion object {
        val TEST_DATE: LocalDate = LocalDate.of(2026, 4, 27)
    }
}

private open class RecordingManagementSettingsRepository : SettingsRepository {
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
        useDefaultSupplementsWhenEmpty: Boolean,
        dashboardCards: List<DashboardCardConfig>?,
        waterReminderSettings: WaterReminderSettings?,
        stepTrackingEnabled: Boolean?,
    ) {
        updateProfile(profile)
        updateGoalSettings(goals)
        waterReminderSettings?.let { updateWaterReminderSettings(it) }
        stepTrackingEnabled?.let { updateStepTrackingEnabled(it) }
    }

    open override suspend fun updateGoalSettings(goals: GoalSettings) {
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

    override suspend fun updateDashboardCardVisibility(
        type: DashboardCardType,
        isVisible: Boolean,
    ) = Unit

    override suspend fun moveDashboardCard(
        type: DashboardCardType,
        newIndex: Int,
    ) = Unit

    override suspend fun resetDashboardCardsToDefault() = Unit

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
