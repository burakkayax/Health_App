package com.burak.healthapp.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.calculation.calculateHydrationTotal
import com.burak.healthapp.domain.calculation.calculateNutritionTotals
import com.burak.healthapp.domain.calculation.calculateSleepDurationMinutes
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.calculation.countExerciseDays
import com.burak.healthapp.domain.calculation.directionAwareProgress
import com.burak.healthapp.domain.calculation.formatClockRange
import com.burak.healthapp.domain.calculation.formatMinutesAsSleepLabel
import com.burak.healthapp.domain.calculation.formatSleepDuration
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.TodaySnapshot
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
import com.burak.healthapp.feature.root.healthApplication
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = selectedDate
        .flatMapLatest { date ->
            dashboardRepository.observeToday(date).map(::snapshotToUiState)
        }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TodayViewModel(
                    dashboardRepository = healthApplication().container.dashboardRepository,
                )
            }
        }
    }
}
