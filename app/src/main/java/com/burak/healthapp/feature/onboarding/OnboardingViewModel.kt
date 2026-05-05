package com.burak.healthapp.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.R
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
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(restoreState() ?: OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.collect { state ->
                saveState(state)
            }
        }
    }

    private fun restoreState(): OnboardingUiState? {
        val currentStepName = savedStateHandle.get<String>("currentStep") ?: return null
        return OnboardingUiState(
            currentStep = OnboardingStep.valueOf(currentStepName),
            selectedTrackingAreas = savedStateHandle.get<List<String>>("selectedTrackingAreas")?.map { DashboardCardType.valueOf(it) }?.toSet() ?: defaultOnboardingTrackingAreas(),
            name = savedStateHandle.get<String>("name") ?: "",
            age = savedStateHandle.get<String>("age") ?: "",
            sex = savedStateHandle.get<String>("sex")?.let { OnboardingSex.valueOf(it) } ?: OnboardingSex.UNSPECIFIED,
            heightCm = savedStateHandle.get<String>("heightCm") ?: "",
            currentWeightKg = savedStateHandle.get<String>("currentWeightKg") ?: "",
            targetWeightKg = savedStateHandle.get<String>("targetWeightKg") ?: "",
            activityLevel = savedStateHandle.get<String>("activityLevel")?.let { OnboardingActivityLevel.valueOf(it) } ?: OnboardingActivityLevel.LIGHT,
            mainGoal = savedStateHandle.get<String>("mainGoal")?.let { OnboardingMainGoal.valueOf(it) } ?: OnboardingMainGoal.MAINTAIN,
            waterTargetMl = savedStateHandle.get<String>("waterTargetMl") ?: "",
            dailyStepTarget = savedStateHandle.get<String>("dailyStepTarget") ?: "",
            sleepBedtime = savedStateHandle.get<String>("sleepBedtime") ?: "",
            sleepWakeTime = savedStateHandle.get<String>("sleepWakeTime") ?: "",
            dailyCaffeineLimitMg = savedStateHandle.get<String>("dailyCaffeineLimitMg") ?: "",
            caffeineCutoffTime = savedStateHandle.get<String>("caffeineCutoffTime") ?: "",
            dailyCaloriesTarget = savedStateHandle.get<String>("dailyCaloriesTarget") ?: "",
            proteinTargetGrams = savedStateHandle.get<String>("proteinTargetGrams") ?: "",
            carbTargetGrams = savedStateHandle.get<String>("carbTargetGrams") ?: "",
            fatTargetGrams = savedStateHandle.get<String>("fatTargetGrams") ?: "",
            exerciseDaysPerWeek = savedStateHandle.get<String>("exerciseDaysPerWeek") ?: "",
            exerciseDurationMinutes = savedStateHandle.get<String>("exerciseDurationMinutes") ?: "",
            smokeDailyLimit = savedStateHandle.get<String>("smokeDailyLimit") ?: "",
            waterReminderEnabled = savedStateHandle.get<Boolean>("waterReminderEnabled") ?: false,
            waterReminderStartTime = savedStateHandle.get<String>("waterReminderStartTime") ?: "",
            waterReminderEndTime = savedStateHandle.get<String>("waterReminderEndTime") ?: "",
            waterReminderIntervalMinutes = savedStateHandle.get<String>("waterReminderIntervalMinutes") ?: "",
            stepTrackingPreferred = savedStateHandle.get<Boolean>("stepTrackingPreferred") ?: false,
        )
    }

    private fun saveState(state: OnboardingUiState) {
        savedStateHandle["currentStep"] = state.currentStep.name
        savedStateHandle["selectedTrackingAreas"] = state.selectedTrackingAreas.map { it.name }
        savedStateHandle["name"] = state.name
        savedStateHandle["age"] = state.age
        savedStateHandle["sex"] = state.sex.name
        savedStateHandle["heightCm"] = state.heightCm
        savedStateHandle["currentWeightKg"] = state.currentWeightKg
        savedStateHandle["targetWeightKg"] = state.targetWeightKg
        savedStateHandle["activityLevel"] = state.activityLevel.name
        savedStateHandle["mainGoal"] = state.mainGoal.name
        savedStateHandle["waterTargetMl"] = state.waterTargetMl
        savedStateHandle["dailyStepTarget"] = state.dailyStepTarget
        savedStateHandle["sleepBedtime"] = state.sleepBedtime
        savedStateHandle["sleepWakeTime"] = state.sleepWakeTime
        savedStateHandle["dailyCaffeineLimitMg"] = state.dailyCaffeineLimitMg
        savedStateHandle["caffeineCutoffTime"] = state.caffeineCutoffTime
        savedStateHandle["dailyCaloriesTarget"] = state.dailyCaloriesTarget
        savedStateHandle["proteinTargetGrams"] = state.proteinTargetGrams
        savedStateHandle["carbTargetGrams"] = state.carbTargetGrams
        savedStateHandle["fatTargetGrams"] = state.fatTargetGrams
        savedStateHandle["exerciseDaysPerWeek"] = state.exerciseDaysPerWeek
        savedStateHandle["exerciseDurationMinutes"] = state.exerciseDurationMinutes
        savedStateHandle["smokeDailyLimit"] = state.smokeDailyLimit
        savedStateHandle["waterReminderEnabled"] = state.waterReminderEnabled
        savedStateHandle["waterReminderStartTime"] = state.waterReminderStartTime
        savedStateHandle["waterReminderEndTime"] = state.waterReminderEndTime
        savedStateHandle["waterReminderIntervalMinutes"] = state.waterReminderIntervalMinutes
        savedStateHandle["stepTrackingPreferred"] = state.stepTrackingPreferred
    }

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
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
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
                    useDefaultSupplementsWhenEmpty = false,
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
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = UiText.StringResource(R.string.onboarding_error_save_failed)) }
            }
        }
    }

    private fun finishOnboarding() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            try {
                val state = _uiState.value

                val profileName = state.name.trim().ifBlank { "Misafir" }
                val profile = UserProfile.fromName(profileName, heightCm = parseLocalizedDecimalInput(state.heightCm))

                val currentWeight = parseLocalizedDecimalInput(state.currentWeightKg) ?: DefaultHealthGoals.BASELINE_WEIGHT_KG
                
                val goals = GoalSettings(
                    dailyCaloriesTarget = state.dailyCaloriesTarget.toIntOrNull() ?: DefaultHealthGoals.DAILY_CALORIES,
                    proteinTargetGrams = state.proteinTargetGrams.toIntOrNull() ?: DefaultHealthGoals.PROTEIN_GRAMS,
                    carbTargetGrams = state.carbTargetGrams.toIntOrNull() ?: DefaultHealthGoals.CARB_GRAMS,
                    fatTargetGrams = state.fatTargetGrams.toIntOrNull() ?: DefaultHealthGoals.FAT_GRAMS,
                    waterTargetMl = state.waterTargetMl.toIntOrNull() ?: DefaultHealthGoals.WATER_TARGET_ML,
                    dailyStepTarget = state.dailyStepTarget.toIntOrNull() ?: DefaultHealthGoals.DAILY_STEPS,
                    dailyCaffeineLimitMg = state.dailyCaffeineLimitMg.toIntOrNull() ?: DefaultHealthGoals.DAILY_CAFFEINE_LIMIT_MG,
                    caffeineCutoffTime = state.caffeineCutoffTime.toLocalTimeOrNull() ?: DefaultHealthGoals.CAFFEINE_CUTOFF_TIME,
                    caffeineSleepBufferHours = DefaultHealthGoals.CAFFEINE_SLEEP_BUFFER_HOURS,
                    sleepTargetBedtime = state.sleepBedtime.toLocalTimeOrNull() ?: DefaultHealthGoals.SLEEP_BEDTIME,
                    sleepTargetWakeTime = state.sleepWakeTime.toLocalTimeOrNull() ?: DefaultHealthGoals.SLEEP_WAKE_TIME,
                    exerciseTargetDaysPerWeek = state.exerciseDaysPerWeek.toIntOrNull() ?: DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK,
                    exerciseTargetDurationMinutes = state.exerciseDurationMinutes.toIntOrNull() ?: DefaultHealthGoals.EXERCISE_DURATION_MINUTES,
                    smokeDailyLimit = state.smokeDailyLimit.toIntOrNull() ?: DefaultHealthGoals.SMOKE_DAILY_LIMIT,
                    baselineWeightKg = currentWeight,
                    targetWeightKg = parseLocalizedDecimalInput(state.targetWeightKg) ?: DefaultHealthGoals.TARGET_WEIGHT_KG,
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

                val useSupplements = state.selectedTrackingAreas.contains(DashboardCardType.SUPPLEMENTS)
                val supplements = if (useSupplements) {
                    listOf("D3 Vitamini", "Omega 3", "Multivitamin")
                } else {
                    emptyList()
                }

                settingsRepository.completeOnboarding(
                    profile = profile,
                    goals = goals,
                    initialMeasurement = measurement,
                    supplements = supplements,
                    useDefaultSupplementsWhenEmpty = useSupplements,
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

                settingsRepository.updateStepTrackingEnabled(false) // Safe mode, intentional
                
                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = UiText.StringResource(R.string.onboarding_error_save_failed)) }
            }
        }
    }

    private fun generateSmartGoals() {
        val state = _uiState.value
        val weight = parseLocalizedDecimalInput(state.currentWeightKg)
        val age = state.age.toIntOrNull()
        val height = parseLocalizedDecimalInput(state.heightCm)
        
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
                    errors["tracking_areas"] = UiText.StringResource(R.string.onboarding_error_select_one_tracking_area)
                }
            }
            OnboardingStep.BASIC_INFO -> {
                val age = state.age.toIntOrNull()
                if (state.age.isNotBlank() && (age == null || age !in 13..100)) {
                    errors["age"] = UiText.StringResource(R.string.onboarding_error_range_age)
                }
                val height = parseLocalizedDecimalInput(state.heightCm)
                if (state.heightCm.isNotBlank() && (height == null || height !in 100f..230f)) {
                    errors["height"] = UiText.StringResource(R.string.onboarding_error_range_height)
                }
                val weight = parseLocalizedDecimalInput(state.currentWeightKg)
                if (state.currentWeightKg.isNotBlank() && (weight == null || weight !in 30f..250f)) {
                    errors["currentWeight"] = UiText.StringResource(R.string.onboarding_error_range_weight)
                }
                val targetWeight = parseLocalizedDecimalInput(state.targetWeightKg)
                if (state.targetWeightKg.isNotBlank() && (targetWeight == null || targetWeight !in 30f..250f)) {
                    errors["targetWeight"] = UiText.StringResource(R.string.onboarding_error_range_weight)
                }
            }
            OnboardingStep.SMART_GOALS -> {
                if (state.selectedTrackingAreas.contains(DashboardCardType.HYDRATION)) {
                    val water = state.waterTargetMl.toIntOrNull()
                    if (water == null || water <= 0) errors["water"] = UiText.StringResource(R.string.onboarding_error_water_positive)
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.STEPS)) {
                    val steps = state.dailyStepTarget.toIntOrNull()
                    if (steps == null || steps < 0 || steps > 100000) errors["steps"] = UiText.StringResource(R.string.onboarding_error_step_target_invalid)
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.SLEEP)) {
                    val bedtime = state.sleepBedtime.toLocalTimeOrNull()
                    if (bedtime == null) errors["sleepBedtime"] = UiText.StringResource(R.string.onboarding_error_invalid_time)
                    val wakeTime = state.sleepWakeTime.toLocalTimeOrNull()
                    if (wakeTime == null) errors["sleepWakeTime"] = UiText.StringResource(R.string.onboarding_error_invalid_time)
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.CAFFEINE)) {
                    val caffeineLimit = state.dailyCaffeineLimitMg.toIntOrNull()
                    if (caffeineLimit == null || caffeineLimit < 0) errors["caffeineLimit"] = UiText.StringResource(R.string.onboarding_error_caffeine_limit_invalid)
                    val caffeineCutoff = state.caffeineCutoffTime.toLocalTimeOrNull()
                    if (caffeineCutoff == null) errors["caffeineCutoff"] = UiText.StringResource(R.string.onboarding_error_invalid_time)
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.NUTRITION)) {
                    val cal = state.dailyCaloriesTarget.toIntOrNull()
                    if (cal == null || cal <= 0) errors["calories"] = UiText.StringResource(R.string.onboarding_error_calorie_positive)
                    
                    val protein = state.proteinTargetGrams.toIntOrNull()
                    if (protein == null || protein < 0) errors["protein"] = UiText.StringResource(R.string.onboarding_error_macro_not_negative)
                    
                    val carb = state.carbTargetGrams.toIntOrNull()
                    if (carb == null || carb < 0) errors["carb"] = UiText.StringResource(R.string.onboarding_error_macro_not_negative)
                    
                    val fat = state.fatTargetGrams.toIntOrNull()
                    if (fat == null || fat < 0) errors["fat"] = UiText.StringResource(R.string.onboarding_error_macro_not_negative)
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.EXERCISE)) {
                    val days = state.exerciseDaysPerWeek.toIntOrNull()
                    if (days == null || days !in 0..7) errors["exerciseDays"] = UiText.StringResource(R.string.onboarding_error_exercise_days)
                    
                    val duration = state.exerciseDurationMinutes.toIntOrNull()
                    if (duration == null || duration < 0) errors["exerciseDuration"] = UiText.StringResource(R.string.onboarding_error_exercise_duration)
                }
                if (state.selectedTrackingAreas.contains(DashboardCardType.SMOKING)) {
                    val smoke = state.smokeDailyLimit.toIntOrNull()
                    if (smoke == null || smoke < 0) errors["smokeLimit"] = UiText.StringResource(R.string.onboarding_error_smoke_limit)
                }
            }
            OnboardingStep.PREFERENCES -> {
                if (state.selectedTrackingAreas.contains(DashboardCardType.HYDRATION) && state.waterReminderEnabled) {
                    val startTime = state.waterReminderStartTime.toLocalTimeOrNull()
                    if (startTime == null) errors["reminder_start_time"] = UiText.StringResource(R.string.onboarding_error_invalid_time)
                    val endTime = state.waterReminderEndTime.toLocalTimeOrNull()
                    if (endTime == null) errors["reminder_end_time"] = UiText.StringResource(R.string.onboarding_error_invalid_time)

                    val interval = state.waterReminderIntervalMinutes.toIntOrNull()
                    if (interval == null || interval < DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES) {
                        errors["reminder_interval"] = UiText.StringResource(R.string.onboarding_error_reminder_interval)
                    }
                }
            }
            else -> {}
        }
        
        return errors
    }

    private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this) }.getOrNull()
}
