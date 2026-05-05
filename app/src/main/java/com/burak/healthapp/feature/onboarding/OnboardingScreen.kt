package com.burak.healthapp.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.DashboardCardType

@Composable
fun OnboardingRoute() {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                is OnboardingAction.NextClicked -> viewModel.goToNextStep()
                is OnboardingAction.BackClicked -> viewModel.goToPreviousStep()
                is OnboardingAction.SkipWithDefaults -> viewModel.skipWithDefaults()
                is OnboardingAction.ToggleTrackingArea -> viewModel.onTrackingAreaToggled(action.area)
                is OnboardingAction.UpdateName -> viewModel.updateName(action.value)
                is OnboardingAction.UpdateAge -> viewModel.updateAge(action.value)
                is OnboardingAction.UpdateSex -> viewModel.updateSex(action.value)
                is OnboardingAction.UpdateHeight -> viewModel.updateHeightCm(action.value)
                is OnboardingAction.UpdateCurrentWeight -> viewModel.updateCurrentWeightKg(action.value)
                is OnboardingAction.UpdateTargetWeight -> viewModel.updateTargetWeightKg(action.value)
                is OnboardingAction.UpdateActivityLevel -> viewModel.updateActivityLevel(action.value)
                is OnboardingAction.UpdateMainGoal -> viewModel.updateMainGoal(action.value)
                is OnboardingAction.UpdateWaterTarget -> viewModel.updateWaterTargetMl(action.value)
                is OnboardingAction.UpdateSleepBedtime -> viewModel.updateSleepBedtime(action.value)
                is OnboardingAction.UpdateSleepWakeTime -> viewModel.updateSleepWakeTime(action.value)
                is OnboardingAction.UpdateCaffeineLimit -> viewModel.updateDailyCaffeineLimitMg(action.value)
                is OnboardingAction.UpdateCaffeineCutoff -> viewModel.updateCaffeineCutoffTime(action.value)
                is OnboardingAction.UpdateCalories -> viewModel.updateDailyCaloriesTarget(action.value)
                is OnboardingAction.UpdateProtein -> viewModel.updateProteinTargetGrams(action.value)
                is OnboardingAction.UpdateCarbs -> viewModel.updateCarbTargetGrams(action.value)
                is OnboardingAction.UpdateFat -> viewModel.updateFatTargetGrams(action.value)
                is OnboardingAction.UpdateExerciseDays -> viewModel.updateExerciseDaysPerWeek(action.value)
                is OnboardingAction.UpdateExerciseDuration -> viewModel.updateExerciseDurationMinutes(action.value)
                is OnboardingAction.UpdateSmokeLimit -> viewModel.updateSmokeDailyLimit(action.value)
                is OnboardingAction.UpdateDailySteps -> viewModel.updateDailyStepTarget(action.value)
                is OnboardingAction.UpdateWaterReminderEnabled -> viewModel.updateWaterReminderEnabled(action.value)
                is OnboardingAction.UpdateWaterReminderStart -> viewModel.updateWaterReminderStartTime(action.value)
                is OnboardingAction.UpdateWaterReminderEnd -> viewModel.updateWaterReminderEndTime(action.value)
                is OnboardingAction.UpdateWaterReminderInterval -> viewModel.updateWaterReminderIntervalMinutes(action.value)
                is OnboardingAction.UpdateStepTrackingPreferred -> viewModel.updateStepTrackingPreferred(action.value)
            }
        }
    )
}

sealed interface OnboardingAction {
    data object NextClicked : OnboardingAction
    data object BackClicked : OnboardingAction
    data object SkipWithDefaults : OnboardingAction
    data class ToggleTrackingArea(val area: DashboardCardType) : OnboardingAction
    data class UpdateName(val value: String) : OnboardingAction
    data class UpdateAge(val value: String) : OnboardingAction
    data class UpdateSex(val value: OnboardingSex) : OnboardingAction
    data class UpdateHeight(val value: String) : OnboardingAction
    data class UpdateCurrentWeight(val value: String) : OnboardingAction
    data class UpdateTargetWeight(val value: String) : OnboardingAction
    data class UpdateActivityLevel(val value: OnboardingActivityLevel) : OnboardingAction
    data class UpdateMainGoal(val value: OnboardingMainGoal) : OnboardingAction
    data class UpdateWaterTarget(val value: String) : OnboardingAction
    data class UpdateDailySteps(val value: String) : OnboardingAction
    data class UpdateSleepBedtime(val value: String) : OnboardingAction
    data class UpdateSleepWakeTime(val value: String) : OnboardingAction
    data class UpdateCaffeineLimit(val value: String) : OnboardingAction
    data class UpdateCaffeineCutoff(val value: String) : OnboardingAction
    data class UpdateCalories(val value: String) : OnboardingAction
    data class UpdateProtein(val value: String) : OnboardingAction
    data class UpdateCarbs(val value: String) : OnboardingAction
    data class UpdateFat(val value: String) : OnboardingAction
    data class UpdateExerciseDays(val value: String) : OnboardingAction
    data class UpdateExerciseDuration(val value: String) : OnboardingAction
    data class UpdateSmokeLimit(val value: String) : OnboardingAction
    data class UpdateWaterReminderEnabled(val value: Boolean) : OnboardingAction
    data class UpdateWaterReminderStart(val value: String) : OnboardingAction
    data class UpdateWaterReminderEnd(val value: String) : OnboardingAction
    data class UpdateWaterReminderInterval(val value: String) : OnboardingAction
    data class UpdateStepTrackingPreferred(val value: Boolean) : OnboardingAction
}

@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onAction: (OnboardingAction) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("onboarding_root"),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = HealthSpacing.sm, vertical = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            item {
                OnboardingHeader(step = state.currentStep)
            }
            
            item {
                when (state.currentStep) {
                    OnboardingStep.WELCOME -> WelcomeStep()
                    OnboardingStep.TRACKING_AREAS -> TrackingAreasStep(state, onAction)
                    OnboardingStep.BASIC_INFO -> BasicInfoStep(state, onAction)
                    OnboardingStep.ACTIVITY_GOAL -> ActivityGoalStep(state, onAction)
                    OnboardingStep.SMART_GOALS -> SmartGoalsStep(state, onAction)
                    OnboardingStep.PREFERENCES -> PreferencesStep(state, onAction)
                    OnboardingStep.DONE -> DoneStep()
                }
            }

            if (state.isSaving) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = HealthSpacing.xs),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = HealthPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.size(HealthSpacing.xs))
                        Text(text = stringResource(R.string.common_saving), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f))
                    }
                }
            } else {
                item {
                    OnboardingFooter(
                        step = state.currentStep,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeader(step: OnboardingStep) {
    if (step == OnboardingStep.WELCOME || step == OnboardingStep.DONE) return
    
    val currentIdx = step.ordinal
    val totalIdx = OnboardingStep.entries.size - 2 // Exclude WELCOME and DONE from progress visually
    Text(
        text = "$currentIdx / $totalIdx",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        modifier = Modifier.padding(bottom = HealthSpacing.xs)
    )
}

@Composable
private fun OnboardingFooter(
    step: OnboardingStep,
    onAction: (OnboardingAction) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        if (step != OnboardingStep.WELCOME && step != OnboardingStep.DONE) {
            RoundedPillButton(
                label = stringResource(R.string.onboarding_back),
                modifier = Modifier.weight(1f).testTag("onboarding_back_button"),
                onClick = { onAction(OnboardingAction.BackClicked) },
            )
        }
        
        if (step == OnboardingStep.WELCOME) {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm), modifier = Modifier.fillMaxWidth()) {
                RoundedPillButton(
                    label = stringResource(R.string.onboarding_start),
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_next_button"),
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                    onClick = { onAction(OnboardingAction.NextClicked) },
                )
                RoundedPillButton(
                    label = stringResource(R.string.onboarding_use_defaults),
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onAction(OnboardingAction.SkipWithDefaults) },
                )
            }
        } else {
            val primaryLabel = if (step == OnboardingStep.DONE) stringResource(R.string.onboarding_go_today) else stringResource(R.string.onboarding_next)
            val testTag = if (step == OnboardingStep.DONE) "onboarding_finish_button" else "onboarding_next_button"
            RoundedPillButton(
                label = primaryLabel,
                modifier = Modifier.weight(1f).testTag(testTag),
                containerColor = HealthPrimary,
                contentColor = Color.White,
                onClick = { onAction(OnboardingAction.NextClicked) },
            )
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_welcome"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrackingAreasStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_tracking_areas"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)
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
            HealthCard(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onAction(OnboardingAction.ToggleTrackingArea(area)) }
                    .testTag("onboarding_tracking_card_${area.name}"),
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(titleRes),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Icon(
                            painter = painterResource(android.R.drawable.checkbox_on_background),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            painter = painterResource(android.R.drawable.checkbox_off_background),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        state.validationErrors["tracking_areas"]?.let { error ->
            Text(text = error.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun BasicInfoStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_basic_info"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)
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
            onValueChange = { onAction(OnboardingAction.UpdateName(it)) }
        )
        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_age),
            value = state.age,
            onValueChange = { onAction(OnboardingAction.UpdateAge(it)) },
            isError = state.validationErrors["age"] != null,
            supportingText = state.validationErrors["age"]?.asString()
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
                        shape = MaterialTheme.shapes.large
                    ).clickable { onAction(OnboardingAction.UpdateSex(sex)) }
                    .padding(HealthSpacing.xs),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(labelRes), style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_height),
            value = state.heightCm,
            onValueChange = { onAction(OnboardingAction.UpdateHeight(it)) },
            isError = state.validationErrors["height"] != null,
            supportingText = state.validationErrors["height"]?.asString()
        )
        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_current_weight),
            value = state.currentWeightKg,
            onValueChange = { onAction(OnboardingAction.UpdateCurrentWeight(it)) },
            isError = state.validationErrors["currentWeight"] != null,
            supportingText = state.validationErrors["currentWeight"]?.asString()
        )
        HealthPillTextField(
            label = stringResource(R.string.onboarding_label_target_weight),
            value = state.targetWeightKg,
            onValueChange = { onAction(OnboardingAction.UpdateTargetWeight(it)) },
            isError = state.validationErrors["targetWeight"] != null,
            supportingText = state.validationErrors["targetWeight"]?.asString()
        )
    }
}

@Composable
private fun ActivityGoalStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_activity_goal"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)
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
            val titleRes = when(level) {
                OnboardingActivityLevel.LOW -> R.string.onboarding_activity_low
                OnboardingActivityLevel.LIGHT -> R.string.onboarding_activity_light
                OnboardingActivityLevel.MODERATE -> R.string.onboarding_activity_moderate
                OnboardingActivityLevel.HIGH -> R.string.onboarding_activity_high
            }
            val descRes = when(level) {
                OnboardingActivityLevel.LOW -> R.string.onboarding_activity_low_desc
                OnboardingActivityLevel.LIGHT -> R.string.onboarding_activity_light_desc
                OnboardingActivityLevel.MODERATE -> R.string.onboarding_activity_moderate_desc
                OnboardingActivityLevel.HIGH -> R.string.onboarding_activity_high_desc
            }
            HealthCard(
                modifier = Modifier.fillMaxWidth().clickable { onAction(OnboardingAction.UpdateActivityLevel(level)) },
                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
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
            val titleRes = when(goal) {
                OnboardingMainGoal.MAINTAIN -> R.string.onboarding_goal_maintain
                OnboardingMainGoal.SLOW_GAIN -> R.string.onboarding_goal_slow_gain
                OnboardingMainGoal.SLOW_LOSS -> R.string.onboarding_goal_slow_loss
            }
            HealthCard(
                modifier = Modifier.fillMaxWidth().clickable { onAction(OnboardingAction.UpdateMainGoal(goal)) },
                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ) {
                Text(text = stringResource(titleRes), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SmartGoalsStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_smart_goals"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)
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
            isError = state.validationErrors["water"] != null,
            supportingText = state.validationErrors["water"]?.asString(),
            modifier = Modifier.testTag("onboarding_water_goal_field")
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
            isError = state.validationErrors["calories"] != null,
            supportingText = state.validationErrors["calories"]?.asString(),
            modifier = Modifier.testTag("onboarding_calorie_goal_field")
        )
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    HealthPillTextField(label = stringResource(R.string.profile_goal_protein), value = state.proteinTargetGrams, onValueChange = { onAction(OnboardingAction.UpdateProtein(it)) }, modifier = Modifier.weight(1f))
                    HealthPillTextField(label = stringResource(R.string.profile_goal_carbs), value = state.carbTargetGrams, onValueChange = { onAction(OnboardingAction.UpdateCarbs(it)) }, modifier = Modifier.weight(1f))
                    HealthPillTextField(label = stringResource(R.string.profile_goal_fat), value = state.fatTargetGrams, onValueChange = { onAction(OnboardingAction.UpdateFat(it)) }, modifier = Modifier.weight(1f))
                }
                Text(text = stringResource(R.string.onboarding_not_medical_advice_short), style = MaterialTheme.typography.labelSmall)
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.CAFFEINE)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_caffeine), fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.onboarding_caffeine_suggestion_info), style = MaterialTheme.typography.bodySmall)
                HealthPillTextField(
                    label = stringResource(R.string.profile_goal_caffeine_cutoff_time),
                    value = state.caffeineCutoffTime,
                    onValueChange = { onAction(OnboardingAction.UpdateCaffeineCutoff(it)) },
                    modifier = Modifier.testTag("onboarding_caffeine_cutoff_field")
                )
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.SLEEP)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_sleep), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    HealthPillTextField(label = stringResource(R.string.profile_goal_bedtime), value = state.sleepBedtime, onValueChange = { onAction(OnboardingAction.UpdateSleepBedtime(it)) }, modifier = Modifier.weight(1f))
                    HealthPillTextField(label = stringResource(R.string.profile_goal_wake_time), value = state.sleepWakeTime, onValueChange = { onAction(OnboardingAction.UpdateSleepWakeTime(it)) }, modifier = Modifier.weight(1f))
                }
            }
        }
        
        if (state.selectedTrackingAreas.contains(DashboardCardType.STEPS)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_steps), fontWeight = FontWeight.Bold)
                HealthPillTextField(label = stringResource(R.string.profile_goal_step_target), value = state.dailyStepTarget, onValueChange = { onAction(OnboardingAction.UpdateDailySteps(it)) })
            }
        }

        if (state.selectedTrackingAreas.contains(DashboardCardType.EXERCISE)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_exercise), fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                    HealthPillTextField(label = stringResource(R.string.profile_goal_exercise_days), value = state.exerciseDaysPerWeek, onValueChange = { onAction(OnboardingAction.UpdateExerciseDays(it)) }, modifier = Modifier.weight(1f), isError = state.validationErrors["exercise_days"] != null, supportingText = state.validationErrors["exercise_days"]?.asString())
                    HealthPillTextField(label = stringResource(R.string.profile_goal_exercise_duration), value = state.exerciseDurationMinutes, onValueChange = { onAction(OnboardingAction.UpdateExerciseDuration(it)) }, modifier = Modifier.weight(1f))
                }
            }
        }
        
        if (state.selectedTrackingAreas.contains(DashboardCardType.SMOKING)) {
            HealthCard {
                Text(text = stringResource(R.string.dashboard_card_smoking), fontWeight = FontWeight.Bold)
                HealthPillTextField(label = stringResource(R.string.profile_goal_smoke_limit), value = state.smokeDailyLimit, onValueChange = { onAction(OnboardingAction.UpdateSmokeLimit(it)) })
            }
        }
    }
}

@Composable
private fun PreferencesStep(state: OnboardingUiState, onAction: (OnboardingAction) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_preferences"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)
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
                        HealthPillTextField(label = stringResource(R.string.profile_goal_start), value = state.waterReminderStartTime, onValueChange = { onAction(OnboardingAction.UpdateWaterReminderStart(it)) }, modifier = Modifier.weight(1f))
                        HealthPillTextField(label = stringResource(R.string.profile_goal_end), value = state.waterReminderEndTime, onValueChange = { onAction(OnboardingAction.UpdateWaterReminderEnd(it)) }, modifier = Modifier.weight(1f))
                        HealthPillTextField(label = stringResource(R.string.profile_goal_frequency), value = state.waterReminderIntervalMinutes, onValueChange = { onAction(OnboardingAction.UpdateWaterReminderInterval(it)) }, modifier = Modifier.weight(1f), isError = state.validationErrors["reminder_interval"] != null, supportingText = state.validationErrors["reminder_interval"]?.asString())
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
private fun DoneStep() {
    Column(
        modifier = Modifier.fillMaxWidth().testTag("onboarding_step_done"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)
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
