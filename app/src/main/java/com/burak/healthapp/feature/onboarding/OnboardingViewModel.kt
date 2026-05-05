package com.burak.healthapp.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.validation.parseLocalizedDecimalInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onTrackingAreaToggled(area: DashboardCardType) {
        _uiState.update { state ->
            val newSelected = if (state.selectedTrackingAreas.contains(area)) {
                state.selectedTrackingAreas - area
            } else {
                state.selectedTrackingAreas + area
            }
            state.copy(selectedTrackingAreas = newSelected)
        }
    }

    fun updateName(value: String) { _uiState.update { it.copy(name = value) } }
    fun updateAge(value: String) { _uiState.update { it.copy(age = value) } }
    fun updateSex(value: OnboardingSex) { _uiState.update { it.copy(sex = value) } }
    fun updateHeightCm(value: String) { _uiState.update { it.copy(heightCm = value) } }
    fun updateCurrentWeightKg(value: String) { _uiState.update { it.copy(currentWeightKg = value) } }
    fun updateTargetWeightKg(value: String) { _uiState.update { it.copy(targetWeightKg = value) } }
    
    fun updateActivityLevel(value: OnboardingActivityLevel) { _uiState.update { it.copy(activityLevel = value) } }
    fun updateMainGoal(value: OnboardingMainGoal) { _uiState.update { it.copy(mainGoal = value) } }

    fun updateWaterTargetMl(value: String) { _uiState.update { it.copy(waterTargetMl = value) } }
    fun updateDailyStepTarget(value: String) { _uiState.update { it.copy(dailyStepTarget = value) } }
    fun updateSleepBedtime(value: String) { _uiState.update { it.copy(sleepBedtime = value) } }
    fun updateSleepWakeTime(value: String) { _uiState.update { it.copy(sleepWakeTime = value) } }
    fun updateDailyCaffeineLimitMg(value: String) { _uiState.update { it.copy(dailyCaffeineLimitMg = value) } }
    fun updateCaffeineCutoffTime(value: String) { _uiState.update { it.copy(caffeineCutoffTime = value) } }
    fun updateDailyCaloriesTarget(value: String) { _uiState.update { it.copy(dailyCaloriesTarget = value) } }
    fun updateProteinTargetGrams(value: String) { _uiState.update { it.copy(proteinTargetGrams = value) } }
    fun updateCarbTargetGrams(value: String) { _uiState.update { it.copy(carbTargetGrams = value) } }
    fun updateFatTargetGrams(value: String) { _uiState.update { it.copy(fatTargetGrams = value) } }
    fun updateExerciseDaysPerWeek(value: String) { _uiState.update { it.copy(exerciseDaysPerWeek = value) } }
    fun updateExerciseDurationMinutes(value: String) { _uiState.update { it.copy(exerciseDurationMinutes = value) } }
    fun updateSmokeDailyLimit(value: String) { _uiState.update { it.copy(smokeDailyLimit = value) } }

    fun updateWaterReminderEnabled(value: Boolean) { _uiState.update { it.copy(waterReminderEnabled = value) } }
    fun updateWaterReminderStartTime(value: String) { _uiState.update { it.copy(waterReminderStartTime = value) } }
    fun updateWaterReminderEndTime(value: String) { _uiState.update { it.copy(waterReminderEndTime = value) } }
    fun updateWaterReminderIntervalMinutes(value: String) { _uiState.update { it.copy(waterReminderIntervalMinutes = value) } }
    fun updateStepTrackingPreferred(value: Boolean) { _uiState.update { it.copy(stepTrackingPreferred = value) } }

    fun goToNextStep() {
        val currentState = _uiState.value
        val errors = validateCurrentStep(currentState)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            return
        }

        _uiState.update { it.copy(validationErrors = emptyMap()) }

        when (currentState.currentStep) {
            OnboardingStep.WELCOME -> _uiState.update { it.copy(currentStep = OnboardingStep.TRACKING_AREAS) }
            OnboardingStep.TRACKING_AREAS -> _uiState.update { it.copy(currentStep = OnboardingStep.BASIC_INFO) }
            OnboardingStep.BASIC_INFO -> _uiState.update { it.copy(currentStep = OnboardingStep.ACTIVITY_GOAL) }
            OnboardingStep.ACTIVITY_GOAL -> {
                generateSmartGoals()
                _uiState.update { it.copy(currentStep = OnboardingStep.SMART_GOALS) }
            }
            OnboardingStep.SMART_GOALS -> {
                populatePreferencesDefaults()
                _uiState.update { it.copy(currentStep = OnboardingStep.PREFERENCES) }
            }
            OnboardingStep.PREFERENCES -> _uiState.update { it.copy(currentStep = OnboardingStep.DONE) }
            OnboardingStep.DONE -> finishOnboarding()
        }
    }

    fun goToPreviousStep() {
        val currentState = _uiState.value
        _uiState.update { it.copy(validationErrors = emptyMap()) }
        when (currentState.currentStep) {
            OnboardingStep.WELCOME -> {}
            OnboardingStep.TRACKING_AREAS -> _uiState.update { it.copy(currentStep = OnboardingStep.WELCOME) }
            OnboardingStep.BASIC_INFO -> _uiState.update { it.copy(currentStep = OnboardingStep.TRACKING_AREAS) }
            OnboardingStep.ACTIVITY_GOAL -> _uiState.update { it.copy(currentStep = OnboardingStep.BASIC_INFO) }
            OnboardingStep.SMART_GOALS -> _uiState.update { it.copy(currentStep = OnboardingStep.ACTIVITY_GOAL) }
            OnboardingStep.PREFERENCES -> _uiState.update { it.copy(currentStep = OnboardingStep.SMART_GOALS) }
            OnboardingStep.DONE -> _uiState.update { it.copy(currentStep = OnboardingStep.PREFERENCES) }
        }
    }

    fun skipWithDefaults() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val profile = UserProfile.fromName("")
            val goals = GoalSettings()
            val measurement = BodyMeasurementEntry(
                date = LocalDate.now(),
                weightKg = goals.baselineWeightKg,
                shoulderCm = goals.baselineShoulderCm,
                waistCm = goals.baselineWaistCm,
                hipCm = goals.baselineHipCm,
            )
            settingsRepository.completeOnboarding(
                profile = profile,
                goals = goals,
                initialMeasurement = measurement,
                supplements = emptyList(),
            )
            // Save dashboard config and preferences
            val defaultTrackingAreas = defaultOnboardingTrackingAreas()
            val dashboardConfig = buildDashboardConfigFromTrackingAreas(defaultTrackingAreas)
            dashboardConfig.forEach { config ->
                settingsRepository.updateDashboardCardVisibility(config.type, config.isVisible)
            }
            settingsRepository.updateWaterReminderSettings(WaterReminderSettings(enabled = false))
            settingsRepository.updateStepTrackingEnabled(false)
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun finishOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            val profileName = state.name.trim().ifBlank { "Misafir" }
            val profile = UserProfile.fromName(profileName, heightCm = state.heightCm.toFloatOrNull())

            val currentWeight = state.currentWeightKg.toFloatOrNull() ?: DefaultHealthGoals.BASELINE_WEIGHT_KG
            
            val goals = GoalSettings(
                dailyCaloriesTarget = state.dailyCaloriesTarget.toIntOrNull() ?: DefaultHealthGoals.DAILY_CALORIES,
                proteinTargetGrams = state.proteinTargetGrams.toIntOrNull() ?: DefaultHealthGoals.PROTEIN_GRAMS,
                carbTargetGrams = state.carbTargetGrams.toIntOrNull() ?: DefaultHealthGoals.CARB_GRAMS,
                fatTargetGrams = state.fatTargetGrams.toIntOrNull() ?: DefaultHealthGoals.FAT_GRAMS,
                waterTargetMl = state.waterTargetMl.toIntOrNull() ?: DefaultHealthGoals.WATER_TARGET_ML,
                sleepTargetBedtime = state.sleepBedtime.toLocalTimeOrNull() ?: DefaultHealthGoals.SLEEP_BEDTIME,
                sleepTargetWakeTime = state.sleepWakeTime.toLocalTimeOrNull() ?: DefaultHealthGoals.SLEEP_WAKE_TIME,
                exerciseTargetDaysPerWeek = state.exerciseDaysPerWeek.toIntOrNull() ?: DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK,
                exerciseTargetDurationMinutes = state.exerciseDurationMinutes.toIntOrNull() ?: DefaultHealthGoals.EXERCISE_DURATION_MINUTES,
                smokeDailyLimit = state.smokeDailyLimit.toIntOrNull() ?: DefaultHealthGoals.SMOKE_DAILY_LIMIT,
                baselineWeightKg = currentWeight,
                targetWeightKg = state.targetWeightKg.toFloatOrNull() ?: DefaultHealthGoals.TARGET_WEIGHT_KG,
                baselineShoulderCm = DefaultHealthGoals.BASELINE_SHOULDER_CM,
                baselineWaistCm = DefaultHealthGoals.BASELINE_WAIST_CM,
                baselineHipCm = DefaultHealthGoals.BASELINE_HIP_CM,
            )

            val measurement = BodyMeasurementEntry(
                date = LocalDate.now(),
                weightKg = currentWeight,
                shoulderCm = DefaultHealthGoals.BASELINE_SHOULDER_CM,
                waistCm = DefaultHealthGoals.BASELINE_WAIST_CM,
                hipCm = DefaultHealthGoals.BASELINE_HIP_CM,
            )

            val supplements = if (state.selectedTrackingAreas.contains(DashboardCardType.SUPPLEMENTS)) {
                listOf("D3 Vitamini", "Omega 3", "Multivitamin")
            } else {
                emptyList()
            }

            settingsRepository.completeOnboarding(
                profile = profile,
                goals = goals,
                initialMeasurement = measurement,
                supplements = supplements,
            )

            val dashboardConfig = buildDashboardConfigFromTrackingAreas(state.selectedTrackingAreas)
            dashboardConfig.forEach { config ->
                settingsRepository.updateDashboardCardVisibility(config.type, config.isVisible)
            }

            settingsRepository.updateWaterReminderSettings(
                WaterReminderSettings(
                    enabled = state.waterReminderEnabled,
                    startTime = state.waterReminderStartTime.toLocalTimeOrNull() ?: DefaultHealthGoals.WATER_REMINDER_START_TIME,
                    endTime = state.waterReminderEndTime.toLocalTimeOrNull() ?: DefaultHealthGoals.WATER_REMINDER_END_TIME,
                    intervalMinutes = state.waterReminderIntervalMinutes.toIntOrNull() ?: DefaultHealthGoals.WATER_REMINDER_INTERVAL_MINUTES,
                )
            )

            settingsRepository.updateStepTrackingEnabled(state.stepTrackingPreferred)
            
            _uiState.update { it.copy(isSaving = false) }
        }
    }

    private fun generateSmartGoals() {
        val state = _uiState.value
        val weight = state.currentWeightKg.toFloatOrNull()
        val age = state.age.toIntOrNull()
        val height = state.heightCm.toFloatOrNull()
        
        val water = suggestWaterTargetMl(weight, state.activityLevel)
        val bedtime = DefaultHealthGoals.SLEEP_BEDTIME
        val wakeTime = DefaultHealthGoals.SLEEP_WAKE_TIME
        val caffeineCutoff = suggestCaffeineCutoffTime(bedtime)
        val calories = suggestCalories(age, state.sex, height, weight, state.activityLevel, state.mainGoal)
        val protein = suggestProteinGrams(weight)
        val fat = suggestFatGrams(calories)
        val carbs = suggestCarbGrams(calories, protein, fat)
        
        val dailySteps = when (state.activityLevel) {
            OnboardingActivityLevel.LOW -> 6000
            OnboardingActivityLevel.LIGHT -> 8000
            OnboardingActivityLevel.MODERATE -> 9000
            OnboardingActivityLevel.HIGH -> 10000
        }

        _uiState.update { 
            it.copy(
                waterTargetMl = water.toString(),
                sleepBedtime = bedtime.toString(),
                sleepWakeTime = wakeTime.toString(),
                dailyCaffeineLimitMg = DefaultHealthGoals.DAILY_CAFFEINE_LIMIT_MG.toString(),
                caffeineCutoffTime = caffeineCutoff.toString(),
                dailyCaloriesTarget = calories.toString(),
                proteinTargetGrams = protein.toString(),
                carbTargetGrams = carbs.toString(),
                fatTargetGrams = fat.toString(),
                exerciseDaysPerWeek = DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK.toString(),
                exerciseDurationMinutes = DefaultHealthGoals.EXERCISE_DURATION_MINUTES.toString(),
                smokeDailyLimit = DefaultHealthGoals.SMOKE_DAILY_LIMIT.toString(),
                dailyStepTarget = dailySteps.toString(),
            )
        }
    }

    private fun populatePreferencesDefaults() {
        _uiState.update {
            it.copy(
                waterReminderEnabled = false,
                waterReminderStartTime = DefaultHealthGoals.WATER_REMINDER_START_TIME.toString(),
                waterReminderEndTime = DefaultHealthGoals.WATER_REMINDER_END_TIME.toString(),
                waterReminderIntervalMinutes = DefaultHealthGoals.WATER_REMINDER_INTERVAL_MINUTES.toString(),
                stepTrackingPreferred = false,
            )
        }
    }

    private fun validateCurrentStep(state: OnboardingUiState): Map<String, UiText> {
        val errors = mutableMapOf<String, UiText>()
        
        when (state.currentStep) {
            OnboardingStep.TRACKING_AREAS -> {
                if (state.selectedTrackingAreas.isEmpty()) {
                    errors["tracking_areas"] = UiText.DynamicString("En az bir takip alanı seçmelisin.")
                }
            }
            OnboardingStep.BASIC_INFO -> {
                val age = state.age.toIntOrNull()
                if (state.age.isNotBlank() && (age == null || age !in 13..100)) {
                    errors["age"] = UiText.DynamicString("Yaş 13 ile 100 arasında olmalı.")
                }
                val height = state.heightCm.toFloatOrNull()
                if (state.heightCm.isNotBlank() && (height == null || height !in 100f..230f)) {
                    errors["height"] = UiText.DynamicString("Boy 100 ile 230 cm arasında olmalı.")
                }
                val weight = state.currentWeightKg.toFloatOrNull()
                if (state.currentWeightKg.isNotBlank() && (weight == null || weight !in 30f..250f)) {
                    errors["currentWeight"] = UiText.DynamicString("Kilo 30 ile 250 kg arasında olmalı.")
                }
                val targetWeight = state.targetWeightKg.toFloatOrNull()
                if (state.targetWeightKg.isNotBlank() && (targetWeight == null || targetWeight !in 30f..250f)) {
                    errors["targetWeight"] = UiText.DynamicString("Kilo 30 ile 250 kg arasında olmalı.")
                }
            }
            OnboardingStep.SMART_GOALS -> {
                if (state.selectedTrackingAreas.contains(DashboardCardType.HYDRATION)) {
                    val water = state.waterTargetMl.toIntOrNull()
                    if (water == null || water <= 0) errors["water"] = UiText.DynamicString("Su hedefi 0’dan büyük olmalı.")
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.NUTRITION)) {
                    val cal = state.dailyCaloriesTarget.toIntOrNull()
                    if (cal == null || cal <= 0) errors["calories"] = UiText.DynamicString("Kalori hedefi 0’dan büyük olmalı.")
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.EXERCISE)) {
                    val days = state.exerciseDaysPerWeek.toIntOrNull()
                    if (days == null || days !in 0..7) errors["exercise_days"] = UiText.DynamicString("Egzersiz günü 0 ile 7 arasında olmalı.")
                }
                // Add more as needed, this matches requirements
            }
            OnboardingStep.PREFERENCES -> {
                if (state.selectedTrackingAreas.contains(DashboardCardType.HYDRATION) && state.waterReminderEnabled) {
                    val interval = state.waterReminderIntervalMinutes.toIntOrNull()
                    if (interval == null || interval < DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES) {
                        errors["reminder_interval"] = UiText.DynamicString("Hatırlatıcı aralığı en az ${DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES} dakika olmalı.")
                    }
                }
            }
            else -> {}
        }
        
        return errors
    }

    private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()
}
