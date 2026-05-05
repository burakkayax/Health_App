package com.burak.healthapp.feature.today.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.model.nutrition.CustomFood
import com.burak.healthapp.domain.repository.CustomFoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CustomFoodEditorViewModel @Inject constructor(
    private val repository: CustomFoodRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CustomFoodEditorState())
    val state = _state.asStateFlow()

    /** Reset state for a fresh "add" form. */
    fun resetForAdd() {
        _state.value = CustomFoodEditorState()
    }

    fun loadFood(id: Long) {
        viewModelScope.launch {
            val food = repository.getById(id) ?: return@launch
            _state.value = CustomFoodEditorState(
                id = food.id,
                name = food.name,
                brand = food.brand ?: "",
                servingName = food.servingName,
                servingGrams = food.servingGrams.toString(),
                calories = food.calories.toString(),
                protein = food.proteinGrams.toString(),
                carbs = food.carbsGrams.toString(),
                fat = food.fatGrams.toString(),
                preservedFiberGrams = food.fiberGrams,
                preservedSugarGrams = food.sugarGrams,
                preservedSodiumMg = food.sodiumMg,
                isFavorite = food.isFavorite,
            )
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null, submitError = null) }
    fun onBrandChange(value: String) = _state.update { it.copy(brand = value, submitError = null) }
    fun onServingNameChange(value: String) = _state.update { it.copy(servingName = value, submitError = null) }
    fun onServingGramsChange(value: String) = _state.update { it.copy(servingGrams = value, servingError = null, submitError = null) }
    fun onCaloriesChange(value: String) = _state.update { it.copy(calories = value, caloriesError = null, submitError = null) }
    fun onProteinChange(value: String) = _state.update { it.copy(protein = value, proteinError = null, submitError = null) }
    fun onCarbsChange(value: String) = _state.update { it.copy(carbs = value, carbsError = null, submitError = null) }
    fun onFatChange(value: String) = _state.update { it.copy(fat = value, fatError = null, submitError = null) }
    fun onFavoriteChange(value: Boolean) = _state.update { it.copy(isFavorite = value) }

    fun save(onSuccess: () -> Unit) {
        val current = _state.value
        if (current.isSaving) return

        val nameErr = if (current.name.isBlank()) CustomFoodFieldError.NAME_REQUIRED else null
        val servingErr = validateRequiredPositiveFloat(current.servingGrams)
        val caloriesErr = validateRequiredNonNegativeInt(current.calories)
        val proteinErr = validateOptionalNonNegativeInt(current.protein)
        val carbsErr = validateOptionalNonNegativeInt(current.carbs)
        val fatErr = validateOptionalNonNegativeInt(current.fat)

        if (nameErr != null ||
            servingErr != null ||
            caloriesErr != null ||
            proteinErr != null ||
            carbsErr != null ||
            fatErr != null
        ) {
            _state.update {
                it.copy(
                    nameError = nameErr,
                    servingError = servingErr,
                    caloriesError = caloriesErr,
                    proteinError = proteinErr,
                    carbsError = carbsErr,
                    fatError = fatErr,
                )
            }
            return
        }

        _state.update { it.copy(isSaving = true, submitError = null) }

        viewModelScope.launch {
            try {
                val food = CustomFood(
                    id = current.id ?: 0L,
                    name = current.name.trim(),
                    brand = current.brand.trim().ifBlank { null },
                    servingName = current.servingName.trim().ifBlank { "Porsiyon" },
                    servingGrams = parseDecimalInput(current.servingGrams)!!,
                    calories = parseDecimalInput(current.calories)!!.roundToInt(),
                    proteinGrams = parseMacroOrZero(current.protein),
                    carbsGrams = parseMacroOrZero(current.carbs),
                    fatGrams = parseMacroOrZero(current.fat),
                    fiberGrams = current.preservedFiberGrams,
                    sugarGrams = current.preservedSugarGrams,
                    sodiumMg = current.preservedSodiumMg,
                    isFavorite = current.isFavorite,
                )
                repository.save(food)
                onSuccess()
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                _state.update { it.copy(isSaving = false, submitError = CustomFoodSubmitError.SAVE_FAILED) }
            }
        }
    }

    fun delete(onSuccess: () -> Unit) {
        val id = _state.value.id ?: return
        if (_state.value.isDeleting) return

        _state.update { it.copy(isDeleting = true, submitError = null) }

        viewModelScope.launch {
            try {
                repository.delete(id)
                onSuccess()
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                _state.update { it.copy(isDeleting = false, submitError = CustomFoodSubmitError.DELETE_FAILED) }
            }
        }
    }

    /**
     * Parse a macro value: blank → 0, valid decimal → roundToInt, already validated non-negative.
     */
    private fun parseMacroOrZero(value: String): Int = if (value.isBlank()) 0 else parseDecimalInput(value)!!.roundToInt()
}
