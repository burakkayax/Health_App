package com.burak.healthapp

import android.net.Uri
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.data.export.HealthDataExportFileWriter
import com.burak.healthapp.data.export.HealthDataImportFileReader
import com.burak.healthapp.data.export.JsonHealthDataExporter
import com.burak.healthapp.data.export.JsonHealthDataImporter
import com.burak.healthapp.domain.export.ExportedGoalSettings
import com.burak.healthapp.domain.export.ExportedMealEntry
import com.burak.healthapp.domain.export.ExportedUserProfile
import com.burak.healthapp.domain.export.ExportedWaterReminderSettings
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.HealthDataImportException
import com.burak.healthapp.domain.export.ImportValidationError
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
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
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.HealthDataExportRepository
import com.burak.healthapp.domain.repository.HealthDataManagementRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.usecase.DeleteAllHealthDataUseCase
import com.burak.healthapp.domain.usecase.ExportHealthDataUseCase
import com.burak.healthapp.domain.usecase.ImportHealthDataUseCase
import com.burak.healthapp.feature.profile.ProfileViewModel
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.Instant
import java.time.LocalDate

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

    @Test
    fun loadImportPreview_withValidJsonStoresPreviewAndConfirmImportsModel() = runTest {
        val managementRepository = RecordingHealthDataManagementRepository()
        val viewModel = createViewModel(
            healthDataManagementRepository = managementRepository,
        )
        collectUiState(viewModel)

        viewModel.loadImportPreviewJson(JsonHealthDataExporter().encode(profileImportModel()))
        advanceUntilIdle()

        val preview = viewModel.uiState.value.exportState.importPreview
        assertNotNull(preview)
        assertEquals(1, preview?.mealCount)
        assertNull(viewModel.uiState.value.exportState.message)

        viewModel.confirmImport()
        advanceUntilIdle()

        val exportState = viewModel.uiState.value.exportState
        assertEquals(1, managementRepository.importedModels.size)
        assertNull(exportState.importPreview)
        assertEquals(
            UiText.StringResource(R.string.import_success),
            exportState.message,
        )
    }

    @Test
    fun loadImportPreview_withInvalidJsonShowsErrorAndDoesNotStorePreview() = runTest {
        val viewModel = createViewModel()
        collectUiState(viewModel)

        viewModel.loadImportPreviewJson("{not-json")
        advanceUntilIdle()

        val exportState = viewModel.uiState.value.exportState
        assertNull(exportState.importPreview)
        assertTrue(exportState.isError)
        assertEquals(
            UiText.StringResource(R.string.import_error_invalid_json),
            exportState.message,
        )
    }

    @Test
    fun confirmImport_withSettingsFailureShowsTypedError() = runTest {
        val managementRepository = FailingHealthDataManagementRepository(
            HealthDataImportException(ImportValidationError.SettingsFailure),
        )
        val viewModel = createViewModel(
            healthDataManagementRepository = managementRepository,
        )
        collectUiState(viewModel)

        viewModel.loadImportPreviewJson(JsonHealthDataExporter().encode(profileImportModel()))
        advanceUntilIdle()

        viewModel.confirmImport()
        advanceUntilIdle()

        val exportState = viewModel.uiState.value.exportState
        assertTrue(exportState.isError)
        assertFalse(exportState.isImporting)
        assertEquals(
            UiText.StringResource(R.string.import_error_settings_failure),
            exportState.message,
        )
    }

    @Test
    fun deleteAllHealthData_requiresConfirmationAndReportsSuccess() = runTest {
        val managementRepository = RecordingHealthDataManagementRepository()
        val viewModel = createViewModel(healthDataManagementRepository = managementRepository)
        collectUiState(viewModel)

        viewModel.requestDeleteAllHealthData()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.exportState.showDeleteConfirmation)

        viewModel.dismissDeleteAllConfirmation()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.exportState.showDeleteConfirmation)
        assertEquals(0, managementRepository.deleteCount)

        viewModel.requestDeleteAllHealthData()
        viewModel.confirmDeleteAllHealthData()
        advanceUntilIdle()

        val exportState = viewModel.uiState.value.exportState
        assertEquals(1, managementRepository.deleteCount)
        assertFalse(exportState.showDeleteConfirmation)
        assertEquals(
            UiText.StringResource(R.string.delete_health_data_success),
            exportState.message,
        )
    }

    private fun TestScope.collectUiState(viewModel: ProfileViewModel) {
        backgroundScope.launch {
            viewModel.uiState.collect { }
        }
    }

    private fun createViewModel(
        settingsRepository: FakeProfileSettingsRepository = FakeProfileSettingsRepository(),
        dashboardRepository: DashboardRepository = FakeProfileDashboardRepository(),
        importFileReader: HealthDataImportFileReader = EmptyImportFileReader,
        healthDataManagementRepository: HealthDataManagementRepository = NoOpHealthDataManagementRepository,
    ): ProfileViewModel = ProfileViewModel(
        settingsRepository = settingsRepository,
        dashboardRepository = dashboardRepository,
        exportHealthDataUseCase = ExportHealthDataUseCase(
            repository = EmptyHealthDataExportRepository,
            jsonExporter = JsonHealthDataExporter(),
            appVersion = "test",
        ),
        exportFileWriter = NoOpExportFileWriter,
        importFileReader = importFileReader,
        jsonImporter = JsonHealthDataImporter(),
        importHealthDataUseCase = ImportHealthDataUseCase(healthDataManagementRepository),
        deleteAllHealthDataUseCase = DeleteAllHealthDataUseCase(healthDataManagementRepository),
    )
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
        useDefaultSupplementsWhenEmpty: Boolean,
        dashboardCards: List<com.burak.healthapp.domain.model.DashboardCardConfig>?,
        waterReminderSettings: WaterReminderSettings?,
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

    override suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>) {
        lastReplaceAttempt = templates
        if (throwOnReplace) error("save failed")
        templatesFlow.value = templates
    }
}

private class FakeProfileDashboardRepository : DashboardRepository {
    override fun observeToday(date: LocalDate): Flow<TodaySnapshot> = flowOf(
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
            caffeineEntries = emptyList(),
            supplementTemplates = emptyList(),
            supplementDoseEntries = emptyList(),
            measurementForDate = null,
        ),
    )

    override fun observeMealsForDate(date: LocalDate): Flow<List<MealEntry>> = flowOf(emptyList())

    override fun observeHydrationBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<HydrationEntry>> = flowOf(emptyList())

    override fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?> = flowOf(null)

    override fun observeWeightHistory(): Flow<List<BodyMeasurementEntry>> = flowOf(emptyList())

    override fun observeSleepSessionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSession>> = flowOf(emptyList())

    override fun observeStepsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntry>> = flowOf(emptyList())

    override fun observeCaffeineForDate(date: LocalDate): Flow<List<CaffeineEntry>> = flowOf(emptyList())

    override fun observeCaffeineBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<CaffeineEntry>> = flowOf(emptyList())

    override fun observeSmokingBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SmokingEntry>> = flowOf(emptyList())

    override fun observeExerciseBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<ExerciseEntry>> = flowOf(emptyList())

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

    override suspend fun addCaffeine(entry: CaffeineEntry) = Unit

    override suspend fun deleteCaffeine(id: Long) = Unit
}

private object EmptyHealthDataExportRepository : HealthDataExportRepository {
    override suspend fun buildExportModel(
        exportedAt: Instant,
        appVersion: String,
    ): HealthDataExportModel {
        val goals = GoalSettings()
        val reminder = WaterReminderSettings()
        return HealthDataExportModel(
            exportedAt = exportedAt.toString(),
            appVersion = appVersion,
            profile = ExportedUserProfile(
                name = "Burak",
                avatarInitials = "BK",
                heightCm = null,
            ),
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
                enabled = reminder.enabled,
                startTime = reminder.startTime.toString(),
                endTime = reminder.endTime.toString(),
                intervalMinutes = reminder.intervalMinutes,
            ),
            themeMode = ThemeMode.SYSTEM.name,
        )
    }
}

private object NoOpExportFileWriter : HealthDataExportFileWriter {
    override suspend fun writeJson(uri: Uri, json: String) = Unit
}

private object EmptyImportFileReader : HealthDataImportFileReader {
    override suspend fun readText(uri: Uri): String = ""
}

private object NoOpHealthDataManagementRepository : HealthDataManagementRepository {
    override suspend fun importHealthData(model: HealthDataExportModel) = Unit

    override suspend fun deleteAllHealthData() = Unit
}

private class RecordingHealthDataManagementRepository : HealthDataManagementRepository {
    val importedModels = mutableListOf<HealthDataExportModel>()
    var deleteCount = 0

    override suspend fun importHealthData(model: HealthDataExportModel) {
        importedModels += model
    }

    override suspend fun deleteAllHealthData() {
        deleteCount++
    }
}

private class FailingHealthDataManagementRepository(
    private val throwable: Throwable,
) : HealthDataManagementRepository {
    override suspend fun importHealthData(model: HealthDataExportModel): Unit = throw throwable

    override suspend fun deleteAllHealthData() = Unit
}

private fun profileImportModel(): HealthDataExportModel {
    val goals = GoalSettings()
    val reminder = WaterReminderSettings()
    return HealthDataExportModel(
        exportedAt = "2026-04-27T10:15:30Z",
        appVersion = "1.0-test",
        profile = ExportedUserProfile(
            name = "Burak",
            avatarInitials = "BK",
            heightCm = 182f,
        ),
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
            enabled = reminder.enabled,
            startTime = reminder.startTime.toString(),
            endTime = reminder.endTime.toString(),
            intervalMinutes = reminder.intervalMinutes,
        ),
        themeMode = ThemeMode.SYSTEM.name,
        meals = listOf(
            ExportedMealEntry(
                id = 10,
                date = "2026-04-27",
                mealType = "BREAKFAST",
                name = "Yulaf",
                calories = 300,
                carbsGrams = 40,
                fatGrams = 8,
                proteinGrams = 20,
                createdAt = "2026-04-27T08:00:00",
            ),
        ),
    )
}
