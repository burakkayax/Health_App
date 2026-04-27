package com.burak.healthapp.feature.profile.goals

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.R
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.feature.profile.goals.ProfileGoalsUiState
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.validation.GoalSettingsValidator
import com.burak.healthapp.domain.validation.HealthInputError
import com.burak.healthapp.domain.validation.ValidationResult
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun ProfileGoalsRoute(
    onSaved: () -> Unit,
) {
    val viewModel: ProfileGoalsViewModel = viewModel(factory = ProfileGoalsViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileGoalsContent(
        state = uiState,
        onSave = { goals, measurement, heightCm, waterReminderSettings ->
            viewModel.saveGoalsAndMeasurement(
                goals = goals,
                measurement = measurement,
                heightCm = heightCm,
                waterReminderSettings = waterReminderSettings,
                onSaved = onSaved,
            )
        },
        onStepTrackingChange = viewModel::updateStepTrackingEnabled,
    )
}

@Composable
fun ProfileGoalsContent(
    state: ProfileGoalsUiState,
    onSave: (GoalSettings, BodyMeasurementEntry, Float?, WaterReminderSettings) -> Unit,
    onStepTrackingChange: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val hasStepSensor = remember { context.hasStepCounterSensor() }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        onStepTrackingChange(granted)
    }
    val latestMeasurement = state.latestMeasurement ?: BodyMeasurementEntry(
        date = LocalDate.now(),
        weightKg = state.goalSettings.baselineWeightKg,
        shoulderCm = state.goalSettings.baselineShoulderCm,
        waistCm = state.goalSettings.baselineWaistCm,
        hipCm = state.goalSettings.baselineHipCm,
    )
    var dailyCalories by remember(state.goalSettings) { mutableStateOf(state.goalSettings.dailyCaloriesTarget.toString()) }
    var protein by remember(state.goalSettings) { mutableStateOf(state.goalSettings.proteinTargetGrams.toString()) }
    var carbs by remember(state.goalSettings) { mutableStateOf(state.goalSettings.carbTargetGrams.toString()) }
    var fat by remember(state.goalSettings) { mutableStateOf(state.goalSettings.fatTargetGrams.toString()) }
    var water by remember(state.goalSettings) { mutableStateOf(state.goalSettings.waterTargetMl.toString()) }
    var dailySteps by remember(state.goalSettings) { mutableStateOf(state.goalSettings.dailyStepTarget.toString()) }
    var stepGoalError by remember(state.goalSettings) { mutableStateOf<StepGoalInputError?>(null) }
    var sleepBedtime by remember(state.goalSettings) { mutableStateOf(state.goalSettings.sleepTargetBedtime.toString()) }
    var sleepWakeTime by remember(state.goalSettings) { mutableStateOf(state.goalSettings.sleepTargetWakeTime.toString()) }
    var exerciseTargetDays by remember(state.goalSettings) { mutableStateOf(state.goalSettings.exerciseTargetDaysPerWeek.toString()) }
    var exerciseTargetDuration by remember(state.goalSettings) { mutableStateOf(state.goalSettings.exerciseTargetDurationMinutes.toString()) }
    var smokeDailyLimit by remember(state.goalSettings) { mutableStateOf(state.goalSettings.smokeDailyLimit.toString()) }
    var targetWeight by remember(state.goalSettings) { mutableStateOf(state.goalSettings.targetWeightKg.toString()) }
    var currentWeight by remember(latestMeasurement) { mutableStateOf(latestMeasurement.weightKg.toString()) }
    var currentShoulder by remember(latestMeasurement) { mutableStateOf(latestMeasurement.shoulderCm.toString()) }
    var currentWaist by remember(latestMeasurement) { mutableStateOf(latestMeasurement.waistCm.toString()) }
    var currentHip by remember(latestMeasurement) { mutableStateOf(latestMeasurement.hipCm.toString()) }
    var currentHeight by remember(state.heightCm) { mutableStateOf(state.heightCm.toEditableText()) }
    var waterReminderEnabled by remember(state.waterReminderSettings) { mutableStateOf(state.waterReminderSettings.enabled) }
    var waterReminderStart by remember(state.waterReminderSettings) { mutableStateOf(state.waterReminderSettings.startTime.toString()) }
    var waterReminderEnd by remember(state.waterReminderSettings) { mutableStateOf(state.waterReminderSettings.endTime.toString()) }
    var waterReminderInterval by remember(state.waterReminderSettings) { mutableStateOf(state.waterReminderSettings.intervalMinutes.toString()) }
    var notificationPermissionGranted by remember { mutableStateOf(context.hasPostNotificationsPermission()) }
    var notificationPermissionDenied by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        notificationPermissionGranted = granted
        notificationPermissionDenied = !granted
        waterReminderEnabled = granted
    }
    var formError by remember(state.goalSettings, latestMeasurement) { mutableStateOf<HealthInputError?>(null) }
    val dailyGoalsTitle = stringResource(R.string.profile_goals_daily_title)
    val dailyGoalsSubtitle = stringResource(R.string.profile_goals_daily_subtitle)
    val waterReminderTitle = stringResource(R.string.profile_goals_water_reminder_title)
    val waterReminderSubtitle = stringResource(R.string.profile_goals_water_reminder_subtitle)
    val stepTrackingTitle = stringResource(R.string.profile_goals_step_tracking_title)
    val stepTrackingSubtitle = stringResource(R.string.profile_goals_step_tracking_subtitle)
    val measurementsTitle = stringResource(R.string.profile_goals_measurements_title)
    val measurementsSubtitle = stringResource(R.string.profile_goals_measurements_subtitle)
    val stepGoalErrorText = stepGoalError?.asErrorText()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_goals_screen"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        profileGoalsSection(
            title = dailyGoalsTitle,
            subtitle = dailyGoalsSubtitle,
        ) {
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_calories),
                leftValue = dailyCalories,
                rightLabel = stringResource(R.string.profile_goal_protein),
                rightValue = protein,
                onLeftChange = { dailyCalories = it },
                onRightChange = { protein = it },
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_carbs),
                leftValue = carbs,
                rightLabel = stringResource(R.string.profile_goal_fat),
                rightValue = fat,
                onLeftChange = { carbs = it },
                onRightChange = { fat = it },
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_water),
                leftValue = water,
                rightLabel = stringResource(R.string.profile_goal_step_target),
                rightValue = dailySteps,
                onLeftChange = { water = it },
                onRightChange = { value ->
                    dailySteps = value
                    if (stepGoalError != null) {
                        stepGoalError = validateStepTargetInput(value).errorOrNull()
                    }
                },
                rightErrorText = stepGoalErrorText,
                rightTestTag = "profile_goal_step_target_field",
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_smoke_limit),
                leftValue = smokeDailyLimit,
                rightLabel = "",
                rightValue = "",
                onLeftChange = { smokeDailyLimit = it },
                onRightChange = {},
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_target_weight),
                leftValue = targetWeight,
                rightLabel = stringResource(R.string.profile_goal_exercise_days),
                rightValue = exerciseTargetDays,
                onLeftChange = { targetWeight = it },
                onRightChange = { exerciseTargetDays = it },
                leftKeyboardType = KeyboardType.Decimal,
                rightKeyboardType = KeyboardType.Number,
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_bedtime),
                leftValue = sleepBedtime,
                rightLabel = stringResource(R.string.profile_goal_wake_time),
                rightValue = sleepWakeTime,
                onLeftChange = { sleepBedtime = it },
                onRightChange = { sleepWakeTime = it },
                leftKeyboardType = KeyboardType.Text,
                rightKeyboardType = KeyboardType.Text,
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_exercise_duration),
                leftValue = exerciseTargetDuration,
                rightLabel = "",
                rightValue = "",
                onLeftChange = { exerciseTargetDuration = it },
                onRightChange = {},
            )
        }
        profileGoalsSection(
            title = waterReminderTitle,
            subtitle = waterReminderSubtitle,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.profile_goal_reminder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Switch(
                    checked = waterReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (!enabled) {
                            waterReminderEnabled = false
                            notificationPermissionDenied = false
                        } else if (context.hasPostNotificationsPermission()) {
                            notificationPermissionGranted = true
                            notificationPermissionDenied = false
                            waterReminderEnabled = true
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            waterReminderEnabled = true
                        }
                    },
                )
            }
            if ((waterReminderEnabled && !notificationPermissionGranted) || notificationPermissionDenied) {
                Text(
                    text = stringResource(R.string.profile_goals_water_reminder_permission_off),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_start),
                leftValue = waterReminderStart,
                rightLabel = stringResource(R.string.profile_goal_end),
                rightValue = waterReminderEnd,
                onLeftChange = { waterReminderStart = it },
                onRightChange = { waterReminderEnd = it },
                leftKeyboardType = KeyboardType.Text,
                rightKeyboardType = KeyboardType.Text,
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_frequency),
                leftValue = waterReminderInterval,
                rightLabel = "",
                rightValue = "",
                onLeftChange = { waterReminderInterval = it },
                onRightChange = {},
            )
        }
        profileGoalsSection(
            title = stepTrackingTitle,
            subtitle = stepTrackingSubtitle,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_goal_step_tracking),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = if (hasStepSensor) {
                            stringResource(R.string.profile_goal_step_tracking_helper)
                        } else {
                            stringResource(R.string.profile_goal_step_tracking_no_sensor)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.stepTrackingEnabled && hasStepSensor,
                    enabled = hasStepSensor,
                    onCheckedChange = { enabled ->
                        if (!enabled) {
                            onStepTrackingChange(false)
                        } else if (context.hasActivityRecognitionPermission()) {
                            onStepTrackingChange(true)
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        } else {
                            onStepTrackingChange(true)
                        }
                    },
                )
            }
        }
        profileGoalsSection(
            title = measurementsTitle,
            subtitle = measurementsSubtitle,
        ) {
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_current_weight),
                leftValue = currentWeight,
                rightLabel = stringResource(R.string.profile_goal_current_shoulder),
                rightValue = currentShoulder,
                onLeftChange = { currentWeight = it },
                onRightChange = { currentShoulder = it },
                decimal = true,
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_current_waist),
                leftValue = currentWaist,
                rightLabel = stringResource(R.string.profile_goal_current_hip),
                rightValue = currentHip,
                onLeftChange = { currentWaist = it },
                onRightChange = { currentHip = it },
                decimal = true,
            )
            GoalFieldRow(
                leftLabel = stringResource(R.string.profile_goal_height),
                leftValue = currentHeight,
                rightLabel = "",
                rightValue = "",
                onLeftChange = { currentHeight = it },
                onRightChange = {},
                decimal = true,
            )
        }
        item {
            RoundedPillButton(
                label = stringResource(R.string.profile_goals_save),
                modifier = Modifier.fillMaxWidth(),
                containerColor = HealthPrimary,
                contentColor = Color.White,
                onClick = {
                    formError = null
                    val stepValidation = validateStepTargetInput(dailySteps)
                    val validatedDailySteps = when (stepValidation) {
                        is StepGoalValidationResult.Valid -> stepValidation.value
                        is StepGoalValidationResult.Invalid -> {
                            stepGoalError = stepValidation.error
                            return@RoundedPillButton
                        }
                    }

                    val parsedCalories = dailyCalories.toIntOrNull()
                    val parsedProtein = protein.toIntOrNull()
                    val parsedCarbs = carbs.toIntOrNull()
                    val parsedFat = fat.toIntOrNull()
                    val parsedWater = water.toIntOrNull()
                    val parsedSmokeLimit = smokeDailyLimit.toIntOrNull()
                    val parsedExerciseDays = exerciseTargetDays.toIntOrNull()
                    val parsedExerciseDuration = exerciseTargetDuration.toIntOrNull()
                    val parsedTargetWeight = targetWeight.toFloatOrNull()
                    val parsedCurrentWeight = currentWeight.toFloatOrNull()
                    val parsedCurrentShoulder = currentShoulder.toFloatOrNull()
                    val parsedCurrentWaist = currentWaist.toFloatOrNull()
                    val parsedCurrentHip = currentHip.toFloatOrNull()
                    val parsedHeight = currentHeight.trim().ifBlank { null }?.toFloatOrNull()
                    val parsedBedtime = sleepBedtime.toLocalTimeOrNull()
                    val parsedWakeTime = sleepWakeTime.toLocalTimeOrNull()
                    val parsedReminderStart = waterReminderStart.toLocalTimeOrNull()
                    val parsedReminderEnd = waterReminderEnd.toLocalTimeOrNull()
                    val parsedReminderInterval = waterReminderInterval.toIntOrNull()

                    if (
                        listOf(
                            parsedCalories,
                            parsedProtein,
                            parsedCarbs,
                            parsedFat,
                            parsedWater,
                            parsedSmokeLimit,
                            parsedExerciseDays,
                            parsedExerciseDuration,
                            parsedReminderInterval,
                        ).any { it == null } ||
                        listOf(
                            parsedTargetWeight,
                            parsedCurrentWeight,
                            parsedCurrentShoulder,
                            parsedCurrentWaist,
                            parsedCurrentHip,
                        ).any { it == null } ||
                        (currentHeight.isNotBlank() && parsedHeight == null)
                    ) {
                        formError = HealthInputError.MUST_BE_NUMBER
                        return@RoundedPillButton
                    }
                    if (
                        parsedBedtime == null ||
                        parsedWakeTime == null ||
                        parsedReminderStart == null ||
                        parsedReminderEnd == null
                    ) {
                        formError = HealthInputError.INVALID_TIME
                        return@RoundedPillButton
                    }

                    val goals = GoalSettings(
                        dailyCaloriesTarget = parsedCalories!!,
                        proteinTargetGrams = parsedProtein!!,
                        carbTargetGrams = parsedCarbs!!,
                        fatTargetGrams = parsedFat!!,
                        waterTargetMl = parsedWater!!,
                        dailyStepTarget = validatedDailySteps,
                        sleepTargetBedtime = parsedBedtime,
                        sleepTargetWakeTime = parsedWakeTime,
                        exerciseTargetDaysPerWeek = parsedExerciseDays!!,
                        exerciseTargetDurationMinutes = parsedExerciseDuration!!,
                        smokeDailyLimit = parsedSmokeLimit!!,
                        baselineWeightKg = state.goalSettings.baselineWeightKg,
                        targetWeightKg = parsedTargetWeight!!,
                        baselineShoulderCm = state.goalSettings.baselineShoulderCm,
                        baselineWaistCm = state.goalSettings.baselineWaistCm,
                        baselineHipCm = state.goalSettings.baselineHipCm,
                    )
                    when (val goalValidation = GoalSettingsValidator.validate(goals)) {
                        is ValidationResult.Valid -> Unit
                        is ValidationResult.Invalid -> {
                            formError = goalValidation.errors.firstOrNull()
                            return@RoundedPillButton
                        }
                    }

                    onSave(
                        goals,
                        BodyMeasurementEntry(
                            date = LocalDate.now(),
                            weightKg = parsedCurrentWeight!!,
                            shoulderCm = parsedCurrentShoulder!!,
                            waistCm = parsedCurrentWaist!!,
                            hipCm = parsedCurrentHip!!,
                        ),
                        parsedHeight,
                        WaterReminderSettings(
                            enabled = waterReminderEnabled,
                            startTime = parsedReminderStart,
                            endTime = parsedReminderEnd,
                            intervalMinutes = parsedReminderInterval!!
                                .coerceAtLeast(DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES),
                        ),
                    )
                },
            )
            formError?.let { error ->
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = error.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun LazyListScope.profileGoalsSection(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    item {
        HealthCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier.padding(top = HealthSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun StepGoalInputError.asErrorText(): String {
    return when (this) {
        StepGoalInputError.REQUIRED -> stringResource(R.string.error_step_target_required)
        StepGoalInputError.MUST_BE_NUMBER -> stringResource(R.string.error_step_target_must_be_number)
        StepGoalInputError.POSITIVE -> stringResource(R.string.error_step_target_positive)
        StepGoalInputError.TOO_HIGH -> stringResource(R.string.error_step_target_too_high)
    }
}

private fun StepGoalValidationResult.errorOrNull(): StepGoalInputError? {
    return (this as? StepGoalValidationResult.Invalid)?.error
}

@Composable
private fun GoalFieldRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    onLeftChange: (String) -> Unit,
    onRightChange: (String) -> Unit,
    decimal: Boolean = false,
    leftKeyboardType: KeyboardType? = null,
    rightKeyboardType: KeyboardType? = null,
    leftErrorText: String? = null,
    rightErrorText: String? = null,
    leftTestTag: String? = null,
    rightTestTag: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        GoalFieldColumn(
            modifier = Modifier.weight(1f),
            value = leftValue,
            onValueChange = onLeftChange,
            label = leftLabel,
            keyboardOptions = KeyboardOptions(
                keyboardType = leftKeyboardType ?: if (decimal) KeyboardType.Decimal else KeyboardType.Number,
            ),
            errorText = leftErrorText,
            testTag = leftTestTag,
        )
        if (rightLabel.isNotBlank()) {
            GoalFieldColumn(
                modifier = Modifier.weight(1f),
                value = rightValue,
                onValueChange = onRightChange,
                label = rightLabel,
                keyboardOptions = KeyboardOptions(
                    keyboardType = rightKeyboardType ?: if (decimal) KeyboardType.Decimal else KeyboardType.Number,
                ),
                errorText = rightErrorText,
                testTag = rightTestTag,
            )
        }
    }
}

@Composable
private fun GoalFieldColumn(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardOptions: KeyboardOptions,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    testTag: String? = null,
) {
    Column(modifier = modifier) {
        HealthPillTextField(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (testTag != null) Modifier.testTag(testTag) else Modifier),
            value = value,
            onValueChange = onValueChange,
            label = label,
            isError = errorText != null,
            keyboardOptions = keyboardOptions,
        )
        if (errorText != null) {
            Text(
                modifier = Modifier.padding(start = HealthSpacing.sm, top = HealthSpacing.xs),
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

private fun String.toIntOrDefault(fallback: Int): Int = toIntOrNull() ?: fallback
private fun String.toFloatOrDefault(fallback: Float): Float = toFloatOrNull() ?: fallback
private fun String.toLocalTimeOrDefault(fallback: LocalTime): LocalTime = runCatching { LocalTime.parse(this) }.getOrElse { fallback }
private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()
private fun Float?.toEditableText(): String = this?.let {
    if (it % 1f == 0f) it.toInt().toString() else it.toString()
} ?: ""

private fun Context.hasPostNotificationsPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.hasActivityRecognitionPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.hasStepCounterSensor(): Boolean {
    val sensorManager = getSystemService(SensorManager::class.java)
    return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}
