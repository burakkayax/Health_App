package com.burak.healthapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.SupplementTemplateDao
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
import com.burak.healthapp.data.repository.SettingsRepositoryImpl
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import androidx.datastore.preferences.core.edit
import com.burak.healthapp.core.datastore.SettingsKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalTime

class SettingsRepositoryTest {
    @Test
    fun updateThemeMode_persistsAndEmitsThemeMode() = runTest {
        val tempDir = Files.createTempDirectory("health-settings").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )

        repository.updateThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.settings.first().themeMode)
        tempDir.deleteRecursively()
    }

    @Test
    fun updateProfile_persistsHeight() = runTest {
        val tempDir = Files.createTempDirectory("health-profile").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )

        repository.updateProfile(UserProfile(name = "Burak", avatarInitials = "BK", heightCm = 175f))

        assertEquals(175f, repository.settings.first().userProfile.heightCm)
        tempDir.deleteRecursively()
    }

    @Test
    fun updateGoalSettings_persistsDailyStepTarget() = runTest {
        val tempDir = Files.createTempDirectory("health-step-goal").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )

        repository.updateGoalSettings(GoalSettings(dailyStepTarget = 12000))

        assertEquals(12000, repository.settings.first().goalSettings.dailyStepTarget)
        tempDir.deleteRecursively()
    }

    @Test
    fun updateWaterReminderSettings_persistsReminderWindow() = runTest {
        val tempDir = Files.createTempDirectory("health-water-reminder").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )

        repository.updateWaterReminderSettings(
            WaterReminderSettings(
                enabled = true,
                startTime = LocalTime.of(10, 0),
                endTime = LocalTime.of(20, 30),
                intervalMinutes = 45,
            ),
        )

        val settings = repository.settings.first().waterReminderSettings
        assertEquals(true, settings.enabled)
        assertEquals(LocalTime.of(10, 0), settings.startTime)
        assertEquals(LocalTime.of(20, 30), settings.endTime)
        assertEquals(45, settings.intervalMinutes)
        tempDir.deleteRecursively()
    }

    @Test
    fun updateStepTrackingEnabled_persistsUserControl() = runTest {
        val tempDir = Files.createTempDirectory("health-step-tracking").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )

        assertEquals(false, repository.settings.first().stepTrackingEnabled)
        repository.updateStepTrackingEnabled(true)

        assertEquals(true, repository.settings.first().stepTrackingEnabled)
        tempDir.deleteRecursively()
    }

    @Test
    fun updateWaterReminderSnoozedDate_persistsAndClearsSnooze() = runTest {
        val tempDir = Files.createTempDirectory("health-water-snooze").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )
        val today = LocalDate.of(2026, 4, 27)

        repository.updateWaterReminderSnoozedDate(today)

        assertEquals(today, repository.settings.first().waterReminderSnoozedDate)

        repository.updateWaterReminderSnoozedDate(null)

        assertEquals(null, repository.settings.first().waterReminderSnoozedDate)
        tempDir.deleteRecursively()
    }

    @Test
    fun dashboardCardCustomization_persistsVisibilityMoveAndReset() = runTest {
        val tempDir = Files.createTempDirectory("health-dashboard-config").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )

        repository.updateDashboardCardVisibility(DashboardCardType.HYDRATION, false)
        repository.moveDashboardCard(DashboardCardType.HYDRATION, 0)

        val customized = repository.settings.first().dashboardCards
        assertEquals(DashboardCardType.HYDRATION, customized.first().type)
        assertEquals(false, customized.first().isVisible)

        repository.resetDashboardCardsToDefault()

        val reset = repository.settings.first().dashboardCards
        assertEquals(DashboardCardType.NUTRITION, reset.first().type)
        assertEquals(true, reset.first { it.type == DashboardCardType.HYDRATION }.isVisible)
        tempDir.deleteRecursively()
    }

    @Test
    fun invalidStoredTimes_fallBackToDefaults() = runTest {
        val tempDir = Files.createTempDirectory("health-invalid-times").toFile()
        val tempFile = File(tempDir, "settings.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { tempFile },
        )
        
        dataStore.edit { preferences ->
            preferences[SettingsKeys.sleepTargetBedtime] = "invalid-time"
            preferences[SettingsKeys.caffeineCutoffTime] = "invalid-time"
            preferences[SettingsKeys.waterReminderStartTime] = "invalid-time"
            preferences[SettingsKeys.waterReminderSnoozedDate] = "invalid-date"
            preferences[SettingsKeys.dashboardCardConfig] = "invalid-json"
        }

        val repository = SettingsRepositoryImpl(
            dataStore = dataStore,
            templateDao = EmptyTemplateDao,
            measurementDao = EmptyMeasurementDao,
        )

        val settings = repository.settings.first()
        
        assertEquals(com.burak.healthapp.domain.config.DefaultHealthGoals.SLEEP_BEDTIME, settings.goalSettings.sleepTargetBedtime)
        assertEquals(com.burak.healthapp.domain.config.DefaultHealthGoals.CAFFEINE_CUTOFF_TIME, settings.goalSettings.caffeineCutoffTime)
        assertEquals(com.burak.healthapp.domain.config.DefaultHealthGoals.WATER_REMINDER_START_TIME, settings.waterReminderSettings.startTime)
        assertEquals(null, settings.waterReminderSnoozedDate)
        assertEquals(com.burak.healthapp.domain.model.defaultDashboardCardConfig().map { it.type }, settings.dashboardCards.map { it.type })
        
        tempDir.deleteRecursively()
    }
}

private object EmptyTemplateDao : SupplementTemplateDao {
    override fun observeActive(): Flow<List<SupplementTemplateEntity>> = emptyFlow()

    override suspend fun getAll(): List<SupplementTemplateEntity> = emptyList()

    override suspend fun insert(template: SupplementTemplateEntity): Long = 1L

    override suspend fun upsertAll(templates: List<SupplementTemplateEntity>) = Unit

    override suspend fun deactivate(ids: List<Long>) = Unit

    override suspend fun deleteAll() = Unit
}

private object EmptyMeasurementDao : BodyMeasurementDao {
    override suspend fun getAll(): List<BodyMeasurementEntity> = emptyList()

    override fun observeForDate(date: java.time.LocalDate): Flow<BodyMeasurementEntity?> = emptyFlow()

    override fun observeLatest(): Flow<BodyMeasurementEntity?> = emptyFlow()

    override fun observeAll(): Flow<List<BodyMeasurementEntity>> = emptyFlow()

    override fun observeEarliest(): Flow<BodyMeasurementEntity?> = emptyFlow()

    override suspend fun getLatest(): BodyMeasurementEntity? = null

    override suspend fun getForDate(date: java.time.LocalDate): BodyMeasurementEntity? = null

    override suspend fun getLatestOnOrBefore(date: java.time.LocalDate): BodyMeasurementEntity? = null

    override fun observeLatestOnOrBefore(date: java.time.LocalDate): Flow<BodyMeasurementEntity?> = emptyFlow()

    override fun observeEarliestOnOrAfter(date: java.time.LocalDate): Flow<BodyMeasurementEntity?> = emptyFlow()

    override fun observeBetween(
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate,
    ): Flow<List<BodyMeasurementEntity>> = emptyFlow()

    override suspend fun deleteById(id: Long) = Unit

    override suspend fun deleteForDate(date: java.time.LocalDate) = Unit

    override suspend fun deleteAll() = Unit

    override suspend fun upsert(measurement: BodyMeasurementEntity) = Unit
}
