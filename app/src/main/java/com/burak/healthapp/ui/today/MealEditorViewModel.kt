package com.burak.healthapp.ui.today

import androidx.lifecycle.ViewModel
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.ui.model.MealDraftFoodState
import com.burak.healthapp.ui.model.MealEditorUiState
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
            state.copy(
                draftFoods = updatedDrafts,
                canSave = updatedDrafts.any { it.name.isNotBlank() },
            )
        }
    }

    private fun updateState(transform: (MealEditorUiState) -> MealEditorUiState) {
        _uiState.value = transform(_uiState.value)
    }

    private fun createDraft(): MealDraftFoodState {
        return MealDraftFoodState(draftId = nextDraftId++)
    }
}
