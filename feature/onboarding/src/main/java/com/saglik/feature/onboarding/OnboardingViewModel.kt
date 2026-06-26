package com.saglik.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.domain.onboarding.CompleteOnboardingInput
import com.saglik.domain.onboarding.OnboardingValidator
import com.saglik.domain.usecase.CompleteOnboardingResult
import com.saglik.domain.usecase.CompleteOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<Unit>()
    val navigationEvents: SharedFlow<Unit> = _navigationEvents.asSharedFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            OnboardingEvent.NextClicked -> moveToNextStep()
            OnboardingEvent.BackClicked -> moveToPreviousStep()
            is OnboardingEvent.SexSelected -> updateState {
                it.copy(sex = event.sex, errorMessage = null)
            }
            is OnboardingEvent.AgeChanged -> updateNumericState(
                value = event.value.cleanInteger(maxLength = 3),
                apply = { state, value -> state.copy(age = value) },
                validate = { OnboardingValidator.validateAge(it.toIntOrNull()) },
            )
            is OnboardingEvent.HeightChanged -> updateNumericState(
                value = event.value.cleanDecimal(maxLength = 5),
                apply = { state, value -> state.copy(heightCm = value) },
                validate = { OnboardingValidator.validateHeightCm(it.toFloatOrNull()) },
            )
            is OnboardingEvent.StartingWeightChanged -> updateNumericState(
                value = event.value.cleanDecimal(maxLength = 5),
                apply = { state, value -> state.copy(startingWeightKg = value) },
                validate = { OnboardingValidator.validateStartingWeightKg(it.toFloatOrNull()) },
            )
            is OnboardingEvent.GoalSelected -> updateState {
                it.copy(goal = event.goal, errorMessage = null)
            }
            OnboardingEvent.CompleteClicked -> completeOnboarding()
        }
    }

    private fun moveToNextStep() {
        val state = _uiState.value
        val error = validateCurrentStep(state)
        if (error != null) {
            updateState { it.copy(errorMessage = error) }
            return
        }

        val nextStep = state.currentStep.nextOrNull() ?: state.currentStep
        updateState { it.copy(currentStep = nextStep, errorMessage = null) }
    }

    private fun moveToPreviousStep() {
        val state = _uiState.value
        val previousStep = state.currentStep.previousOrNull() ?: return
        updateState { it.copy(currentStep = previousStep, errorMessage = null) }
    }

    private fun completeOnboarding() {
        val state = _uiState.value
        if (state.isSaving) return

        val validation = OnboardingValidator.validateForCompletion(state.toInput())
        if (!validation.isValid) {
            updateState { it.copy(errorMessage = validation.firstMessage) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null).withNextEnabled() }
            val result = runCatching { completeOnboardingUseCase(state.toInput()) }
                .getOrElse {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Something went wrong. Please try again.",
                        ).withNextEnabled()
                    }
                    return@launch
                }

            when (result) {
                CompleteOnboardingResult.Success -> _navigationEvents.emit(Unit)
                is CompleteOnboardingResult.ValidationError -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.errors.firstMessage,
                        ).withNextEnabled()
                    }
                }
            }
        }
    }

    private fun updateState(transform: (OnboardingUiState) -> OnboardingUiState) {
        _uiState.update { transform(it).withNextEnabled() }
    }

    private fun updateNumericState(
        value: String,
        apply: (OnboardingUiState, String) -> OnboardingUiState,
        validate: (String) -> String?,
    ) {
        updateState { state ->
            val error = if (value.isBlank()) null else validate(value)
            apply(state, value).copy(errorMessage = error)
        }
    }

    private fun OnboardingUiState.withNextEnabled(): OnboardingUiState =
        copy(isNextEnabled = !isSaving && validateCurrentStep(this) == null)

    private fun validateCurrentStep(state: OnboardingUiState): String? =
        when (state.currentStep) {
            OnboardingStep.WELCOME -> null
            OnboardingStep.SEX -> OnboardingValidator.validateSex(state.sex)
            OnboardingStep.AGE -> OnboardingValidator.validateAge(state.age.toIntOrNull())
            OnboardingStep.HEIGHT -> OnboardingValidator.validateHeightCm(state.heightCm.toFloatOrNull())
            OnboardingStep.WEIGHT -> {
                OnboardingValidator.validateStartingWeightKg(state.startingWeightKg.toFloatOrNull())
            }
            OnboardingStep.GOAL -> OnboardingValidator.validateGoal(state.goal)
            OnboardingStep.REVIEW -> OnboardingValidator.validateForCompletion(state.toInput()).firstMessage
        }

    private fun OnboardingUiState.toInput(): CompleteOnboardingInput =
        CompleteOnboardingInput(
            sex = sex,
            age = age.toIntOrNull(),
            heightCm = heightCm.toFloatOrNull(),
            startingWeightKg = startingWeightKg.toFloatOrNull(),
            goal = goal,
        )

    private fun OnboardingStep.nextOrNull(): OnboardingStep? =
        OnboardingStep.entries.getOrNull(ordinal + 1)

    private fun OnboardingStep.previousOrNull(): OnboardingStep? =
        OnboardingStep.entries.getOrNull(ordinal - 1)

    private fun String.cleanInteger(maxLength: Int): String =
        filter { it.isDigit() }.take(maxLength)

    private fun String.cleanDecimal(maxLength: Int): String {
        val builder = StringBuilder()
        var hasSeparator = false
        for (char in this) {
            when {
                char.isDigit() -> builder.append(char)
                (char == '.' || char == ',') && !hasSeparator -> {
                    builder.append('.')
                    hasSeparator = true
                }
            }
            if (builder.length >= maxLength) break
        }
        return builder.toString()
    }
}
