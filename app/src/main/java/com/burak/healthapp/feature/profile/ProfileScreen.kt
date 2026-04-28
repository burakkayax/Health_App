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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.R
import com.burak.healthapp.core.notification.HealthNotifications
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.WaterReminderSettings
import java.time.LocalDate

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
            ProfileHeaderCard(state = state)
        }
        item {
            ProfileGoalsSummaryCard(
                state = state,
                onOpenGoals = onOpenGoals,
            )
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
            ProfileThemeCard(
                themeMode = state.themeMode,
                onThemeModeChange = onThemeModeChange,
            )
        }
        item {
            ProfileDataManagementCard(
                exportState = state.exportState,
                onExportData = onExportData,
                onImportData = onImportData,
                onDeleteAllHealthData = onDeleteAllHealthData,
            )
        }
        item {
            ProfileSupplementsCard(
                templates = state.supplementTemplates,
                onManageSupplements = onManageSupplements,
            )
        }
    }
}

private fun defaultExportFileName(): String = "health_app_export_${LocalDate.now()}.json"

private fun Context.hasActivityRecognitionPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACTIVITY_RECOGNITION,
    ) == PackageManager.PERMISSION_GRANTED

private fun Context.hasStepCounterSensor(): Boolean {
    val sensorManager = getSystemService(SensorManager::class.java)
    return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}
