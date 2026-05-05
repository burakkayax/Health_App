package com.burak.healthapp.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.DashboardCardType

@Composable
fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_welcome"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_body),
            style = MaterialTheme.typography.bodyLarge,
        )
        HealthCard {
            Text(
                text = stringResource(R.string.onboarding_medical_disclaimer),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrackingAreasStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_tracking_areas"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.onboarding_tracking_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_tracking_body),
            style = MaterialTheme.typography.bodyMedium,
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            maxItemsInEachRow = 2,
        ) {
            DashboardCardType.entries.forEach { area ->
                val isSelected = state.selectedTrackingAreas.contains(area)
                val titleRes = when (area) {
                    DashboardCardType.HYDRATION -> R.string.dashboard_card_hydration
                    DashboardCardType.SLEEP -> R.string.dashboard_card_sleep
                    DashboardCardType.NUTRITION -> R.string.dashboard_card_nutrition
                    DashboardCardType.STEPS -> R.string.dashboard_card_steps
                    DashboardCardType.WEIGHT -> R.string.dashboard_card_weight
                    DashboardCardType.CAFFEINE -> R.string.dashboard_card_caffeine
                    DashboardCardType.SMOKING -> R.string.dashboard_card_smoking
                    DashboardCardType.EXERCISE -> R.string.dashboard_card_exercise
                    DashboardCardType.SUPPLEMENTS -> R.string.dashboard_card_supplements
                }
                val subtitleRes = when (area) {
                    DashboardCardType.HYDRATION -> R.string.onboarding_tracking_subtitle_hydration
                    DashboardCardType.SLEEP -> R.string.onboarding_tracking_subtitle_sleep
                    DashboardCardType.NUTRITION -> R.string.onboarding_tracking_subtitle_nutrition
                    DashboardCardType.STEPS -> R.string.onboarding_tracking_subtitle_steps
                    DashboardCardType.WEIGHT -> R.string.onboarding_tracking_subtitle_weight
                    DashboardCardType.CAFFEINE -> R.string.onboarding_tracking_subtitle_caffeine
                    DashboardCardType.SMOKING -> R.string.onboarding_tracking_subtitle_smoking
                    DashboardCardType.EXERCISE -> R.string.onboarding_tracking_subtitle_exercise
                    DashboardCardType.SUPPLEMENTS -> R.string.onboarding_tracking_subtitle_supplements
                }
                HealthCard(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAction(OnboardingAction.ToggleTrackingArea(area)) }
                        .testTag("onboarding_tracking_card_${area.name}"),
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = HealthSpacing.xs)) {
                            Text(
                                text = stringResource(titleRes),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            )
                            Text(
                                text = stringResource(subtitleRes),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        if (isSelected) {
                            Box(
                                modifier = Modifier.size(20.dp).background(MaterialTheme.colorScheme.primary, shape = androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("✓", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            Box(
                                modifier = Modifier.size(20.dp).background(MaterialTheme.colorScheme.surfaceVariant, shape = androidx.compose.foundation.shape.CircleShape),
                            )
                        }
                    }
                }
            }
        }

        state.validationErrors[OnboardingFieldKeys.TRACKING_AREAS]?.let { error ->
            Text(text = error.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun BasicInfoStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_basic_info"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.onboarding_basic_info_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_basic_info_body),
            style = MaterialTheme.typography.bodyMedium,
        )

        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_name),
            value = state.name,
            onValueChange = { onAction(OnboardingAction.UpdateName(it)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        )
        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_age),
            value = state.age,
            onValueChange = { onAction(OnboardingAction.UpdateAge(it)) },
            isError = state.validationErrors[OnboardingFieldKeys.AGE] != null,
            supportingText = state.validationErrors[OnboardingFieldKeys.AGE]?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        Text(text = stringResource(R.string.onboarding_label_sex), style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
            OnboardingSex.entries.forEach { sex ->
                val labelRes = when (sex) {
                    OnboardingSex.MALE -> R.string.onboarding_sex_male
                    OnboardingSex.FEMALE -> R.string.onboarding_sex_female
                    OnboardingSex.UNSPECIFIED -> R.string.onboarding_sex_unspecified
                }
                val selected = state.sex == sex
                Box(
                    modifier = Modifier.weight(1f).background(
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.large,
                    ).clickable { onAction(OnboardingAction.UpdateSex(sex)) }
                        .padding(HealthSpacing.xs),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = stringResource(labelRes), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_height),
            value = state.heightCm,
            onValueChange = { onAction(OnboardingAction.UpdateHeight(it)) },
            isError = state.validationErrors[OnboardingFieldKeys.HEIGHT] != null,
            supportingText = state.validationErrors[OnboardingFieldKeys.HEIGHT]?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("cm") },
        )
        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_current_weight),
            value = state.currentWeightKg,
            onValueChange = { onAction(OnboardingAction.UpdateCurrentWeight(it)) },
            isError = state.validationErrors[OnboardingFieldKeys.CURRENT_WEIGHT] != null,
            supportingText = state.validationErrors[OnboardingFieldKeys.CURRENT_WEIGHT]?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("kg") },
        )
        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_target_weight),
            value = state.targetWeightKg,
            onValueChange = { onAction(OnboardingAction.UpdateTargetWeight(it)) },
            isError = state.validationErrors[OnboardingFieldKeys.TARGET_WEIGHT] != null,
            supportingText = state.validationErrors[OnboardingFieldKeys.TARGET_WEIGHT]?.asString(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            suffix = { Text("kg") },
        )
    }
}

@Composable
fun ActivityGoalStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_activity_goal"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.onboarding_activity_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_activity_body),
            style = MaterialTheme.typography.bodyMedium,
        )

        OnboardingActivityLevel.entries.forEach { level ->
            val selected = state.activityLevel == level
            val titleRes = when (level) {
                OnboardingActivityLevel.LOW -> R.string.onboarding_activity_low
                OnboardingActivityLevel.LIGHT -> R.string.onboarding_activity_light
                OnboardingActivityLevel.MODERATE -> R.string.onboarding_activity_moderate
                OnboardingActivityLevel.HIGH -> R.string.onboarding_activity_high
            }
            val descRes = when (level) {
                OnboardingActivityLevel.LOW -> R.string.onboarding_activity_low_desc
                OnboardingActivityLevel.LIGHT -> R.string.onboarding_activity_light_desc
                OnboardingActivityLevel.MODERATE -> R.string.onboarding_activity_moderate_desc
                OnboardingActivityLevel.HIGH -> R.string.onboarding_activity_high_desc
            }
            HealthCard(
                modifier = Modifier.fillMaxWidth().clickable { onAction(OnboardingAction.UpdateActivityLevel(level)) },
                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            ) {
                Column {
                    Text(text = stringResource(titleRes), fontWeight = FontWeight.Bold)
                    Text(text = stringResource(descRes), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.size(HealthSpacing.xs))

        OnboardingMainGoal.entries.forEach { goal ->
            val selected = state.mainGoal == goal
            val titleRes = when (goal) {
                OnboardingMainGoal.MAINTAIN -> R.string.onboarding_goal_maintain
                OnboardingMainGoal.SLOW_GAIN -> R.string.onboarding_goal_slow_gain
                OnboardingMainGoal.SLOW_LOSS -> R.string.onboarding_goal_slow_loss
            }
            HealthCard(
                modifier = Modifier.fillMaxWidth().clickable { onAction(OnboardingAction.UpdateMainGoal(goal)) },
                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            ) {
                Text(text = stringResource(titleRes), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SmartGoalsStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_smart_goals"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.onboarding_smart_goals_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_smart_goals_body),
            style = MaterialTheme.typography.bodyMedium,
        )

        if (state.selectedTrackingAreas.contains(DashboardCardType.HYDRATION)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_hydration), fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.onboarding_water_suggestion_info), style = MaterialTheme.typography.bodySmall)
                HealthPillTextField(
                    label = stringResource(R.string.onboarding_water_goal_field),
                    value = state.waterTargetMl,
                    onValueChange = { onAction(OnboardingAction.UpdateWaterTarget(it)) },
                    isError = state.validationErrors[OnboardingFieldKeys.WATER] != null,
                    supportingText = state.validationErrors[OnboardingFieldKeys.WATER]?.asString(),
                    modifier = Modifier.testTag("onboarding_water_goal_field"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("ml") },
                )
                Text(text = stringResource(R.string.onboarding_not_medical_advice_short), style = MaterialTheme.typography.labelSmall)
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.NUTRITION)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_nutrition), fontWeight = FontWeight.Bold)
                if (state.age.isBlank() || state.heightCm.isBlank() || state.currentWeightKg.isBlank() || state.sex == OnboardingSex.UNSPECIFIED) {
                    Text(text = stringResource(R.string.onboarding_nutrition_missing_data), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
                HealthPillTextField(
                    label = stringResource(R.string.profile_goal_calories),
                    value = state.dailyCaloriesTarget,
                    onValueChange = { onAction(OnboardingAction.UpdateCalories(it)) },
                    isError = state.validationErrors[OnboardingFieldKeys.CALORIES] != null,
                    supportingText = state.validationErrors[OnboardingFieldKeys.CALORIES]?.asString(),
                    modifier = Modifier.testTag("onboarding_calorie_goal_field"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("kcal") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    HealthPillTextField(label = stringResource(R.string.profile_goal_protein), value = state.proteinTargetGrams, onValueChange = { onAction(OnboardingAction.UpdateProtein(it)) }, isError = state.validationErrors[OnboardingFieldKeys.PROTEIN] != null, supportingText = state.validationErrors[OnboardingFieldKeys.PROTEIN]?.asString(), modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("g") })
                    HealthPillTextField(label = stringResource(R.string.profile_goal_carbs), value = state.carbTargetGrams, onValueChange = { onAction(OnboardingAction.UpdateCarbs(it)) }, isError = state.validationErrors[OnboardingFieldKeys.CARBS] != null, supportingText = state.validationErrors[OnboardingFieldKeys.CARBS]?.asString(), modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("g") })
                    HealthPillTextField(label = stringResource(R.string.profile_goal_fat), value = state.fatTargetGrams, onValueChange = { onAction(OnboardingAction.UpdateFat(it)) }, isError = state.validationErrors[OnboardingFieldKeys.FAT] != null, supportingText = state.validationErrors[OnboardingFieldKeys.FAT]?.asString(), modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("g") })
                }
                Text(text = stringResource(R.string.onboarding_not_medical_advice_short), style = MaterialTheme.typography.labelSmall)
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.CAFFEINE)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_caffeine), fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.onboarding_caffeine_suggestion_info), style = MaterialTheme.typography.bodySmall)
                HealthPillTextField(
                    label = stringResource(R.string.onboarding_caffeine_limit_label),
                    value = state.dailyCaffeineLimitMg,
                    onValueChange = { onAction(OnboardingAction.UpdateCaffeineLimit(it)) },
                    isError = state.validationErrors[OnboardingFieldKeys.CAFFEINE_LIMIT] != null,
                    supportingText = state.validationErrors[OnboardingFieldKeys.CAFFEINE_LIMIT]?.asString(),
                    modifier = Modifier.testTag("onboarding_caffeine_limit_field"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    suffix = { Text("mg") },
                )
                HealthPillTextField(
                    label = stringResource(R.string.profile_goal_caffeine_cutoff_time),
                    value = state.caffeineCutoffTime,
                    onValueChange = { onAction(OnboardingAction.UpdateCaffeineCutoff(it)) },
                    isError = state.validationErrors[OnboardingFieldKeys.CAFFEINE_CUTOFF] != null,
                    supportingText = state.validationErrors[OnboardingFieldKeys.CAFFEINE_CUTOFF]?.asString(),
                    modifier = Modifier.testTag("onboarding_caffeine_cutoff_field"),
                )
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.SLEEP)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_sleep), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    HealthPillTextField(label = stringResource(R.string.profile_goal_bedtime), value = state.sleepBedtime, onValueChange = { onAction(OnboardingAction.UpdateSleepBedtime(it)) }, isError = state.validationErrors[OnboardingFieldKeys.SLEEP_BEDTIME] != null, supportingText = state.validationErrors[OnboardingFieldKeys.SLEEP_BEDTIME]?.asString(), modifier = Modifier.weight(1f))
                    HealthPillTextField(label = stringResource(R.string.profile_goal_wake_time), value = state.sleepWakeTime, onValueChange = { onAction(OnboardingAction.UpdateSleepWakeTime(it)) }, isError = state.validationErrors[OnboardingFieldKeys.SLEEP_WAKE_TIME] != null, supportingText = state.validationErrors[OnboardingFieldKeys.SLEEP_WAKE_TIME]?.asString(), modifier = Modifier.weight(1f))
                }
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.STEPS)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_steps), fontWeight = FontWeight.Bold)
                HealthPillTextField(label = stringResource(R.string.profile_goal_step_target), value = state.dailyStepTarget, onValueChange = { onAction(OnboardingAction.UpdateDailySteps(it)) }, isError = state.validationErrors[OnboardingFieldKeys.DAILY_STEPS] != null, supportingText = state.validationErrors[OnboardingFieldKeys.DAILY_STEPS]?.asString(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.EXERCISE)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_exercise), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    HealthPillTextField(label = stringResource(R.string.profile_goal_exercise_days), value = state.exerciseDaysPerWeek, onValueChange = { onAction(OnboardingAction.UpdateExerciseDays(it)) }, modifier = Modifier.weight(1f), isError = state.validationErrors[OnboardingFieldKeys.EXERCISE_DAYS] != null, supportingText = state.validationErrors[OnboardingFieldKeys.EXERCISE_DAYS]?.asString(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    HealthPillTextField(label = stringResource(R.string.profile_goal_exercise_duration), value = state.exerciseDurationMinutes, onValueChange = { onAction(OnboardingAction.UpdateExerciseDuration(it)) }, modifier = Modifier.weight(1f), isError = state.validationErrors[OnboardingFieldKeys.EXERCISE_DURATION] != null, supportingText = state.validationErrors[OnboardingFieldKeys.EXERCISE_DURATION]?.asString(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("dk") })
                }
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.SMOKING)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_smoking), fontWeight = FontWeight.Bold)
                HealthPillTextField(label = stringResource(R.string.profile_goal_smoke_limit), value = state.smokeDailyLimit, onValueChange = { onAction(OnboardingAction.UpdateSmokeLimit(it)) }, isError = state.validationErrors[OnboardingFieldKeys.SMOKE_LIMIT] != null, supportingText = state.validationErrors[OnboardingFieldKeys.SMOKE_LIMIT]?.asString(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
        }
    }
}

@Composable
fun PreferencesStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_preferences"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.onboarding_preferences_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_preferences_body),
            style = MaterialTheme.typography.bodyMedium,
        )

        if (state.selectedTrackingAreas.contains(DashboardCardType.HYDRATION)) {
            HealthCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.profile_water_reminder_title), fontWeight = FontWeight.Bold)
                    Switch(checked = state.waterReminderEnabled, onCheckedChange = { onAction(OnboardingAction.UpdateWaterReminderEnabled(it)) }, colors = SwitchDefaults.colors(checkedTrackColor = HealthPrimary))
                }
                if (state.waterReminderEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                        HealthPillTextField(label = stringResource(R.string.profile_goal_start), value = state.waterReminderStartTime, onValueChange = { onAction(OnboardingAction.UpdateWaterReminderStart(it)) }, isError = state.validationErrors[OnboardingFieldKeys.REMINDER_START_TIME] != null, supportingText = state.validationErrors[OnboardingFieldKeys.REMINDER_START_TIME]?.asString(), modifier = Modifier.weight(1f))
                        HealthPillTextField(label = stringResource(R.string.profile_goal_end), value = state.waterReminderEndTime, onValueChange = { onAction(OnboardingAction.UpdateWaterReminderEnd(it)) }, isError = state.validationErrors[OnboardingFieldKeys.REMINDER_END_TIME] != null, supportingText = state.validationErrors[OnboardingFieldKeys.REMINDER_END_TIME]?.asString(), modifier = Modifier.weight(1f))
                        HealthPillTextField(label = stringResource(R.string.profile_goal_frequency), value = state.waterReminderIntervalMinutes, onValueChange = { onAction(OnboardingAction.UpdateWaterReminderInterval(it)) }, modifier = Modifier.weight(1f), isError = state.validationErrors[OnboardingFieldKeys.REMINDER_INTERVAL] != null, supportingText = state.validationErrors[OnboardingFieldKeys.REMINDER_INTERVAL]?.asString(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), suffix = { Text("dk") })
                    }
                }
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.STEPS)) {
            HealthCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.profile_step_tracking_title), fontWeight = FontWeight.Bold)
                        Text(text = stringResource(R.string.profile_goal_step_tracking_helper), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = state.stepTrackingPreferred, onCheckedChange = { onAction(OnboardingAction.UpdateStepTrackingPreferred(it)) }, colors = SwitchDefaults.colors(checkedTrackColor = HealthPrimary))
                }
            }
        }
    }
}

@Composable
fun DoneStep() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_done"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.onboarding_done_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.onboarding_done_body),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
