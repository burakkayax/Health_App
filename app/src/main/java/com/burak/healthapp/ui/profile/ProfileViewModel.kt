package com.burak.healthapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.data.repository.DashboardRepository
import com.burak.healthapp.data.repository.SettingsRepository
import com.burak.healthapp.domain.calculation.formatClockRange
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.ui.model.EditableSupplementTemplateState
import com.burak.healthapp.ui.model.ProfileGoalSummaryState
import com.burak.healthapp.ui.model.ProfileSupplementTemplateState
import com.burak.healthapp.ui.model.ProfileUiState
import com.burak.healthapp.ui.model.SupplementEditorUiState
import com.burak.healthapp.ui.model.toDomainTemplate
import com.burak.healthapp.ui.root.healthApplication
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private var nextDraftId = 1L
    private var latestTemplates: List<SupplementTemplate> = emptyList()

    private val editorState = MutableStateFlow(SupplementEditorUiState())
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
    ) { settings, templates, latestMeasurement, editor ->
        settings.toProfileUiState(
            supplementTemplates = templates,
            latestMeasurement = latestMeasurement,
            supplementEditor = editor,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState(
            userName = "Misafir",
            avatarInitials = "M",
            themeMode = ThemeMode.SYSTEM,
            goalSummaries = emptyList(),
            supplementTemplates = emptyList(),
            supplementEditor = SupplementEditorUiState(),
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
                        saveErrorMessage = "Takviyeler kaydedilemedi. Tekrar dene.",
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                    dashboardRepository = healthApplication().container.dashboardRepository,
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
            hasErrors -> "Kaydetmeden önce eksik alanları düzelt."
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
            val parsedAmount = trimmedAmount.toFloatOrNull()

            draft.copy(
                name = trimmedName,
                targetAmount = trimmedAmount,
                unitLabel = trimmedUnit,
                nameError = when {
                    trimmedName.isBlank() -> "Takviye adı gerekli."
                    draft.draftId in duplicateDraftIds -> DUPLICATE_NAME_ERROR
                    else -> null
                },
                targetAmountError = when {
                    trimmedAmount.isBlank() -> "Hedef doz gerekli."
                    parsedAmount == null -> "Hedef doz sayı olmalı."
                    parsedAmount <= 0f -> "Hedef doz 0'dan büyük olmalı."
                    else -> null
                },
                unitLabelError = if (trimmedUnit.isBlank()) "Birim gerekli." else null,
            )
        }
    }

    private fun createDraftId(): Long {
        return nextDraftId++
    }
}

private const val DUPLICATE_NAME_ERROR = "Aynı takviye adı iki kez kullanılamaz."

private fun SettingsState.toProfileUiState(
    supplementTemplates: List<SupplementTemplate>,
    latestMeasurement: BodyMeasurementEntry?,
    supplementEditor: SupplementEditorUiState,
): ProfileUiState {
    val locale = Locale.forLanguageTag("tr")
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
        goalSummaries = goalSettings.toSummaryStates(
            measurement = measurement,
            locale = locale,
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
    )
}

private fun GoalSettings.toSummaryStates(
    measurement: BodyMeasurementEntry,
    locale: Locale,
    heightCm: Float?,
    waterReminderSettings: WaterReminderSettings,
): List<ProfileGoalSummaryState> {
    return listOf(
        ProfileGoalSummaryState(
            title = "Kalori / Su",
            value = "${dailyCaloriesTarget} kcal • ${waterTargetMl} ml",
        ),
        ProfileGoalSummaryState(
            title = "Makrolar",
            value = "${proteinTargetGrams}P • ${carbTargetGrams}K • ${fatTargetGrams}Y",
        ),
        ProfileGoalSummaryState(
            title = "Uyku Hedefi",
            value = formatClockRange(sleepTargetBedtime, sleepTargetWakeTime),
        ),
        ProfileGoalSummaryState(
            title = "Egzersiz",
            value = "${exerciseTargetDaysPerWeek} gün • ${exerciseTargetDurationMinutes} dk",
        ),
        ProfileGoalSummaryState(
            title = "Adım Hedefi",
            value = "$dailyStepTarget adım",
        ),
        ProfileGoalSummaryState(
            title = "Su Hatırlatma",
            value = if (waterReminderSettings.enabled) {
                "${waterReminderSettings.startTime} - ${waterReminderSettings.endTime} • ${waterReminderSettings.intervalMinutes} dk"
            } else {
                "Kapalı"
            },
        ),
        ProfileGoalSummaryState(
            title = "Sigara Limiti",
            value = if (smokeDailyLimit > 0) "$smokeDailyLimit adet" else "Ayarlanmadı",
        ),
        ProfileGoalSummaryState(
            title = "Boy",
            value = heightCm?.let { String.format(locale, "%.0f cm", it) } ?: "Eklenmedi",
        ),
        ProfileGoalSummaryState(
            title = "Kilo ve Ölçüler",
            value = String.format(
                locale,
                "%.1f kg • %.0f/%.0f/%.0f cm",
                measurement.weightKg,
                measurement.shoulderCm,
                measurement.waistCm,
                measurement.hipCm,
            ),
        ),
    )
}

private fun formatEditableAmount(amount: Float): String {
    return if (amount % 1f == 0f) {
        amount.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", amount)
    }
}
