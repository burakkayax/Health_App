package com.burak.healthapp.feature.today.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.data.nutrition.TurkishSearchNormalizer
import com.burak.healthapp.domain.model.nutrition.CustomFood
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import com.burak.healthapp.domain.repository.CustomFoodRepository
import com.burak.healthapp.domain.repository.NutritionPresetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
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
    private val isLoading = MutableStateFlow(true)
    private val isError = MutableStateFlow(false)

    private data class UiInputs(
        val query: String,
        val category: String?,
        val source: FoodSearchSourceFilter,
        val categories: List<String>,
    )

    /**
     * Custom foods observed reactively from Room.
     * Filtered in-memory with Turkish-aware normalize to support
     * queries like "sut" finding "Süt".
     */
    private val filteredCustomFoods: StateFlow<List<CustomFood>> = combine(
        customFoodRepository.observeAll(),
        query.debounce(150),
        selectedSource,
    ) { allCustom, q, src ->
        if (src == FoodSearchSourceFilter.PRESETS) return@combine emptyList()
        val normalizedQuery = TurkishSearchNormalizer.normalize(q)
        if (normalizedQuery.isBlank()) {
            // Favorites first, then by updatedAt (DAO already sorts this way)
            allCustom
        } else {
            allCustom.filter { food ->
                val nameN = TurkishSearchNormalizer.normalize(food.name)
                val brandN = food.brand?.let { TurkishSearchNormalizer.normalize(it) } ?: ""
                nameN.contains(normalizedQuery) || brandN.contains(normalizedQuery)
            }.sortedWith(
                compareByDescending<CustomFood> { it.isFavorite }
                    .thenBy { TurkishSearchNormalizer.normalize(it.name) },
            )
        }
    }.catch {
        // On error, emit empty list so the UI doesn't break
        emit(emptyList())
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<MealFoodSearchUiState> = combine(
        combine(
            query,
            selectedCategory,
            selectedSource,
            categories,
        ) { q, cat, src, cats ->
            UiInputs(q, cat, src, cats)
        },
        presetResults,
        filteredCustomFoods,
        isLoading,
        isError,
    ) { inputs, presets, custom, loading, error ->
        MealFoodSearchUiState(
            query = inputs.query,
            selectedCategory = inputs.category,
            selectedSource = inputs.source,
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
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
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
                        isError.value = false
                    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
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
