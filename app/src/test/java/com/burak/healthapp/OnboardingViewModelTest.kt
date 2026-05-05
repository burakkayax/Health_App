package com.burak.healthapp

import androidx.lifecycle.SavedStateHandle
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.onboarding.OnboardingActivityLevel
import com.burak.healthapp.feature.onboarding.OnboardingFieldKeys
import com.burak.healthapp.feature.onboarding.OnboardingMainGoal
import com.burak.healthapp.feature.onboarding.OnboardingSex
import com.burak.healthapp.feature.onboarding.OnboardingStep
import com.burak.healthapp.feature.onboarding.OnboardingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = OnboardingMainDispatcherRule()

    @Test
    fun cannotProceedFromTrackingAreasWhenNothingSelected() = runTest {
        val viewModel = OnboardingViewModel(FakeOnboardingSettingsRepository(), SavedStateHandle())

        viewModel.goToNextStep() // WELCOME -> TRACKING_AREAS
        advanceUntilIdle()
        assertEquals(OnboardingStep.TRACKING_AREAS, viewModel.uiState.value.currentStep)

        // Deselect all
        viewModel.uiState.value.selectedTrackingAreas.toList().forEach {
            viewModel.onTrackingAreaToggled(it)
        }
        advanceUntilIdle()

        viewModel.goToNextStep()
        advanceUntilIdle()

        assertEquals(OnboardingStep.TRACKING_AREAS, viewModel.uiState.value.currentStep)
        assertNotNull(viewModel.uiState.value.validationErrors["tracking_areas"])
    }

    @Test
    fun emptyOptionalBasicInfoCanProceed() = runTest {
        val viewModel = OnboardingViewModel(FakeOnboardingSettingsRepository(), SavedStateHandle())
        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        advanceUntilIdle()
        assertEquals(OnboardingStep.BASIC_INFO, viewModel.uiState.value.currentStep)

        viewModel.goToNextStep() // Should proceed
        advanceUntilIdle()
        assertEquals(OnboardingStep.ACTIVITY_GOAL, viewModel.uiState.value.currentStep)
    }

    @Test
    fun invalidAgeShowsValidationError() = runTest {
        val viewModel = OnboardingViewModel(FakeOnboardingSettingsRepository(), SavedStateHandle())
        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO

        viewModel.updateAge("12") // Invalid age
        viewModel.goToNextStep()
        advanceUntilIdle()

        assertEquals(OnboardingStep.BASIC_INFO, viewModel.uiState.value.currentStep)
        assertNotNull(viewModel.uiState.value.validationErrors["age"])
    }

    @Test
    fun invalidWeightShowsValidationError() = runTest {
        val viewModel = OnboardingViewModel(FakeOnboardingSettingsRepository(), SavedStateHandle())
        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO

        viewModel.updateCurrentWeightKg("20") // Invalid weight
        viewModel.goToNextStep()
        advanceUntilIdle()

        assertEquals(OnboardingStep.BASIC_INFO, viewModel.uiState.value.currentStep)
        assertNotNull(viewModel.uiState.value.validationErrors["currentWeight"])
    }

    @Test
    fun invalidReminderIntervalShowsValidationError() = runTest {
        val viewModel = OnboardingViewModel(FakeOnboardingSettingsRepository(), SavedStateHandle())
        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        advanceUntilIdle()

        viewModel.updateWaterReminderEnabled(true)
        viewModel.updateWaterReminderIntervalMinutes("10") // Invalid interval (< 15)
        viewModel.goToNextStep()
        advanceUntilIdle()

        assertEquals(OnboardingStep.PREFERENCES, viewModel.uiState.value.currentStep)
        assertNotNull(viewModel.uiState.value.validationErrors["reminder_interval"])
    }

    @Test
    fun finishOnboardingBuildsExpectedGoalSettings() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.updateWaterTargetMl("3000")
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertNotNull(repository.lastGoals)
        assertEquals(3000, repository.lastGoals?.waterTargetMl)
    }

    @Test
    fun finishOnboardingPersistsDashboardVisibility() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        // Keep defaults, which includes NUTRITION and HYDRATION, doesn't include CAFFEINE
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertTrue(repository.dashboardVisibilities[DashboardCardType.NUTRITION] == true)
        assertTrue(repository.dashboardVisibilities[DashboardCardType.HYDRATION] == true)
        assertTrue(repository.dashboardVisibilities[DashboardCardType.CAFFEINE] == false)
    }

    @Test
    fun finishOnboardingSavesWaterReminderPreference() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.updateWaterReminderEnabled(true)
        viewModel.updateWaterReminderIntervalMinutes("60")
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertNotNull(repository.lastWaterReminderSettings)
        assertTrue(repository.lastWaterReminderSettings?.enabled == true)
        assertEquals(60, repository.lastWaterReminderSettings?.intervalMinutes)
    }

    @Test
    fun finishOnboardingSavesStepTrackingPreference() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.updateStepTrackingPreferred(true)
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertTrue(repository.lastStepTrackingEnabled == false) // As per new safety requirement
    }

    @Test
    fun saveFailureUpdatesUiStateWithError() = runTest {
        val repository = object : SettingsRepository by FakeOnboardingSettingsRepository() {
            override suspend fun completeOnboarding(
                profile: UserProfile,
                goals: GoalSettings,
                initialMeasurement: BodyMeasurementEntry,
                supplements: List<String>,
                useDefaultSupplementsWhenEmpty: Boolean,
                dashboardCards: List<DashboardCardConfig>?,
                waterReminderSettings: WaterReminderSettings?,
                stepTrackingEnabled: Boolean?,
            ): Unit = error("Database error")
        }
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.saveError)
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(OnboardingStep.DONE, viewModel.uiState.value.currentStep)
    }

    @Test
    fun finishOnboardingSavesDailyStepTarget() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.updateDailyStepTarget("12345")
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertEquals(12345, repository.lastGoals?.dailyStepTarget)
    }

    @Test
    fun finishOnboardingSavesDailyCaffeineLimit() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.onTrackingAreaToggled(DashboardCardType.CAFFEINE)
        advanceUntilIdle()
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.updateDailyCaffeineLimitMg("350")
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertEquals(350, repository.lastGoals?.dailyCaffeineLimitMg)
    }

    @Test
    fun finishOnboardingSavesCaffeineCutoffTime() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.onTrackingAreaToggled(DashboardCardType.CAFFEINE)
        advanceUntilIdle()
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.updateCaffeineCutoffTime("14:30")
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertEquals(LocalTime.parse("14:30"), repository.lastGoals?.caffeineCutoffTime)
    }

    @Test
    fun finishOnboardingDoesNotCreateSupplementsWhenSupplementsUnselected() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        // Ensure SUPPLEMENTS is not selected (it is not in the default set)
        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertTrue(repository.dashboardVisibilities[DashboardCardType.SUPPLEMENTS] == false)
        assertTrue(repository.lastSupplements?.isEmpty() == true)
        assertEquals(false, repository.lastUseDefaultSupplementsWhenEmpty)
    }

    @Test
    fun onboardingSelectedSupplements_showsCardAndCreatesTemplates() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.onTrackingAreaToggled(DashboardCardType.SUPPLEMENTS)
        advanceUntilIdle()
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertTrue(repository.dashboardVisibilities[DashboardCardType.SUPPLEMENTS] == true)
        assertEquals(true, repository.lastUseDefaultSupplementsWhenEmpty)
    }

    @Test
    fun onboardingUnselectedCaffeine_hidesCaffeineCard() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        // CAFFEINE is not selected by default
        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertEquals(false, repository.dashboardVisibilities[DashboardCardType.CAFFEINE])
    }

    @Test
    fun onboardingUnselectedSmoking_hidesSmokingCard() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        // SMOKING is not selected by default
        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        assertEquals(false, repository.dashboardVisibilities[DashboardCardType.SMOKING])
    }

    @Test
    fun skipWithDefaults_usesDefaultDashboardVisibility() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.skipWithDefaults()
        advanceUntilIdle()

        val expectedDefaults = com.burak.healthapp.domain.model.defaultDashboardCardConfig()
        expectedDefaults.forEach {
            assertEquals(it.isVisible, repository.dashboardVisibilities[it.type])
        }
    }

    @Test
    fun onboardingDashboardConfig_keepsDefaultSortOrder() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        viewModel.goToNextStep() // PREFERENCES
        viewModel.goToNextStep() // DONE
        viewModel.goToNextStep() // FINISH
        advanceUntilIdle()

        val savedCards = repository.lastDashboardCards
        assertNotNull(savedCards)

        val expectedDefaults = com.burak.healthapp.domain.model.defaultDashboardCardConfig()
        assertEquals(expectedDefaults.size, savedCards!!.size)

        savedCards.forEachIndexed { index, config ->
            assertEquals(expectedDefaults[index].type, config.type)
        }
    }

    @Test
    fun invalidCaffeineCutoffBlocksSmartGoalsNext() = runTest {
        val repository = FakeOnboardingSettingsRepository()
        val viewModel = OnboardingViewModel(repository, SavedStateHandle())

        viewModel.goToNextStep() // TRACKING_AREAS
        viewModel.onTrackingAreaToggled(DashboardCardType.CAFFEINE)
        advanceUntilIdle()
        viewModel.goToNextStep() // BASIC_INFO
        viewModel.goToNextStep() // ACTIVITY_GOAL
        viewModel.goToNextStep() // SMART_GOALS
        advanceUntilIdle()
        assertEquals(OnboardingStep.SMART_GOALS, viewModel.uiState.value.currentStep)

        viewModel.updateCaffeineCutoffTime("bad-time")
        viewModel.goToNextStep()
        advanceUntilIdle()

        assertEquals(OnboardingStep.SMART_GOALS, viewModel.uiState.value.currentStep)
        assertNotNull(viewModel.uiState.value.validationErrors[OnboardingFieldKeys.CAFFEINE_CUTOFF])
    }

    @Test
    fun restoreStateWithInvalidEnumNamesDoesNotCrash() = runTest {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "currentStep" to "INVALID_STEP",
                "selectedTrackingAreas" to listOf("INVALID_AREA", "BOGUS"),
                "sex" to "ALIEN",
                "activityLevel" to "EXTREME",
                "mainGoal" to "FLY",
            ),
        )
        val viewModel = OnboardingViewModel(FakeOnboardingSettingsRepository(), savedStateHandle)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(OnboardingStep.WELCOME, state.currentStep)
        assertEquals(OnboardingSex.UNSPECIFIED, state.sex)
        assertEquals(OnboardingActivityLevel.LIGHT, state.activityLevel)
        assertEquals(OnboardingMainGoal.MAINTAIN, state.mainGoal)
        // Invalid tracking areas fallback to default set
        assertTrue(state.selectedTrackingAreas.isNotEmpty())
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingMainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

class FakeOnboardingSettingsRepository : SettingsRepository {
    var lastProfile: UserProfile? = null
    var lastGoals: GoalSettings? = null
    var lastMeasurement: BodyMeasurementEntry? = null
    var lastSupplements: List<String>? = null
    var lastWaterReminderSettings: WaterReminderSettings? = null
    var lastStepTrackingEnabled: Boolean? = null
    var lastUseDefaultSupplementsWhenEmpty: Boolean? = null
    var lastDashboardCards: List<DashboardCardConfig>? = null
    val dashboardVisibilities = mutableMapOf<DashboardCardType, Boolean>()

    override val settings: Flow<SettingsState> = MutableStateFlow(SettingsState())
    override fun observeSupplementTemplates(): Flow<List<SupplementTemplate>> = emptyFlow()

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
        lastProfile = profile
        lastGoals = goals
        lastMeasurement = initialMeasurement
        lastSupplements = supplements
        lastUseDefaultSupplementsWhenEmpty = useDefaultSupplementsWhenEmpty
        if (dashboardCards != null) {
            dashboardCards.forEach {
                dashboardVisibilities[it.type] = it.isVisible
            }
        }
        if (waterReminderSettings != null) {
            lastWaterReminderSettings = waterReminderSettings
        }
        if (stepTrackingEnabled != null) {
            lastStepTrackingEnabled = stepTrackingEnabled
        }
        if (dashboardCards != null) {
            lastDashboardCards = dashboardCards
        }
    }

    override suspend fun updateGoalSettings(goals: GoalSettings) = Unit
    override suspend fun updateWaterReminderSettings(settings: WaterReminderSettings) {
        lastWaterReminderSettings = settings
    }
    override suspend fun updateWaterReminderSnoozedDate(date: LocalDate?) = Unit
    override suspend fun updateStepTrackingEnabled(enabled: Boolean) {
        lastStepTrackingEnabled = enabled
    }
    override suspend fun updateDashboardCardVisibility(type: DashboardCardType, isVisible: Boolean) {
        dashboardVisibilities[type] = isVisible
    }
    override suspend fun moveDashboardCard(type: DashboardCardType, newIndex: Int) = Unit
    override suspend fun resetDashboardCardsToDefault() = Unit
    override suspend fun updateProfile(profile: UserProfile) = Unit
    override suspend fun updateThemeMode(mode: ThemeMode) = Unit
    override suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>) = Unit
}
