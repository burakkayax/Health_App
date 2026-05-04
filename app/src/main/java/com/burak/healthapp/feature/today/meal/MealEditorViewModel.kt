package com.burak.healthapp.feature.today.meal

import androidx.lifecycle.ViewModel
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.validation.HealthInputError
import com.burak.healthapp.domain.validation.MealInputValidator
import com.burak.healthapp.domain.validation.ValidationResult
import com.burak.healthapp.feature.today.meal.MealDraftFoodState
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MealEditorViewModel : ViewModel() {
    private var nextDraftId = 1L

    private val _uiState = MutableStateFlow(
        MealEditorUiState(
            mealType = MealType.BREAKFAST,
            draftFoods = listOf(createDraft()),
            canSave = false,
        ),
    )
    val uiState: StateFlow<MealEditorUiState> = _uiState.asStateFlow()

    fun setMealType(mealType: MealType) {
        updateState { state -> state.copy(mealType = mealType) }
    }

    fun addDraftFood() {
        updateDraftFoods { drafts -> drafts + createDraft() }
    }

    fun removeDraftFood(draftId: Long) {
        updateDraftFoods { drafts ->
            val remaining = drafts.filterNot { it.draftId == draftId }
            if (remaining.isEmpty()) listOf(createDraft()) else remaining
        }
    }

    fun updateDraftName(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(name = value) }
    }

    fun updateDraftCalories(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(calories = value) }
    }

    fun updateDraftProtein(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(protein = value) }
    }

    fun updateDraftCarbs(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(carbs = value) }
    }

    fun updateDraftFat(draftId: Long, value: String) {
        updateDraft(draftId) { draft -> draft.copy(fat = value) }
    }

    fun reset() {
        _uiState.value = MealEditorUiState(
            mealType = MealType.BREAKFAST,
            draftFoods = listOf(createDraft()),
            canSave = false,
        )
    }

    private fun updateDraft(
        draftId: Long,
        transform: (MealDraftFoodState) -> MealDraftFoodState,
    ) {
        updateDraftFoods { drafts ->
            drafts.map { draft ->
                if (draft.draftId == draftId) {
                    transform(draft)
                } else {
                    draft
                }
            }
        }
    }

    private fun updateDraftFoods(
        transform: (List<MealDraftFoodState>) -> List<MealDraftFoodState>,
    ) {
        updateState { state ->
            val updatedDrafts = transform(state.draftFoods)
            val validatedDrafts = attachValidation(updatedDrafts)
            state.copy(
                draftFoods = validatedDrafts,
                canSave = validatedDrafts.any { it.hasInput() } &&
                    validatedDrafts.none { it.hasValidationError() },
                totalSummary = computeTotalSummary(validatedDrafts),
            )
        }
    }

    private fun updateState(transform: (MealEditorUiState) -> MealEditorUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun createDraft(): MealDraftFoodState = MealDraftFoodState(draftId = nextDraftId++)

    private fun attachValidation(drafts: List<MealDraftFoodState>): List<MealDraftFoodState> = drafts.map { draft ->
        if (!draft.hasInput()) {
            draft.copy(nameError = null, calorieError = null, macroError = null)
        } else {
            when (
                val result = MealInputValidator.validate(
                    name = draft.name,
                    calories = draft.calories,
                    protein = draft.protein,
                    carbs = draft.carbs,
                    fat = draft.fat,
                )
            ) {
                is ValidationResult.Valid -> draft.copy(
                    nameError = null,
                    calorieError = null,
                    macroError = null,
                )
                is ValidationResult.Invalid -> draft.copy(
                    nameError = result.errors.firstOrNull { it == HealthInputError.REQUIRED },
                    calorieError = draft.calories.errorFrom(result.errors),
                    macroError = result.errors.firstOrNull {
                        it != HealthInputError.REQUIRED && draft.calories.isBlank()
                    } ?: result.errors.firstOrNull {
                        it != HealthInputError.REQUIRED && draft.calories.isNotBlank()
                    },
                )
            }
        }
    }
}

private fun MealDraftFoodState.hasInput(): Boolean = listOf(name, calories, protein, carbs, fat).any { it.isNotBlank() }

private fun MealDraftFoodState.hasValidationError(): Boolean = nameError != null || calorieError != null || macroError != null

private fun String.errorFrom(errors: List<HealthInputError>): HealthInputError? {
    if (isBlank()) return null
    return errors.firstOrNull { it != HealthInputError.REQUIRED }
}

private fun computeTotalSummary(drafts: List<MealDraftFoodState>): MealTotalSummary {
    val withInput = drafts.filter { it.name.isNotBlank() }
    return MealTotalSummary(
        totalCalories = withInput.sumOf { it.calories.toIntOrNull() ?: 0 },
        totalProtein = withInput.sumOf { it.protein.toIntOrNull() ?: 0 },
        totalCarbs = withInput.sumOf { it.carbs.toIntOrNull() ?: 0 },
        totalFat = withInput.sumOf { it.fat.toIntOrNull() ?: 0 },
        foodCount = withInput.size,
    )
}
