@file:Suppress("EmptyFunctionBlock")

package com.burak.healthapp.feature.today

import com.burak.healthapp.R
import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.today.TodayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    @get:Rule
    val mainDispatcherRule = TodayMainDispatcherRule()

    @Test
    fun dashboardCustomization_toggleVisibility_persistsToSettings() = runTest {
        val repository = FakeTodaySettingsRepository()
        val viewModel = TodayViewModel(
            dashboardRepository = FakeDashboardRepository(),
            settingsRepository = repository,
        )

        viewModel.updateDashboardCardVisibility(DashboardCardType.NUTRITION, false)
        advanceUntilIdle()

        assertEquals(false, repository.dashboardVisibilities[DashboardCardType.NUTRITION])
    }

    @Test
    fun dashboardCustomization_resetToDefault_persistsToSettings() = runTest {
        val repository = FakeTodaySettingsRepository()
        val viewModel = TodayViewModel(
            dashboardRepository = FakeDashboardRepository(),
            settingsRepository = repository,
        )

        viewModel.resetDashboardCards()
        advanceUntilIdle()

        assertEquals(true, repository.wasResetCalled)
    }

    @Test
    fun dashboardCustomization_failureHandling_emitsErrorUserMessage() = runTest {
        val repository = object : FakeTodaySettingsRepository() {
            override suspend fun updateDashboardCardVisibility(type: DashboardCardType, isVisible: Boolean) {
                error("Simulated failure")
            }
            override suspend fun resetDashboardCardsToDefault() {
                error("Simulated failure")
            }
        }
        val viewModel = TodayViewModel(
            dashboardRepository = FakeDashboardRepository(),
            settingsRepository = repository,
        )

        val emittedMessages = mutableListOf<Int>()
        val job = launch {
            viewModel.errorMessages.collect { emittedMessages.add(it) }
        }

        viewModel.updateDashboardCardVisibility(DashboardCardType.NUTRITION, false)
        advanceUntilIdle()

        viewModel.resetDashboardCards()
        advanceUntilIdle()

        assertTrue(emittedMessages.contains(R.string.dashboard_visibility_update_failed))
        assertTrue(emittedMessages.contains(R.string.dashboard_reset_failed))

        job.cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TodayMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

open class FakeTodaySettingsRepository : SettingsRepository {
    val dashboardVisibilities = mutableMapOf<DashboardCardType, Boolean>()
    var wasResetCalled = false

    override val settings: Flow<SettingsState> = MutableStateFlow(SettingsState())

    override suspend fun updateDashboardCardVisibility(type: DashboardCardType, isVisible: Boolean) {
        dashboardVisibilities[type] = isVisible
    }

    override suspend fun resetDashboardCardsToDefault() {
        wasResetCalled = true
    }

    // Unused overrides for Fake
    override fun observeSupplementTemplates() = kotlinx.coroutines.flow.emptyFlow<List<com.burak.healthapp.domain.model.SupplementTemplate>>()
    override suspend fun completeOnboarding(profile: com.burak.healthapp.domain.model.UserProfile, goals: com.burak.healthapp.domain.model.GoalSettings, initialMeasurement: com.burak.healthapp.domain.model.BodyMeasurementEntry, supplements: List<String>, useDefaultSupplementsWhenEmpty: Boolean, dashboardCards: List<DashboardCardConfig>?, waterReminderSettings: com.burak.healthapp.domain.model.WaterReminderSettings?, stepTrackingEnabled: Boolean?) {}
    override suspend fun updateGoalSettings(goals: com.burak.healthapp.domain.model.GoalSettings) {}
    override suspend fun updateWaterReminderSettings(settings: com.burak.healthapp.domain.model.WaterReminderSettings) {}
    override suspend fun updateWaterReminderSnoozedDate(date: java.time.LocalDate?) {}
    override suspend fun updateStepTrackingEnabled(enabled: Boolean) {}
    override suspend fun moveDashboardCard(type: DashboardCardType, newIndex: Int) {}
    override suspend fun updateProfile(profile: com.burak.healthapp.domain.model.UserProfile) {}
    override suspend fun updateThemeMode(mode: com.burak.healthapp.domain.model.ThemeMode) {}
    override suspend fun replaceSupplementTemplates(templates: List<com.burak.healthapp.domain.model.SupplementTemplate>) {}
}

class FakeDashboardRepository : DashboardRepository {
    override fun observeToday(date: java.time.LocalDate): Flow<com.burak.healthapp.domain.model.TodaySnapshot> = emptyFlow()
    override fun observeMealsForDate(date: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.MealEntry>> = emptyFlow()
    override fun observeHydrationBetween(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.HydrationEntry>> = emptyFlow()
    override fun observeLatestMeasurement(): Flow<com.burak.healthapp.domain.model.BodyMeasurementEntry?> = emptyFlow()
    override fun observeWeightHistory(): Flow<List<com.burak.healthapp.domain.model.BodyMeasurementEntry>> = emptyFlow()
    override fun observeSleepSessionsBetween(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.SleepSession>> = emptyFlow()
    override fun observeStepsBetween(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.StepEntry>> = emptyFlow()
    override fun observeCaffeineForDate(date: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.CaffeineEntry>> = emptyFlow()
    override fun observeCaffeineBetween(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.CaffeineEntry>> = emptyFlow()
    override fun observeSmokingBetween(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.SmokingEntry>> = emptyFlow()
    override fun observeExerciseBetween(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Flow<List<com.burak.healthapp.domain.model.ExerciseEntry>> = emptyFlow()
    override suspend fun saveMealEntry(entry: com.burak.healthapp.domain.model.MealEntry) {}
    override suspend fun deleteMealEntry(id: Long) {}
    override suspend fun deleteHydrationEntry(id: Long) {}
    override suspend fun deleteSleepForDate(date: java.time.LocalDate) {}
    override suspend fun deleteExerciseForDate(date: java.time.LocalDate) {}
    override suspend fun deleteSmokingForDate(date: java.time.LocalDate) {}
    override suspend fun deleteSupplementDoseForDate(templateId: Long, date: java.time.LocalDate) {}
    override suspend fun deleteStepsForDate(date: java.time.LocalDate) {}
    override suspend fun deleteBodyMeasurement(id: Long) {}
    override suspend fun addHydration(amountMl: Int, date: java.time.LocalDate) {}
    override suspend fun saveSleepSession(session: com.burak.healthapp.domain.model.SleepSession) {}
    override suspend fun saveExerciseEntry(entry: com.burak.healthapp.domain.model.ExerciseEntry, date: java.time.LocalDate) {}
    override suspend fun saveSmokingCount(count: Int, date: java.time.LocalDate) {}
    override suspend fun incrementSmokingCount(date: java.time.LocalDate, delta: Int) {}
    override suspend fun saveSupplementDoseEntries(entries: List<com.burak.healthapp.domain.model.SupplementDoseEntry>, date: java.time.LocalDate) {}
    override suspend fun saveBodyMeasurement(entry: com.burak.healthapp.domain.model.BodyMeasurementEntry) {}
    override suspend fun saveWeightMeasurement(weightKg: Float, date: java.time.LocalDate) {}
    override suspend fun recordStepSensorValue(sensorValue: Int, date: java.time.LocalDate) {}
    override suspend fun addCaffeine(entry: com.burak.healthapp.domain.model.CaffeineEntry) {}
    override suspend fun deleteCaffeine(id: Long) {}
}
