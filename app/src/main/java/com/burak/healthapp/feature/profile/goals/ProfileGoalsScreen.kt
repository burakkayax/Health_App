package com.burak.healthapp.feature.profile.goals

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.burak.healthapp.feature.profile.goals.ProfileGoalsUiState
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
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
    )
}

@Composable
fun ProfileGoalsContent(
    state: ProfileGoalsUiState,
    onSave: (GoalSettings, BodyMeasurementEntry, Float?, WaterReminderSettings) -> Unit,
) {
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
    val dailyGoalsTitle = stringResource(R.string.profile_goals_daily_title)
    val dailyGoalsSubtitle = stringResource(R.string.profile_goals_daily_subtitle)
    val waterReminderTitle = stringResource(R.string.profile_goals_water_reminder_title)
    val waterReminderSubtitle = stringResource(R.string.profile_goals_water_reminder_subtitle)
    val measurementsTitle = stringResource(R.string.profile_goals_measurements_title)
    val measurementsSubtitle = stringResource(R.string.profile_goals_measurements_subtitle)

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
                onRightChange = { dailySteps = it },
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
                    onCheckedChange = { waterReminderEnabled = it },
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
                    onSave(
                        GoalSettings(
                            dailyCaloriesTarget = dailyCalories.toIntOrDefault(DefaultHealthGoals.DAILY_CALORIES),
                            proteinTargetGrams = protein.toIntOrDefault(DefaultHealthGoals.PROTEIN_GRAMS),
                            carbTargetGrams = carbs.toIntOrDefault(DefaultHealthGoals.CARBS_GRAMS),
                            fatTargetGrams = fat.toIntOrDefault(DefaultHealthGoals.FAT_GRAMS),
                            waterTargetMl = water.toIntOrDefault(DefaultHealthGoals.WATER_TARGET_ML),
                            dailyStepTarget = dailySteps.toIntOrDefault(DefaultHealthGoals.DAILY_STEPS),
                            sleepTargetBedtime = sleepBedtime.toLocalTimeOrDefault(state.goalSettings.sleepTargetBedtime),
                            sleepTargetWakeTime = sleepWakeTime.toLocalTimeOrDefault(state.goalSettings.sleepTargetWakeTime),
                            exerciseTargetDaysPerWeek = exerciseTargetDays.toIntOrDefault(DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK),
                            exerciseTargetDurationMinutes = exerciseTargetDuration.toIntOrDefault(DefaultHealthGoals.EXERCISE_DURATION_MINUTES),
                            smokeDailyLimit = smokeDailyLimit.toIntOrDefault(DefaultHealthGoals.SMOKE_DAILY_LIMIT),
                            baselineWeightKg = state.goalSettings.baselineWeightKg,
                            targetWeightKg = targetWeight.toFloatOrDefault(DefaultHealthGoals.TARGET_WEIGHT_KG),
                            baselineShoulderCm = state.goalSettings.baselineShoulderCm,
                            baselineWaistCm = state.goalSettings.baselineWaistCm,
                            baselineHipCm = state.goalSettings.baselineHipCm,
                        ),
                        BodyMeasurementEntry(
                            date = LocalDate.now(),
                            weightKg = currentWeight.toFloatOrDefault(latestMeasurement.weightKg),
                            shoulderCm = currentShoulder.toFloatOrDefault(latestMeasurement.shoulderCm),
                            waistCm = currentWaist.toFloatOrDefault(latestMeasurement.waistCm),
                            hipCm = currentHip.toFloatOrDefault(latestMeasurement.hipCm),
                        ),
                        currentHeight.toFloatOrNull(),
                        WaterReminderSettings(
                            enabled = waterReminderEnabled,
                            startTime = waterReminderStart.toLocalTimeOrDefault(state.waterReminderSettings.startTime),
                            endTime = waterReminderEnd.toLocalTimeOrDefault(state.waterReminderSettings.endTime),
                            intervalMinutes = waterReminderInterval
                                .toIntOrDefault(DefaultHealthGoals.DEFAULT_WATER_REMINDER_INTERVAL_MINUTES)
                                .coerceAtLeast(DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES),
                        ),
                    )
                },
            )
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
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        HealthPillTextField(
            modifier = Modifier.weight(1f),
            value = leftValue,
            onValueChange = onLeftChange,
            label = leftLabel,
            keyboardOptions = KeyboardOptions(
                keyboardType = leftKeyboardType ?: if (decimal) KeyboardType.Decimal else KeyboardType.Number,
            ),
        )
        if (rightLabel.isNotBlank()) {
            HealthPillTextField(
                modifier = Modifier.weight(1f),
                value = rightValue,
                onValueChange = onRightChange,
                label = rightLabel,
                keyboardOptions = KeyboardOptions(
                    keyboardType = rightKeyboardType ?: if (decimal) KeyboardType.Decimal else KeyboardType.Number,
                ),
            )
        }
    }
}

private fun String.toIntOrDefault(fallback: Int): Int = toIntOrNull() ?: fallback
private fun String.toFloatOrDefault(fallback: Float): Float = toFloatOrNull() ?: fallback
private fun String.toLocalTimeOrDefault(fallback: LocalTime): LocalTime = runCatching { LocalTime.parse(this) }.getOrElse { fallback }
private fun Float?.toEditableText(): String = this?.let {
    if (it % 1f == 0f) it.toInt().toString() else it.toString()
} ?: ""
