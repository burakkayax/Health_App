package com.burak.healthapp.feature.detail.mealhistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.calculation.groupMealsByType
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.feature.detail.mealhistory.MealHistoryEntryState
import com.burak.healthapp.feature.detail.mealhistory.MealHistorySectionState
import com.burak.healthapp.feature.detail.mealhistory.MealHistoryUiState
import com.burak.healthapp.feature.today.meal.labelResId
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

internal fun toMealHistoryUiState(entries: List<MealEntry>): MealHistoryUiState {
    val grouped = groupMealsByType(entries)
    val sections = grouped.map { groupedMeal ->
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
    }
    val dailySummary = if (entries.isEmpty()) {
        null
    } else {
        val totalProtein = entries.sumOf { it.proteinGrams }
        val totalCarbs = entries.sumOf { it.carbsGrams }
        val totalFat = entries.sumOf { it.fatGrams }

        val proteinKcal = totalProtein * 4
        val carbsKcal = totalCarbs * 4
        val fatKcal = totalFat * 9
        val macroKcal = proteinKcal + carbsKcal + fatKcal

        val macroDistribution = if (macroKcal > 0) {
            val proteinPercent = kotlin.math.round((proteinKcal.toFloat() / macroKcal) * 100).toInt()
            val carbsPercent = kotlin.math.round((carbsKcal.toFloat() / macroKcal) * 100).toInt()
            val fatPercent = 100 - proteinPercent - carbsPercent
            MealMacroDistribution(proteinPercent, carbsPercent, fatPercent)
        } else {
            null
        }

        MealHistoryDailySummary(
            totalCalories = entries.sumOf { it.calories },
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            mealCount = grouped.size,
            foodCount = entries.size,
            macroDistribution = macroDistribution,
        )
    }
    return MealHistoryUiState(
        sections = sections,
        dailySummary = dailySummary,
    )
}
