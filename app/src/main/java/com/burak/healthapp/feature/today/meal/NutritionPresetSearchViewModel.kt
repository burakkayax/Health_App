package com.burak.healthapp.feature.today.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
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

data class NutritionPresetSearchUiState(
    val query: String = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val results: List<NutritionPresetFood> = emptyList(),
    val isLoading: Boolean = true,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class NutritionPresetSearchViewModel @Inject constructor(
    private val repository: NutritionPresetRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val categories = MutableStateFlow<List<String>>(emptyList())
    private val results = MutableStateFlow<List<NutritionPresetFood>>(emptyList())
    private val isLoading = MutableStateFlow(true)

    val uiState = combine(query, selectedCategory, categories, results, isLoading) { query, category, categories, results, loading ->
        NutritionPresetSearchUiState(
            query = query,
            selectedCategory = category,
            categories = categories,
            results = results,
            isLoading = loading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = NutritionPresetSearchUiState(),
    )

    init {
        viewModelScope.launch {
            categories.value = repository.getCategories()
        }
        viewModelScope.launch {
            combine(query.debounce(150), selectedCategory) { query, category -> query to category }
                .collect { (query, category) ->
                    isLoading.value = true
                    results.value = repository.searchPresets(query = query, category = category)
                    isLoading.value = false
                }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategoryChange(value: String?) {
        selectedCategory.value = value
    }
}
