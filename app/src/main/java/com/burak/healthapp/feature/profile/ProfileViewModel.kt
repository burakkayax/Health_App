package com.burak.healthapp.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.R
import com.burak.healthapp.core.reminder.WaterReminderSettingsApplier
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.data.export.HealthDataExportFileWriter
import com.burak.healthapp.data.export.HealthDataImportFileReader
import com.burak.healthapp.domain.calculation.formatClockRange
import com.burak.healthapp.domain.export.HealthDataExportModel
import com.burak.healthapp.domain.export.HealthDataImportException
import com.burak.healthapp.domain.export.HealthDataJsonImporter
import com.burak.healthapp.domain.export.ImportValidationError
import com.burak.healthapp.domain.export.ImportValidationResult
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.usecase.DeleteAllHealthDataUseCase
import com.burak.healthapp.domain.usecase.ExportHealthDataUseCase
import com.burak.healthapp.domain.usecase.ImportHealthDataUseCase
import com.burak.healthapp.domain.validation.parseLocalizedDecimalInput
import com.burak.healthapp.feature.profile.EditableSupplementTemplateState
import com.burak.healthapp.feature.profile.ProfileExportUiState
import com.burak.healthapp.feature.profile.ProfileGoalSummaryState
import com.burak.healthapp.feature.profile.ProfileSupplementTemplateState
import com.burak.healthapp.feature.profile.ProfileUiState
import com.burak.healthapp.feature.profile.SupplementEditorUiState
import com.burak.healthapp.feature.profile.toDomainTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
    private val exportHealthDataUseCase: ExportHealthDataUseCase,
    private val exportFileWriter: HealthDataExportFileWriter,
    private val importFileReader: HealthDataImportFileReader,
    private val jsonImporter: HealthDataJsonImporter,
    private val importHealthDataUseCase: ImportHealthDataUseCase,
    private val deleteAllHealthDataUseCase: DeleteAllHealthDataUseCase,
    private val waterReminderSettingsApplier: WaterReminderSettingsApplier = WaterReminderSettingsApplier {},
) : ViewModel() {
    private var nextDraftId = 1L
    private var latestTemplates: List<SupplementTemplate> = emptyList()
    private var pendingImportModel: HealthDataExportModel? = null

    private val editorState = MutableStateFlow(SupplementEditorUiState())
    private val exportState = MutableStateFlow(ProfileExportUiState())
    private val supplementTemplates = settingsRepository.observeSupplementTemplates()
        .onEach { templates -> latestTemplates = templates }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val uiState = combine(
        settingsRepository.settings,
        supplementTemplates,
        dashboardRepository.observeLatestMeasurement(),
        editorState,
        exportState,
    ) { settings, templates, latestMeasurement, editor, export ->
        settings.toProfileUiState(
            supplementTemplates = templates,
            latestMeasurement = latestMeasurement,
            supplementEditor = editor,
            exportState = export,
        )
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState(
                userName = "Misafir",
                avatarInitials = "M",
                themeMode = ThemeMode.SYSTEM,
                goalSummaries = emptyList(),
                supplementTemplates = emptyList(),
                supplementEditor = SupplementEditorUiState(),
                exportState = ProfileExportUiState(),
            ),
        )

    fun openSupplementEditor() {
        val drafts = latestTemplates.map { template ->
            EditableSupplementTemplateState(
                draftId = createDraftId(),
                id = template.id,
                name = template.name,
                targetAmount = formatEditableAmount(template.targetAmount),
                unitLabel = template.unitLabel,
            )
        }
        editorState.value = validateEditorState(
            SupplementEditorUiState(
                isVisible = true,
                drafts = drafts,
            ),
        )
    }

    fun dismissSupplementEditor() {
        editorState.value = SupplementEditorUiState()
    }

    fun addSupplementDraft() {
        updateEditorDrafts { drafts ->
            drafts + EditableSupplementTemplateState(
                draftId = createDraftId(),
                unitLabel = "mg",
            )
        }
    }

    fun updateSupplementName(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(name = value) }
    }

    fun updateSupplementTargetAmount(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(targetAmount = value) }
    }

    fun updateSupplementUnitLabel(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(unitLabel = value) }
    }

    fun removeSupplementDraft(draftId: Long) {
        updateEditorDrafts { drafts -> drafts.filterNot { it.draftId == draftId } }
    }

    fun saveSupplementTemplates() {
        val currentState = validateEditorState(editorState.value)
        editorState.value = currentState
        if (!currentState.canSave) return

        viewModelScope.launch {
            editorState.value = currentState.copy(
                isSaving = true,
                canSave = false,
                saveErrorMessage = null,
            )

            try {
                settingsRepository.replaceSupplementTemplates(
                    currentState.drafts.mapIndexed { index, draft ->
                        draft.toDomainTemplate(sortOrder = index)
                    },
                )
                editorState.value = SupplementEditorUiState()
            } catch (_: Exception) {
                editorState.value = validateEditorState(
                    currentState.copy(
                        isSaving = false,
                        saveErrorMessage = UiText.StringResource(R.string.error_supplement_save_failed),
                    ),
                    clearSaveError = false,
                )
            }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    fun updateStepTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateStepTrackingEnabled(enabled)
        }
    }

    fun updateWaterReminderSettings(settings: WaterReminderSettings) {
        viewModelScope.launch {
            settingsRepository.updateWaterReminderSettings(settings)
            waterReminderSettingsApplier.apply(settings)
        }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            exportState.value = exportState.value.copy(
                isExporting = true,
                message = null,
                isError = false,
            )
            runCatching {
                val json = exportHealthDataUseCase.exportJson()
                exportFileWriter.writeJson(uri, json)
            }.onSuccess {
                exportState.value = exportState.value.copy(
                    isExporting = false,
                    message = UiText.StringResource(R.string.export_success),
                    isError = false,
                )
            }.onFailure {
                exportState.value = exportState.value.copy(
                    isExporting = false,
                    message = UiText.StringResource(R.string.export_failed),
                    isError = true,
                )
            }
        }
    }

    fun loadImportPreview(uri: Uri) {
        viewModelScope.launch {
            exportState.value = exportState.value.copy(
                isImporting = true,
                importPreview = null,
                message = null,
                isError = false,
            )
            try {
                val rawJson = importFileReader.readText(uri)
                handleImportPreviewJson(rawJson)
            } catch (exception: Exception) {
                exportState.value = exportState.value.copy(
                    isImporting = false,
                    message = exception.toImportUiText(),
                    isError = true,
                )
            }
        }
    }

    internal fun loadImportPreviewJson(rawJson: String) = viewModelScope.launch {
        handleImportPreviewJson(rawJson)
    }

    private fun handleImportPreviewJson(rawJson: String) {
        pendingImportModel = null
        exportState.value = exportState.value.copy(
            isImporting = true,
            importPreview = null,
            message = null,
            isError = false,
        )

        when (val result = jsonImporter.validate(rawJson)) {
            is ImportValidationResult.Valid -> {
                pendingImportModel = result.model
                exportState.value = exportState.value.copy(
                    isImporting = false,
                    importPreview = result.preview,
                )
            }
            is ImportValidationResult.Invalid -> {
                exportState.value = exportState.value.copy(
                    isImporting = false,
                    message = result.error.toUiText(),
                    isError = true,
                )
            }
        }
    }

    fun dismissImportPreview() {
        pendingImportModel = null
        exportState.value = exportState.value.copy(importPreview = null)
    }

    fun confirmImport() {
        val model = pendingImportModel ?: return
        viewModelScope.launch {
            exportState.value = exportState.value.copy(isImporting = true)
            runCatching {
                importHealthDataUseCase.import(model)
            }.onSuccess {
                pendingImportModel = null
                exportState.value = exportState.value.copy(
                    isImporting = false,
                    importPreview = null,
                    message = UiText.StringResource(R.string.import_success),
                    isError = false,
                )
            }.onFailure { exception ->
                exportState.value = exportState.value.copy(
                    isImporting = false,
                    message = exception.toImportUiText(),
                    isError = true,
                )
            }
        }
    }

    fun requestDeleteAllHealthData() {
        exportState.value = exportState.value.copy(showDeleteConfirmation = true)
    }

    fun dismissDeleteAllConfirmation() {
        exportState.value = exportState.value.copy(showDeleteConfirmation = false)
    }

    fun confirmDeleteAllHealthData() {
        viewModelScope.launch {
            exportState.value = exportState.value.copy(
                isDeleting = true,
                showDeleteConfirmation = false,
                message = null,
                isError = false,
            )
            runCatching {
                deleteAllHealthDataUseCase.deleteAllHealthData()
            }.onSuccess {
                exportState.value = exportState.value.copy(
                    isDeleting = false,
                    message = UiText.StringResource(R.string.delete_health_data_success),
                    isError = false,
                )
            }.onFailure {
                exportState.value = exportState.value.copy(
                    isDeleting = false,
                    message = UiText.StringResource(R.string.delete_health_data_failed),
                    isError = true,
                )
            }
        }
    }

    private fun updateDraft(
        draftId: Long,
        transform: (EditableSupplementTemplateState) -> EditableSupplementTemplateState,
    ) {
        updateEditorDrafts { drafts ->
            drafts.map { draft ->
                if (draft.draftId == draftId) transform(draft) else draft
            }
        }
    }

    private fun updateEditorDrafts(
        transform: (List<EditableSupplementTemplateState>) -> List<EditableSupplementTemplateState>,
    ) {
        editorState.value = validateEditorState(
            editorState.value.copy(
                drafts = transform(editorState.value.drafts),
            ),
        )
    }

    private fun validateEditorState(
        state: SupplementEditorUiState,
        clearSaveError: Boolean = true,
    ): SupplementEditorUiState {
        val draftsWithValidation = attachValidation(state.drafts)
        val hasErrors = draftsWithValidation.any { draft ->
            draft.nameError != null || draft.targetAmountError != null || draft.unitLabelError != null
        }
        val validationMessage = when {
            draftsWithValidation.any { it.nameError == DUPLICATE_NAME_ERROR } -> DUPLICATE_NAME_ERROR
            hasErrors -> UiText.StringResource(R.string.error_fix_missing_fields)
            else -> null
        }

        return state.copy(
            drafts = draftsWithValidation,
            validationMessage = validationMessage,
            canSave = !hasErrors && !state.isSaving,
            saveErrorMessage = if (clearSaveError) null else state.saveErrorMessage,
        )
    }

    private fun attachValidation(
        drafts: List<EditableSupplementTemplateState>,
    ): List<EditableSupplementTemplateState> {
        val normalizedNames = drafts
            .map { draft -> draft.draftId to draft.name.trim().lowercase() }
            .filter { (_, name) -> name.isNotBlank() }
            .groupBy(
                keySelector = { (_, name) -> name },
                valueTransform = { (draftId, _) -> draftId },
            )
        val duplicateDraftIds = normalizedNames
            .values
            .filter { ids -> ids.size > 1 }
            .flatten()
            .toSet()

        return drafts.map { draft ->
            val trimmedName = draft.name.trim()
            val trimmedAmount = draft.targetAmount.trim()
            val trimmedUnit = draft.unitLabel.trim()
            val parsedAmount = parseLocalizedDecimalInput(trimmedAmount)

            draft.copy(
                name = trimmedName,
                targetAmount = trimmedAmount,
                unitLabel = trimmedUnit,
                nameError = when {
                    trimmedName.isBlank() -> UiText.StringResource(R.string.error_supplement_name_required)
                    draft.draftId in duplicateDraftIds -> DUPLICATE_NAME_ERROR
                    else -> null
                },
                targetAmountError = when {
                    trimmedAmount.isBlank() -> UiText.StringResource(R.string.error_supplement_target_required)
                    parsedAmount == null -> UiText.StringResource(R.string.error_supplement_target_must_be_number)
                    parsedAmount <= 0f -> UiText.StringResource(R.string.error_supplement_target_positive)
                    else -> null
                },
                unitLabelError = if (trimmedUnit.isBlank()) {
                    UiText.StringResource(R.string.error_supplement_unit_required)
                } else {
                    null
                },
            )
        }
    }

    private fun createDraftId(): Long = nextDraftId++
}

private val DUPLICATE_NAME_ERROR = UiText.StringResource(R.string.error_supplement_duplicate_name)

private fun Throwable.toImportUiText(): UiText = (this as? HealthDataImportException)
    ?.error
    ?.toUiText()
    ?: UiText.StringResource(R.string.import_failed)

private fun ImportValidationError.toUiText(): UiText = when (this) {
    ImportValidationError.EmptyFile -> UiText.StringResource(R.string.import_error_empty_file)
    ImportValidationError.InvalidJson -> UiText.StringResource(R.string.import_error_invalid_json)
    ImportValidationError.MissingSchemaVersion -> UiText.StringResource(R.string.import_error_missing_schema_version)
    is ImportValidationError.UnsupportedSchemaVersion -> UiText.StringResource(R.string.import_error_unsupported_schema_version)
    is ImportValidationError.MissingRequiredField -> UiText.StringResource(R.string.import_error_missing_required_field)
    is ImportValidationError.InvalidDate -> UiText.StringResource(R.string.import_error_invalid_date)
    is ImportValidationError.InvalidTime -> UiText.StringResource(R.string.import_error_invalid_time)
    is ImportValidationError.InvalidDateTime -> if (fieldPath.startsWith("sleep")) {
        UiText.StringResource(R.string.import_validation_expected_datetime_format)
    } else {
        UiText.StringResource(R.string.import_error_invalid_datetime)
    }
    is ImportValidationError.InvalidEnum -> UiText.StringResource(R.string.import_error_invalid_enum)
    is ImportValidationError.InvalidNumber -> UiText.StringResource(R.string.import_error_invalid_number)
    is ImportValidationError.NegativeValue -> UiText.StringResource(R.string.import_error_negative_value)
    is ImportValidationError.NonPositiveValue -> UiText.StringResource(R.string.import_error_negative_value)
    is ImportValidationError.InvalidRange -> UiText.StringResource(R.string.import_error_invalid_number)
    is ImportValidationError.FileTooLarge -> UiText.StringResource(R.string.import_error_file_too_large)
    ImportValidationError.DecodeFailure -> UiText.StringResource(R.string.import_error_decode_failure)
    ImportValidationError.DatabaseFailure -> UiText.StringResource(R.string.import_error_database_failure)
    ImportValidationError.SettingsFailure -> UiText.StringResource(R.string.import_error_settings_failure)
    ImportValidationError.Unknown -> UiText.StringResource(R.string.import_error_unknown)
}

private fun SettingsState.toProfileUiState(
    supplementTemplates: List<SupplementTemplate>,
    latestMeasurement: BodyMeasurementEntry?,
    supplementEditor: SupplementEditorUiState,
    exportState: ProfileExportUiState,
): ProfileUiState {
    val measurement = latestMeasurement ?: BodyMeasurementEntry(
        date = LocalDate.now(),
        weightKg = goalSettings.baselineWeightKg,
        shoulderCm = goalSettings.baselineShoulderCm,
        waistCm = goalSettings.baselineWaistCm,
        hipCm = goalSettings.baselineHipCm,
    )

    return ProfileUiState(
        userName = userProfile.name,
        avatarInitials = userProfile.avatarInitials,
        themeMode = themeMode,
        stepTrackingEnabled = stepTrackingEnabled,
        waterReminderSettings = waterReminderSettings,
        goalSummaries = goalSettings.toSummaryStates(
            measurement = measurement,
            heightCm = userProfile.heightCm,
            waterReminderSettings = waterReminderSettings,
        ),
        supplementTemplates = supplementTemplates.map { template ->
            ProfileSupplementTemplateState(
                id = template.id,
                name = template.name,
                targetAmount = template.targetAmount,
                unitLabel = template.unitLabel,
            )
        },
        supplementEditor = supplementEditor,
        exportState = exportState,
    )
}

private fun GoalSettings.toSummaryStates(
    measurement: BodyMeasurementEntry,
    heightCm: Float?,
    waterReminderSettings: WaterReminderSettings,
): List<ProfileGoalSummaryState> = listOf(
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_calories_water),
        value = UiText.StringResource(
            R.string.format_kcal_water,
            listOf(dailyCaloriesTarget, waterTargetMl),
        ),
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_macros),
        value = UiText.StringResource(
            R.string.format_macros,
            listOf(proteinTargetGrams, carbTargetGrams, fatTargetGrams),
        ),
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_sleep_target),
        value = UiText.DynamicString(formatClockRange(sleepTargetBedtime, sleepTargetWakeTime)),
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_exercise),
        value = UiText.StringResource(
            R.string.format_days_minutes,
            listOf(exerciseTargetDaysPerWeek, exerciseTargetDurationMinutes),
        ),
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_goal_step_target),
        value = UiText.StringResource(R.string.format_steps, listOf(dailyStepTarget)),
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_water_reminder),
        value = if (waterReminderSettings.enabled) {
            UiText.StringResource(
                R.string.profile_summary_water_reminder_on,
                listOf(
                    waterReminderSettings.startTime,
                    waterReminderSettings.endTime,
                    waterReminderSettings.intervalMinutes,
                ),
            )
        } else {
            UiText.StringResource(R.string.common_off)
        },
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_smoke_limit),
        value = if (smokeDailyLimit > 0) {
            UiText.StringResource(R.string.format_count, listOf(smokeDailyLimit))
        } else {
            UiText.StringResource(R.string.common_not_set)
        },
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_height),
        value = heightCm?.let {
            UiText.StringResource(R.string.format_cm, listOf(it))
        } ?: UiText.StringResource(R.string.common_not_added),
    ),
    ProfileGoalSummaryState(
        title = UiText.StringResource(R.string.profile_summary_weight_measurements),
        value = UiText.StringResource(
            R.string.format_weight_measurements,
            listOf(
                measurement.weightKg,
                measurement.shoulderCm,
                measurement.waistCm,
                measurement.hipCm,
            ),
        ),
    ),
)

private fun formatEditableAmount(amount: Float): String = if (amount % 1f == 0f) {
    amount.toInt().toString()
} else {
    String.format(Locale.US, "%.1f", amount)
}
