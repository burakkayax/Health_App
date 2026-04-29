package com.burak.healthapp.feature.detail.mealhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.calculation.groupMealsByType
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.feature.detail.mealhistory.MealHistoryEntryState
import com.burak.healthapp.feature.detail.mealhistory.MealHistorySectionState
import com.burak.healthapp.feature.detail.mealhistory.MealHistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MealHistoryViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = selectedDate
        .flatMapLatest { date ->
            dashboardRepository.observeMealsForDate(date)
                .map(::toMealHistoryUiState)
                .flowOn(Dispatchers.Default)
                .distinctUntilChanged()
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MealHistoryUiState(emptyList()),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun deleteMeal(id: Long) {
        viewModelScope.launch {
            dashboardRepository.deleteMealEntry(id)
        }
    }
}

internal fun toMealHistoryUiState(entries: List<MealEntry>): MealHistoryUiState = MealHistoryUiState(
    sections = groupMealsByType(entries).map { groupedMeal ->
        MealHistorySectionState(
            titleResId = groupedMeal.mealType.labelResId,
            entries = groupedMeal.entries.map { mealEntry ->
                MealHistoryEntryState(
                    id = mealEntry.id,
                    mealType = mealEntry.mealType,
                    name = mealEntry.name,
                    calories = mealEntry.calories,
                    proteinGrams = mealEntry.proteinGrams,
                    carbsGrams = mealEntry.carbsGrams,
                    fatGrams = mealEntry.fatGrams,
                )
            },
        )
    },
)
