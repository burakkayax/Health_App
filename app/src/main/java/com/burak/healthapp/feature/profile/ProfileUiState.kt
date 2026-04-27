package com.burak.healthapp.feature.profile

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.export.HealthDataImportPreview

data class ProfileGoalSummaryState(
    val title: UiText,
    val value: UiText,
)

data class ProfileSupplementTemplateState(
    val id: Long,
    val name: String,
    val targetAmount: Float,
    val unitLabel: String,
)

data class EditableSupplementTemplateState(
    val draftId: Long,
    val id: Long = 0,
    val name: String = "",
    val targetAmount: String = "",
    val unitLabel: String = "",
    val nameError: UiText? = null,
    val targetAmountError: UiText? = null,
    val unitLabelError: UiText? = null,
)

fun EditableSupplementTemplateState.toDomainTemplate(sortOrder: Int): SupplementTemplate {
    return SupplementTemplate(
        id = id,
        name = name.trim(),
        targetAmount = targetAmount.toFloatOrNull() ?: 0f,
        unitLabel = unitLabel.trim(),
        sortOrder = sortOrder,
    )
}

data class SupplementEditorUiState(
    val isVisible: Boolean = false,
    val drafts: List<EditableSupplementTemplateState> = emptyList(),
    val canSave: Boolean = true,
    val isSaving: Boolean = false,
    val validationMessage: UiText? = null,
    val saveErrorMessage: UiText? = null,
)

data class ProfileExportUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isDeleting: Boolean = false,
    val message: UiText? = null,
    val isError: Boolean = false,
    val importPreview: HealthDataImportPreview? = null,
    val showDeleteConfirmation: Boolean = false,
)

data class ProfileUiState(
    val userName: String,
    val avatarInitials: String,
    val themeMode: ThemeMode,
    val goalSummaries: List<ProfileGoalSummaryState>,
    val supplementTemplates: List<ProfileSupplementTemplateState>,
    val supplementEditor: SupplementEditorUiState = SupplementEditorUiState(),
    val exportState: ProfileExportUiState = ProfileExportUiState(),
)
