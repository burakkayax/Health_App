package com.burak.healthapp.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.domain.model.CaffeineDrinkSize
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.today.ExerciseCardState
import com.burak.healthapp.feature.today.HydrationCardState
import com.burak.healthapp.feature.today.MacroRingState
import com.burak.healthapp.feature.today.NutritionCardState
import com.burak.healthapp.feature.today.SleepCardState
import com.burak.healthapp.feature.today.SmokingCardState
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.StepCardState
import com.burak.healthapp.feature.today.SupplementCardState
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.TodayUiState
import com.burak.healthapp.feature.today.WeightCardState
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
import java.time.LocalTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = selectedDate
        .flatMapLatest { date ->
            dashboardRepository.observeToday(date)
                .map(::snapshotToUiState)
                .flowOn(Dispatchers.Default)
                .distinctUntilChanged()
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyUiState(),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun addMeal(
        mealType: MealType,
        name: String,
        calories: Int,
        carbs: Int,
        fat: Int,
        protein: Int,
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            dashboardRepository.saveMealEntry(
                MealEntry(
                    date = selectedDate.value,
                    mealType = mealType,
                    name = name.trim(),
                    calories = calories,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    proteinGrams = protein,
                ),
            )
        }
    }

    fun addHydration(amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            dashboardRepository.addHydration(
                amountMl = amountMl,
                date = selectedDate.value,
            )
        }
    }

    fun addCaffeine(
        drinkType: CaffeineDrinkType,
        size: CaffeineDrinkSize,
        estimatedMg: Int,
        customName: String?,
    ) {
        if (estimatedMg <= 0) return
        viewModelScope.launch {
            dashboardRepository.addCaffeine(
                CaffeineEntry(
                    date = selectedDate.value,
                    time = LocalTime.now(),
                    drinkType = drinkType,
                    size = size,
                    estimatedMg = estimatedMg,
                    customName = customName,
                ),
            )
        }
    }

    fun saveSleep(startTime: LocalTime, endTime: LocalTime) {
        viewModelScope.launch {
            val targetDate = selectedDate.value
            val end = targetDate.atTime(endTime)
            val start = if (endTime <= startTime) {
                targetDate.minusDays(1).atTime(startTime)
            } else {
                targetDate.atTime(startTime)
            }
            dashboardRepository.saveSleepSession(
                com.burak.healthapp.domain.model.SleepSession(
                    startTime = start,
                    endTime = end,
                ),
            )
        }
    }

    fun saveWeight(weightKg: Float) {
        if (weightKg <= 0f) return
        viewModelScope.launch {
            dashboardRepository.saveWeightMeasurement(
                weightKg = weightKg,
                date = selectedDate.value,
            )
        }
    }

    fun saveSupplementDoses(doses: List<SupplementDoseEntry>) {
        viewModelScope.launch {
            dashboardRepository.saveSupplementDoseEntries(
                entries = doses,
                date = selectedDate.value,
            )
        }
    }

    fun saveExercise(
        type: ExerciseType,
        durationMinutes: Int,
        intensity: ExerciseIntensity,
    ) {
        if (durationMinutes <= 0) return
        viewModelScope.launch {
            dashboardRepository.saveExerciseEntry(
                entry = ExerciseEntry(
                    date = selectedDate.value,
                    type = type,
                    durationMinutes = durationMinutes,
                    intensity = intensity,
                ),
                date = selectedDate.value,
            )
        }
    }

    fun saveSmokingCount(count: Int) {
        viewModelScope.launch {
            dashboardRepository.saveSmokingCount(
                count = count,
                date = selectedDate.value,
            )
        }
    }

    fun deleteHydrationEntry(id: Long) {
        viewModelScope.launch {
            dashboardRepository.deleteHydrationEntry(id)
        }
    }

    fun deleteSleep() {
        viewModelScope.launch {
            dashboardRepository.deleteSleepForDate(selectedDate.value)
        }
    }

    fun deleteExercise() {
        viewModelScope.launch {
            dashboardRepository.deleteExerciseForDate(selectedDate.value)
        }
    }

    fun deleteSmoking() {
        viewModelScope.launch {
            dashboardRepository.deleteSmokingForDate(selectedDate.value)
        }
    }

    fun deleteSupplementDose(templateId: Long) {
        viewModelScope.launch {
            dashboardRepository.deleteSupplementDoseForDate(
                templateId = templateId,
                date = selectedDate.value,
            )
        }
    }

    fun incrementSmoking() {
        viewModelScope.launch {
            dashboardRepository.incrementSmokingCount(date = selectedDate.value)
        }
    }

    fun updateDashboardCardVisibility(type: DashboardCardType, isVisible: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDashboardCardVisibility(type, isVisible)
        }
    }

    fun moveDashboardCard(type: DashboardCardType, newIndex: Int) {
        viewModelScope.launch {
            settingsRepository.moveDashboardCard(type, newIndex)
        }
    }

    fun resetDashboardCards() {
        viewModelScope.launch {
            settingsRepository.resetDashboardCardsToDefault()
        }
    }
}
