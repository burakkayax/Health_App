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

@HiltViewModel
class CustomFoodEditorViewModel @Inject constructor(
    private val repository: CustomFoodRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CustomFoodEditorState())
    val state = _state.asStateFlow()

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
                isFavorite = food.isFavorite,
            )
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = null) }
    fun onBrandChange(value: String) = _state.update { it.copy(brand = value) }
    fun onServingNameChange(value: String) = _state.update { it.copy(servingName = value) }
    fun onServingGramsChange(value: String) = _state.update { it.copy(servingGrams = value, servingError = null) }
    fun onCaloriesChange(value: String) = _state.update { it.copy(calories = value, caloriesError = null) }
    fun onProteinChange(value: String) = _state.update { it.copy(protein = value) }
    fun onCarbsChange(value: String) = _state.update { it.copy(carbs = value) }
    fun onFatChange(value: String) = _state.update { it.copy(fat = value) }

    fun save(onSuccess: () -> Unit) {
        val current = _state.value
        val servingGrams = current.servingGrams.replace(',', '.').toFloatOrNull()
        val calories = current.calories.replace(',', '.').toFloatOrNull()?.toInt()

        var hasError = false
        if (current.name.isBlank()) {
            _state.update { it.copy(nameError = "Besin adı gerekli.") }
            hasError = true
        }
        if (servingGrams == null || servingGrams <= 0f) {
            _state.update { it.copy(servingError = "Porsiyon gramı geçerli olmalı.") }
            hasError = true
        }
        if (calories == null || calories < 0) {
            _state.update { it.copy(caloriesError = "Kalori geçerli olmalı.") }
            hasError = true
        }
        if (hasError) return

        viewModelScope.launch {
            val food = CustomFood(
                id = current.id ?: 0L,
                name = current.name.trim(),
                brand = current.brand.trim().ifBlank { null },
                servingName = current.servingName.trim().ifBlank { "Porsiyon" },
                servingGrams = servingGrams!!,
                calories = calories!!,
                proteinGrams = current.protein.toSafeInt(),
                carbsGrams = current.carbs.toSafeInt(),
                fatGrams = current.fat.toSafeInt(),
                isFavorite = current.isFavorite,
            )
            repository.save(food)
            onSuccess()
        }
    }

    fun delete(onSuccess: () -> Unit) {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.delete(id)
            onSuccess()
        }
    }

    private fun String.toSafeInt(): Int = replace(',', '.').toFloatOrNull()?.toInt()?.coerceAtLeast(0) ?: 0
}
