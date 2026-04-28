package com.burak.healthapp.feature.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.R
import com.burak.healthapp.core.notification.HealthNotifications
import com.burak.healthapp.core.ui.components.AvatarBadge
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.export.HealthDataImportPreview
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.feature.profile.EditableSupplementTemplateState
import com.burak.healthapp.feature.profile.ProfileUiState
import com.burak.healthapp.feature.profile.SupplementEditorUiState
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    onOpenGoals: () -> Unit,
) {
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hasStepSensor = remember { context.hasStepCounterSensor() }
    var stepPreferenceMessage by remember { mutableStateOf<UiText?>(null) }
    var waterPreferenceMessage by remember { mutableStateOf<UiText?>(null) }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            viewModel.exportData(uri)
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.loadImportPreview(uri)
        }
    }
    val stepPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            stepPreferenceMessage = null
            viewModel.updateStepTrackingEnabled(true)
        } else {
            stepPreferenceMessage = UiText.StringResource(R.string.profile_step_tracking_permission_required)
            viewModel.updateStepTrackingEnabled(false)
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            waterPreferenceMessage = null
            viewModel.updateWaterReminderSettings(uiState.waterReminderSettings.copy(enabled = true))
        } else {
            waterPreferenceMessage = UiText.StringResource(R.string.profile_water_reminder_permission_off)
            viewModel.updateWaterReminderSettings(uiState.waterReminderSettings.copy(enabled = false))
        }
    }

    ProfileContent(
        state = uiState,
        hasStepSensor = hasStepSensor,
        canPostNotifications = HealthNotifications.canPostNotifications(context),
        stepPreferenceMessage = stepPreferenceMessage,
        waterPreferenceMessage = waterPreferenceMessage,
        onOpenGoals = onOpenGoals,
        onManageSupplements = viewModel::openSupplementEditor,
        onExportData = {
            exportLauncher.launch(defaultExportFileName())
        },
        onImportData = {
            importLauncher.launch(arrayOf("application/json", "text/json", "*/*"))
        },
        onDeleteAllHealthData = viewModel::requestDeleteAllHealthData,
        onThemeModeChange = viewModel::updateThemeMode,
        onStepTrackingToggle = { enabled ->
            when {
                !enabled -> {
                    stepPreferenceMessage = null
                    viewModel.updateStepTrackingEnabled(false)
                }
                !hasStepSensor -> {
                    stepPreferenceMessage = UiText.StringResource(R.string.profile_step_tracking_no_sensor)
                    viewModel.updateStepTrackingEnabled(false)
                }
                context.hasActivityRecognitionPermission() -> {
                    stepPreferenceMessage = null
                    viewModel.updateStepTrackingEnabled(true)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    stepPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
                else -> {
                    stepPreferenceMessage = null
                    viewModel.updateStepTrackingEnabled(true)
                }
            }
        },
        onWaterReminderToggle = { enabled ->
            when {
                !enabled -> {
                    waterPreferenceMessage = null
                    viewModel.updateWaterReminderSettings(uiState.waterReminderSettings.copy(enabled = false))
                }
                HealthNotifications.canPostNotifications(context) -> {
                    waterPreferenceMessage = null
                    viewModel.updateWaterReminderSettings(uiState.waterReminderSettings.copy(enabled = true))
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    waterPreferenceMessage = null
                    viewModel.updateWaterReminderSettings(uiState.waterReminderSettings.copy(enabled = true))
                }
            }
        },
        onWaterReminderSettingsSave = { settings ->
            waterPreferenceMessage = null
            viewModel.updateWaterReminderSettings(settings)
        },
    )

    if (uiState.supplementEditor.isVisible) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissSupplementEditor) {
            SupplementTemplateEditorSheet(
                state = uiState.supplementEditor,
                onNameChange = viewModel::updateSupplementName,
                onTargetAmountChange = viewModel::updateSupplementTargetAmount,
                onUnitLabelChange = viewModel::updateSupplementUnitLabel,
                onRemoveDraft = viewModel::removeSupplementDraft,
                onAddDraft = viewModel::addSupplementDraft,
                onSave = viewModel::saveSupplementTemplates,
            )
        }
    }

    uiState.exportState.importPreview?.let { preview ->
        ImportPreviewDialog(
            preview = preview,
            isImporting = uiState.exportState.isImporting,
            onDismiss = viewModel::dismissImportPreview,
            onConfirm = viewModel::confirmImport,
        )
    }

    if (uiState.exportState.showDeleteConfirmation) {
        DeleteHealthDataConfirmationDialog(
            isDeleting = uiState.exportState.isDeleting,
            onDismiss = viewModel::dismissDeleteAllConfirmation,
            onConfirm = viewModel::confirmDeleteAllHealthData,
        )
    }
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    hasStepSensor: Boolean = true,
    canPostNotifications: Boolean = true,
    stepPreferenceMessage: UiText? = null,
    waterPreferenceMessage: UiText? = null,
    onOpenGoals: () -> Unit,
    onManageSupplements: () -> Unit,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    onDeleteAllHealthData: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onStepTrackingToggle: (Boolean) -> Unit = {},
    onWaterReminderToggle: (Boolean) -> Unit = {},
    onWaterReminderSettingsSave: (WaterReminderSettings) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
                ) {
                    AvatarBadge(initials = state.avatarInitials)
                    Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                        Text(
                            text = state.userName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.profile_summary_helper),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.profile_goals_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    CardHeaderActionButton(
                        label = stringResource(R.string.common_edit),
                        icon = null,
                        modifier = Modifier.testTag("profile_goals_button"),
                        onClick = onOpenGoals,
                    )
                }
                Column(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
                ) {
                    state.goalSummaries.forEach { summary ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = summary.title.asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = summary.value.asString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
        item {
            StepTrackingPreferenceCard(
                enabled = state.stepTrackingEnabled,
                hasStepSensor = hasStepSensor,
                message = stepPreferenceMessage,
                onToggle = onStepTrackingToggle,
            )
        }
        item {
            WaterReminderPreferenceCard(
                settings = state.waterReminderSettings,
                canPostNotifications = canPostNotifications,
                message = waterPreferenceMessage,
                onToggle = onWaterReminderToggle,
                onSave = onWaterReminderSettingsSave,
            )
        }
        item {
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_theme_section"),
            ) {
                Text(
                    text = stringResource(R.string.profile_theme_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = stringResource(R.string.profile_theme_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SegmentedControl(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = HealthSpacing.sm),
                    options = listOf(
                        stringResource(R.string.profile_theme_light),
                        stringResource(R.string.profile_theme_dark),
                        stringResource(R.string.profile_theme_system),
                    ),
                    selectedIndex = when (state.themeMode) {
                        ThemeMode.LIGHT -> 0
                        ThemeMode.DARK -> 1
                        ThemeMode.SYSTEM -> 2
                    },
                    onSelectionChange = { index ->
                        onThemeModeChange(
                            when (index) {
                                0 -> ThemeMode.LIGHT
                                1 -> ThemeMode.DARK
                                else -> ThemeMode.SYSTEM
                            },
                        )
                    },
                )
            }
        }
        item {
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_data_management_section"),
            ) {
                Text(
                    text = stringResource(R.string.profile_data_management_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = stringResource(R.string.profile_data_management_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = stringResource(R.string.export_sensitive_data_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                state.exportState.message?.let { message ->
                    Text(
                        modifier = Modifier
                            .padding(top = HealthSpacing.xs)
                            .testTag("profile_export_message"),
                        text = message.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.exportState.isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
                RoundedPillButton(
                    label = if (state.exportState.isExporting) {
                        stringResource(R.string.common_saving)
                    } else {
                        stringResource(R.string.profile_export_data)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = HealthSpacing.sm)
                        .testTag("profile_export_data_button"),
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                    enabled = !state.exportState.isExporting,
                    onClick = onExportData,
                )
                RoundedPillButton(
                    label = if (state.exportState.isImporting) {
                        stringResource(R.string.common_loading)
                    } else {
                        stringResource(R.string.profile_import_data)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = HealthSpacing.xs)
                        .testTag("profile_import_data_button"),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    enabled = !state.exportState.isImporting,
                    onClick = onImportData,
                )
                RoundedPillButton(
                    label = if (state.exportState.isDeleting) {
                        stringResource(R.string.common_loading)
                    } else {
                        stringResource(R.string.profile_delete_all_health_data)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = HealthSpacing.xs)
                        .testTag("profile_delete_all_health_data_button"),
                    containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                    contentColor = MaterialTheme.colorScheme.error,
                    enabled = !state.exportState.isDeleting,
                    onClick = onDeleteAllHealthData,
                )
            }
        }
        item {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.profile_supplements_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    CardHeaderActionButton(
                        label = stringResource(R.string.common_manage),
                        icon = null,
                        modifier = Modifier.testTag("profile_supplements_button"),
                        onClick = onManageSupplements,
                    )
                }
                Column(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
                ) {
                    if (state.supplementTemplates.isEmpty()) {
                        Text(
                            text = stringResource(R.string.profile_empty_supplements),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.supplementTemplates.forEach { template ->
                            Text(
                                text = "${template.name} • ${formatTemplateAmount(template.targetAmount, template.unitLabel)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTrackingPreferenceCard(
    enabled: Boolean,
    hasStepSensor: Boolean,
    message: UiText?,
    onToggle: (Boolean) -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_step_tracking_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_step_tracking_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = when {
                        !hasStepSensor -> stringResource(R.string.profile_step_tracking_no_sensor)
                        enabled -> stringResource(R.string.profile_step_tracking_status_on)
                        else -> stringResource(R.string.profile_step_tracking_status_off)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = stringResource(R.string.profile_step_tracking_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                modifier = Modifier.testTag("profile_step_tracking_toggle"),
                checked = enabled && hasStepSensor,
                enabled = hasStepSensor,
                onCheckedChange = onToggle,
            )
        }
        message?.let {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun WaterReminderPreferenceCard(
    settings: WaterReminderSettings,
    canPostNotifications: Boolean,
    message: UiText?,
    onToggle: (Boolean) -> Unit,
    onSave: (WaterReminderSettings) -> Unit,
) {
    var startTime by remember(settings) { mutableStateOf(settings.startTime.toString()) }
    var endTime by remember(settings) { mutableStateOf(settings.endTime.toString()) }
    var interval by remember(settings) { mutableStateOf(settings.intervalMinutes.toString()) }
    var formError by remember(settings) { mutableStateOf<UiText?>(null) }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_water_reminder_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_water_reminder_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = if (settings.enabled) {
                        stringResource(R.string.profile_water_reminder_status_on)
                    } else {
                        stringResource(R.string.profile_water_reminder_status_off)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                modifier = Modifier.testTag("profile_water_reminder_toggle"),
                checked = settings.enabled,
                onCheckedChange = onToggle,
            )
        }
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.profile_water_reminder_helper),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (settings.enabled && !canPostNotifications) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = stringResource(R.string.profile_water_reminder_permission_off),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        message?.let {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            HealthPillTextField(
                modifier = Modifier.weight(1f),
                value = startTime,
                onValueChange = { startTime = it },
                label = stringResource(R.string.profile_goal_start),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            HealthPillTextField(
                modifier = Modifier.weight(1f),
                value = endTime,
                onValueChange = { endTime = it },
                label = stringResource(R.string.profile_goal_end),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }
        HealthPillTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.xs),
            value = interval,
            onValueChange = { interval = it },
            label = stringResource(R.string.profile_goal_frequency),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = formError != null,
        )
        formError?.let {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        RoundedPillButton(
            label = stringResource(R.string.common_save),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm)
                .testTag("profile_water_reminder_save_button"),
            containerColor = HealthPrimary.copy(alpha = 0.12f),
            contentColor = HealthPrimary,
            onClick = {
                val parsedStart = startTime.toLocalTimeOrNull()
                val parsedEnd = endTime.toLocalTimeOrNull()
                val parsedInterval = interval.toIntOrNull()
                if (parsedStart == null || parsedEnd == null || parsedInterval == null || parsedInterval <= 0) {
                    formError = UiText.StringResource(R.string.profile_water_reminder_invalid_settings)
                    return@RoundedPillButton
                }
                formError = null
                onSave(
                    settings.copy(
                        startTime = parsedStart,
                        endTime = parsedEnd,
                        intervalMinutes = parsedInterval
                            .coerceAtLeast(DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES),
                    ),
                )
            },
        )
    }
}

@Composable
fun SupplementTemplateEditorSheet(
    state: SupplementEditorUiState,
    onNameChange: (Long, String) -> Unit,
    onTargetAmountChange: (Long, String) -> Unit,
    onUnitLabelChange: (Long, String) -> Unit,
    onRemoveDraft: (Long) -> Unit,
    onAddDraft: () -> Unit,
    onSave: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("supplement_template_sheet"),
            contentPadding = PaddingValues(
                start = HealthSpacing.md,
                end = HealthSpacing.md,
                top = HealthSpacing.xs,
                bottom = 168.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            item {
                Text(
                    text = stringResource(R.string.profile_supplements_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            item {
                Text(
                    text = stringResource(R.string.profile_supplements_sheet_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.drafts, key = EditableSupplementTemplateState::draftId) { draft ->
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    HealthPillTextField(
                        value = draft.name,
                        onValueChange = { newValue -> onNameChange(draft.draftId, newValue) },
                        label = stringResource(R.string.profile_supplement_name),
                        isError = draft.nameError != null,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = HealthSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HealthPillTextField(
                            modifier = Modifier.weight(1f),
                            value = draft.targetAmount,
                            onValueChange = { newValue -> onTargetAmountChange(draft.draftId, newValue) },
                            label = stringResource(R.string.profile_supplement_target),
                            isError = draft.targetAmountError != null,
                        )
                        HealthPillTextField(
                            modifier = Modifier.weight(1f),
                            value = draft.unitLabel,
                            onValueChange = { newValue -> onUnitLabelChange(draft.draftId, newValue) },
                            label = stringResource(R.string.profile_supplement_unit),
                            isError = draft.unitLabelError != null,
                        )
                        IconButton(
                            modifier = Modifier.testTag("supplement_remove_${draft.draftId}"),
                            onClick = { onRemoveDraft(draft.draftId) },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.content_description_remove_supplement),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    draft.combinedErrorMessage()?.let { message ->
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                val helperMessage = state.saveErrorMessage ?: state.validationMessage
                if (helperMessage != null) {
                    Text(
                        modifier = Modifier.testTag("supplement_editor_message"),
                        text = helperMessage.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.saveErrorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                RoundedPillButton(
                    label = stringResource(R.string.profile_new_supplement),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supplement_template_add_button"),
                    containerColor = HealthPrimary.copy(alpha = 0.12f),
                    contentColor = HealthPrimary,
                    onClick = onAddDraft,
                )
                RoundedPillButton(
                    label = if (state.isSaving) {
                        stringResource(R.string.common_saving)
                    } else {
                        stringResource(R.string.profile_save_supplements)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supplement_template_save_button"),
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                    enabled = state.canSave,
                    onClick = onSave,
                )
            }
        }
    }
}

@Composable
private fun EditableSupplementTemplateState.combinedErrorMessage(): String? {
    val messages = listOfNotNull(
        nameError,
        targetAmountError,
        unitLabelError,
    ).distinct()
    if (messages.isEmpty()) return null
    return messages.map { it.asString() }.joinToString(" ")
}

private fun formatTemplateAmount(
    amount: Float,
    unitLabel: String,
): String {
    val value = if (amount % 1f == 0f) {
        amount.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", amount)
    }
    return "$value $unitLabel"
}

@Composable
private fun ImportPreviewDialog(
    preview: HealthDataImportPreview,
    isImporting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.import_preview_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                Text(
                    text = stringResource(R.string.import_preview_settings_notice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ImportPreviewRow(stringResource(R.string.import_preview_meals), preview.mealCount)
                ImportPreviewRow(stringResource(R.string.import_preview_hydration), preview.hydrationCount)
                ImportPreviewRow(stringResource(R.string.import_preview_sleep), preview.sleepCount)
                ImportPreviewRow(stringResource(R.string.import_preview_exercise), preview.exerciseCount)
                ImportPreviewRow(stringResource(R.string.import_preview_smoking), preview.smokingCount)
                ImportPreviewRow(stringResource(R.string.import_preview_steps), preview.stepCount)
                ImportPreviewRow(stringResource(R.string.import_preview_body_measurements), preview.bodyMeasurementCount)
                ImportPreviewRow(stringResource(R.string.import_preview_supplement_templates), preview.supplementTemplateCount)
                ImportPreviewRow(stringResource(R.string.import_preview_supplement_doses), preview.supplementDoseCount)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isImporting,
                onClick = onConfirm,
            ) {
                Text(text = stringResource(R.string.profile_import_confirm))
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isImporting,
                onClick = onDismiss,
            ) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun ImportPreviewRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DeleteHealthDataConfirmationDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.delete_health_data_title))
        },
        text = {
            Text(text = stringResource(R.string.delete_health_data_message))
        },
        confirmButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(R.string.profile_delete_all_health_data),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onDismiss,
            ) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
    )
}

private fun defaultExportFileName(): String = "health_app_export_${LocalDate.now()}.json"

private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this.trim()) }.getOrNull()

private fun Context.hasActivityRecognitionPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACTIVITY_RECOGNITION,
    ) == PackageManager.PERMISSION_GRANTED

private fun Context.hasStepCounterSensor(): Boolean {
    val sensorManager = getSystemService(SensorManager::class.java)
    return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}
