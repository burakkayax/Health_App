package com.burak.healthapp

import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TodaySnapshot
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.feature.profile.ProfileViewModel
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addSupplementDraft_marksBlankFieldsAsInvalid() = runTest {
        val viewModel = createViewModel()
        collectUiState(viewModel)

        viewModel.openSupplementEditor()
        viewModel.addSupplementDraft()
        advanceUntilIdle()

        val editor = viewModel.uiState.value.supplementEditor
        val draft = editor.drafts.single()

        assertTrue(editor.isVisible)
        assertFalse(editor.canSave)
        assertEquals(
            UiText.StringResource(R.string.error_fix_missing_fields),
            editor.validationMessage,
        )
        assertEquals(
            UiText.StringResource(R.string.error_supplement_name_required),
            draft.nameError,
        )
        assertEquals(
            UiText.StringResource(R.string.error_supplement_target_required),
            draft.targetAmountError,
        )
    }

    @Test
    fun duplicateSupplementNames_areRejected() = runTest {
        val viewModel = createViewModel()
        collectUiState(viewModel)

        viewModel.openSupplementEditor()
        viewModel.addSupplementDraft()
        viewModel.addSupplementDraft()
        advanceUntilIdle()

        val drafts = viewModel.uiState.value.supplementEditor.drafts
        viewModel.updateSupplementName(drafts[0].draftId, "Magnezyum")
        viewModel.updateSupplementTargetAmount(drafts[0].draftId, "200")
        viewModel.updateSupplementName(drafts[1].draftId, "magnezyum")
        viewModel.updateSupplementTargetAmount(drafts[1].draftId, "150")
        advanceUntilIdle()

        val editor = viewModel.uiState.value.supplementEditor

        assertFalse(editor.canSave)
        assertEquals(
            UiText.StringResource(R.string.error_supplement_duplicate_name),
            editor.validationMessage,
        )
        assertTrue(
            editor.drafts.all {
                it.nameError == UiText.StringResource(R.string.error_supplement_duplicate_name)
            },
        )
    }

    @Test
    fun saveSupplementTemplates_keepsEditorOpenWhenRepositoryFails() = runTest {
        val settingsRepository = FakeProfileSettingsRepository(throwOnReplace = true)
        val viewModel = createViewModel(settingsRepository = settingsRepository)
        collectUiState(viewModel)

        viewModel.openSupplementEditor()
        viewModel.addSupplementDraft()
        advanceUntilIdle()

        val draftId = viewModel.uiState.value.supplementEditor.drafts.single().draftId
        viewModel.updateSupplementName(draftId, "B12")
        viewModel.updateSupplementTargetAmount(draftId, "500")
        viewModel.updateSupplementUnitLabel(draftId, "mcg")
        advanceUntilIdle()

        viewModel.saveSupplementTemplates()
        advanceUntilIdle()

        val editor = viewModel.uiState.value.supplementEditor
        assertTrue(editor.isVisible)
        assertTrue(editor.canSave)
        assertEquals(
            UiText.StringResource(R.string.error_supplement_save_failed),
            editor.saveErrorMessage,
        )
        assertNotNull(settingsRepository.lastReplaceAttempt)
    }

    private fun TestScope.collectUiState(viewModel: ProfileViewModel) {
        backgroundScope.launch {
            viewModel.uiState.collect { }
        }
    }

    private fun createViewModel(
        settingsRepository: FakeProfileSettingsRepository = FakeProfileSettingsRepository(),
        dashboardRepository: DashboardRepository = FakeProfileDashboardRepository(),
    ): ProfileViewModel {
        return ProfileViewModel(
            settingsRepository = settingsRepository,
            dashboardRepository = dashboardRepository,
        )
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

private class FakeProfileSettingsRepository(
    private val throwOnReplace: Boolean = false,
) : SettingsRepository {
    override val settings: StateFlow<SettingsState> = MutableStateFlow(
        SettingsState(
            onboardingCompleted = true,
            userProfile = UserProfile(name = "Burak", avatarInitials = "BK"),
            goalSettings = GoalSettings(),
            themeMode = ThemeMode.SYSTEM,
        ),
    ).asStateFlow()

    private val templatesFlow = MutableStateFlow<List<SupplementTemplate>>(emptyList())
    var lastReplaceAttempt: List<SupplementTemplate>? = null

    override fun observeSupplementTemplates(): Flow<List<SupplementTemplate>> = templatesFlow

    override suspend fun completeOnboarding(
        profile: UserProfile,
        goals: GoalSettings,
        initialMeasurement: BodyMeasurementEntry,
        supplements: List<String>,
    ) = Unit

    override suspend fun updateGoalSettings(goals: GoalSettings) = Unit

    override suspend fun updateWaterReminderSettings(settings: WaterReminderSettings) = Unit

    override suspend fun updateProfile(profile: UserProfile) = Unit

    override suspend fun updateThemeMode(mode: ThemeMode) = Unit

    override suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>) {
        lastReplaceAttempt = templates
        if (throwOnReplace) error("save failed")
        templatesFlow.value = templates
    }
}

private class FakeProfileDashboardRepository : DashboardRepository {
    override fun observeToday(date: LocalDate): Flow<TodaySnapshot> {
        return flowOf(
            TodaySnapshot(
                settings = SettingsState(),
                meals = emptyList<MealEntry>(),
                hydrationEntries = emptyList(),
                sleepSessionForDate = null,
                exerciseEntryForDate = null,
                weekExerciseEntries = emptyList(),
                smokingEntryForDate = null,
                stepEntryForDate = null,
                weekStepEntries = emptyList(),
                supplementTemplates = emptyList(),
                supplementDoseEntries = emptyList(),
                measurementForDate = null,
            ),
        )
    }

    override fun observeMealsForDate(date: LocalDate): Flow<List<MealEntry>> = flowOf(emptyList())

    override fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?> = flowOf(null)

    override fun observeWeightHistory(): Flow<List<BodyMeasurementEntry>> = flowOf(emptyList())

    override fun observeSleepSessionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSession>> {
        return flowOf(emptyList())
    }

    override fun observeStepsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntry>> {
        return flowOf(emptyList())
    }

    override suspend fun saveMealEntry(entry: MealEntry) = Unit

    override suspend fun deleteMealEntry(id: Long) = Unit

    override suspend fun deleteHydrationEntry(id: Long) = Unit

    override suspend fun deleteSleepForDate(date: LocalDate) = Unit

    override suspend fun deleteExerciseForDate(date: LocalDate) = Unit

    override suspend fun deleteSmokingForDate(date: LocalDate) = Unit

    override suspend fun deleteSupplementDoseForDate(templateId: Long, date: LocalDate) = Unit

    override suspend fun deleteStepsForDate(date: LocalDate) = Unit

    override suspend fun deleteBodyMeasurement(id: Long) = Unit

    override suspend fun addHydration(amountMl: Int, date: LocalDate) = Unit

    override suspend fun saveSleepSession(session: SleepSession) = Unit

    override suspend fun saveExerciseEntry(entry: ExerciseEntry, date: LocalDate) = Unit

    override suspend fun saveSmokingCount(count: Int, date: LocalDate) = Unit

    override suspend fun incrementSmokingCount(date: LocalDate, delta: Int) = Unit

    override suspend fun saveSupplementDoseEntries(entries: List<SupplementDoseEntry>, date: LocalDate) = Unit

    override suspend fun saveBodyMeasurement(entry: BodyMeasurementEntry) = Unit

    override suspend fun saveWeightMeasurement(weightKg: Float, date: LocalDate) = Unit

    override suspend fun recordStepSensorValue(sensorValue: Int, date: LocalDate) = Unit
}
