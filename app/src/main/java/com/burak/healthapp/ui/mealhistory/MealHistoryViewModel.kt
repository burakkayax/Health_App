package com.burak.healthapp.ui.mealhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.data.repository.DashboardRepository
import com.burak.healthapp.domain.calculation.groupMealsByType
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.ui.model.MealHistoryEntryState
import com.burak.healthapp.ui.model.MealHistorySectionState
import com.burak.healthapp.ui.model.MealHistoryUiState
import com.burak.healthapp.ui.root.healthApplication
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MealHistoryViewModel(
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = selectedDate
        .flatMapLatest { date ->
            dashboardRepository.observeMealsForDate(date).map(::toMealHistoryUiState)
        }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MealHistoryViewModel(
                    dashboardRepository = healthApplication().container.dashboardRepository,
                )
            }
        }
    }
}

internal fun toMealHistoryUiState(entries: List<MealEntry>): MealHistoryUiState {
    return MealHistoryUiState(
        sections = groupMealsByType(entries).map { groupedMeal ->
            MealHistorySectionState(
                title = groupedMeal.mealType.label,
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
}
