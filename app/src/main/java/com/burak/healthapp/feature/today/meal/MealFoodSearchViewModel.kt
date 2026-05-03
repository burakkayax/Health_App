package com.burak.healthapp.feature.today.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.model.nutrition.CustomFood
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import com.burak.healthapp.domain.repository.CustomFoodRepository
import com.burak.healthapp.domain.repository.NutritionPresetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FoodSearchSourceFilter { ALL, PRESETS, CUSTOM }

data class MealFoodSearchUiState(
    val query: String = "",
    val selectedCategory: String? = null,
    val selectedSource: FoodSearchSourceFilter = FoodSearchSourceFilter.ALL,
    val categories: List<String> = emptyList(),
    val presetResults: List<NutritionPresetFood> = emptyList(),
    val customResults: List<CustomFood> = emptyList(),
    val isLoading: Boolean = true,
    val isError: Boolean = false,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class MealFoodSearchViewModel @Inject constructor(
    private val presetRepository: NutritionPresetRepository,
    private val customFoodRepository: CustomFoodRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val selectedSource = MutableStateFlow(FoodSearchSourceFilter.ALL)
    private val categories = MutableStateFlow<List<String>>(emptyList())
    private val presetResults = MutableStateFlow<List<NutritionPresetFood>>(emptyList())
    private val customResults = MutableStateFlow<List<CustomFood>>(emptyList())
    private val isLoading = MutableStateFlow(true)
    private val isError = MutableStateFlow(false)

    private data class SearchInputs(
        val query: String,
        val selectedCategory: String?,
        val selectedSource: FoodSearchSourceFilter,
        val categories: List<String>,
        val presetResults: List<NutritionPresetFood>,
        val customResults: List<CustomFood>,
    )

    val uiState = combine(
        combine(
            query,
            selectedCategory,
            selectedSource,
            categories,
        ) { q, cat, src, cats -> SearchInputs(q, cat, src, cats, emptyList(), emptyList()) },
        presetResults,
        customResults,
        isLoading,
        isError,
    ) { inputs, presets, custom, loading, error ->
        MealFoodSearchUiState(
            query = inputs.query,
            selectedCategory = inputs.selectedCategory,
            selectedSource = inputs.selectedSource,
            categories = inputs.categories,
            presetResults = presets,
            customResults = custom,
            isLoading = loading,
            isError = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MealFoodSearchUiState(),
    )

    init {
        viewModelScope.launch {
            try {
                categories.value = presetRepository.getCategories()
            } catch (e: Exception) {
                isError.value = true
            }
        }
        viewModelScope.launch {
            combine(
                query.debounce(150),
                selectedCategory,
                selectedSource,
            ) { q, cat, src -> Triple(q, cat, src) }
                .collect { (q, cat, src) ->
                    isLoading.value = true
                    try {
                        if (src != FoodSearchSourceFilter.CUSTOM) {
                            presetResults.value = presetRepository.searchPresets(query = q, category = cat)
                        } else {
                            presetResults.value = emptyList()
                        }
                        if (src != FoodSearchSourceFilter.PRESETS) {
                            customResults.value = customFoodRepository.searchCustomFoods(q)
                        } else {
                            customResults.value = emptyList()
                        }
                        isError.value = false
                    } catch (e: Exception) {
                        isError.value = true
                    } finally {
                        isLoading.value = false
                    }
                }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategoryChange(value: String?) {
        selectedCategory.value = value
    }

    fun onSourceFilterChange(value: FoodSearchSourceFilter) {
        selectedSource.value = value
    }
}
