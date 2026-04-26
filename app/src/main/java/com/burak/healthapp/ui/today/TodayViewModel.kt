package com.burak.healthapp.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.data.repository.DashboardRepository
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
import com.burak.healthapp.ui.model.ExerciseCardState
import com.burak.healthapp.ui.model.HydrationCardState
import com.burak.healthapp.ui.model.MacroRingState
import com.burak.healthapp.ui.model.NutritionCardState
import com.burak.healthapp.ui.model.SleepCardState
import com.burak.healthapp.ui.model.SmokingCardState
import com.burak.healthapp.ui.model.SmokingStatus
import com.burak.healthapp.ui.model.StepCardState
import com.burak.healthapp.ui.model.SupplementCardState
import com.burak.healthapp.ui.model.SupplementItemState
import com.burak.healthapp.ui.model.TodayUiState
import com.burak.healthapp.ui.model.WeightCardState
import com.burak.healthapp.ui.root.healthApplication
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

private fun snapshotToUiState(snapshot: TodaySnapshot): TodayUiState {
    val totals = calculateNutritionTotals(snapshot.meals)
    val hydrationTotal = calculateHydrationTotal(snapshot.hydrationEntries)
    val goals = snapshot.settings.goalSettings
    val sleepMinutes = calculateSleepDurationMinutes(snapshot.sleepSessionForDate)
    val locale = Locale.forLanguageTag("tr")
    val currentDoseByTemplate = snapshot.supplementDoseEntries
        .groupBy { it.templateId }
        .mapValues { (_, entries) -> entries.sumOf { it.amount.toDouble() }.toFloat() }
    val weightState = snapshot.measurementForDate.toWeightCardState(goals, locale)
    val exerciseDays = countExerciseDays(snapshot.weekExerciseEntries)
    val exerciseEntry = snapshot.exerciseEntryForDate
    val smokingEntry = snapshot.smokingEntryForDate
    val stepCount = snapshot.stepEntryForDate?.steps ?: 0
    val weekSteps = snapshot.weekStepEntries.sumOf { it.steps }

    return TodayUiState(
        userName = snapshot.settings.userProfile.name,
        avatarInitials = snapshot.settings.userProfile.avatarInitials,
        goalSettings = goals,
        latestMeasurement = snapshot.measurementForDate,
        nutrition = NutritionCardState(
            currentCalories = totals.calories,
            targetCalories = goals.dailyCaloriesTarget,
            progress = clampProgress(totals.calories.toFloat(), goals.dailyCaloriesTarget.toFloat()),
            macros = listOf(
                MacroRingState(
                    label = "Karb",
                    current = totals.carbsGrams,
                    target = goals.carbTargetGrams,
                    progress = clampProgress(totals.carbsGrams.toFloat(), goals.carbTargetGrams.toFloat()),
                ),
                MacroRingState(
                    label = "Yağ",
                    current = totals.fatGrams,
                    target = goals.fatTargetGrams,
                    progress = clampProgress(totals.fatGrams.toFloat(), goals.fatTargetGrams.toFloat()),
                ),
                MacroRingState(
                    label = "Protein",
                    current = totals.proteinGrams,
                    target = goals.proteinTargetGrams,
                    progress = clampProgress(totals.proteinGrams.toFloat(), goals.proteinTargetGrams.toFloat()),
                    isEmphasized = true,
                ),
            ),
            entries = snapshot.meals,
        ),
        weight = weightState,
        exercise = ExerciseCardState(
            type = exerciseEntry?.type,
            durationMinutes = exerciseEntry?.durationMinutes ?: 0,
            intensity = exerciseEntry?.intensity,
            progress = clampProgress(
                (exerciseEntry?.durationMinutes ?: 0).toFloat(),
                goals.exerciseTargetDurationMinutes.toFloat(),
            ),
            title = exerciseEntry?.type?.label ?: "Antrenman eklenmedi",
            durationLabel = if (exerciseEntry != null) {
                "${exerciseEntry.durationMinutes} dk"
            } else {
                "Süre eklenmedi"
            },
            intensityLabel = exerciseEntry?.intensity?.label ?: "Yoğunluk seçilmedi",
            helperLabel = "Bu hafta $exerciseDays / ${goals.exerciseTargetDaysPerWeek} gün",
        ),
        hydration = HydrationCardState(
            currentMl = hydrationTotal,
            targetMl = goals.waterTargetMl,
            progress = clampProgress(hydrationTotal.toFloat(), goals.waterTargetMl.toFloat()),
            entries = snapshot.hydrationEntries,
        ),
        sleep = SleepCardState(
            durationLabel = formatSleepDuration(snapshot.sleepSessionForDate),
            timeRangeLabel = formatClockRange(snapshot.sleepSessionForDate),
            targetLabel = formatMinutesAsSleepLabel(goals.sleepTargetMinutes),
            progress = clampProgress(sleepMinutes.toFloat(), goals.sleepTargetMinutes.toFloat()),
        ),
        smoking = smokingEntry.toSmokingCardState(goals),
        steps = StepCardState(
            currentSteps = stepCount,
            targetSteps = goals.dailyStepTarget,
            progress = clampProgress(stepCount.toFloat(), goals.dailyStepTarget.toFloat()),
            headline = "$stepCount adım",
            supportingLabel = "Hedef ${goals.dailyStepTarget} adım",
            helperLabel = "Bu hafta $weekSteps adım",
        ),
        supplements = SupplementCardState(
            items = snapshot.supplementTemplates.map { template ->
                val currentAmount = currentDoseByTemplate[template.id] ?: 0f
                SupplementItemState(
                    id = template.id,
                    name = template.name,
                    currentAmount = currentAmount,
                    targetAmount = template.targetAmount,
                    unitLabel = template.unitLabel,
                    progress = clampProgress(currentAmount, template.targetAmount),
                )
            },
        ),
    )
}

private fun com.burak.healthapp.domain.model.BodyMeasurementEntry?.toWeightCardState(
    goals: GoalSettings,
    locale: Locale,
): WeightCardState {
    if (this == null) {
        return WeightCardState(
            currentWeightKg = null,
            targetWeightKg = goals.targetWeightKg,
            progress = 0f,
            hasMeasurement = false,
            headline = "Kayıt yok",
            supportingLabel = "Bu tarih için kilo eklenmedi",
            helperLabel = String.format(locale, "Hedef %.1f kg", goals.targetWeightKg),
        )
    }

    return WeightCardState(
        currentWeightKg = weightKg,
        targetWeightKg = goals.targetWeightKg,
        progress = directionAwareProgress(
            baseline = goals.baselineWeightKg,
            current = weightKg,
            target = goals.targetWeightKg,
        ),
        hasMeasurement = true,
        headline = String.format(locale, "%.1f kg", weightKg),
        supportingLabel = String.format(locale, "Hedef %.1f kg", goals.targetWeightKg),
        helperLabel = String.format(locale, "Başlangıç %.1f kg", goals.baselineWeightKg),
    )
}

private fun emptyUiState(): TodayUiState {
    return TodayUiState(
        userName = "",
        avatarInitials = "M",
        goalSettings = GoalSettings(),
        latestMeasurement = null,
        nutrition = NutritionCardState(
            currentCalories = 0,
            targetCalories = 0,
            progress = 0f,
            macros = emptyList(),
            entries = emptyList(),
        ),
        weight = WeightCardState(
            currentWeightKg = null,
            targetWeightKg = 0f,
            progress = 0f,
            hasMeasurement = false,
            headline = "Kayıt yok",
            supportingLabel = "Bu tarih için kilo eklenmedi",
            helperLabel = "",
        ),
        exercise = ExerciseCardState(
            type = null,
            durationMinutes = 0,
            intensity = null,
            progress = 0f,
            title = "Antrenman eklenmedi",
            durationLabel = "Süre eklenmedi",
            intensityLabel = "Yoğunluk seçilmedi",
            helperLabel = "",
        ),
        hydration = HydrationCardState(
            currentMl = 0,
            targetMl = 0,
            progress = 0f,
        ),
        sleep = SleepCardState(
            durationLabel = "Henüz kayıt yok",
            timeRangeLabel = "Saat ekle",
            targetLabel = "8s 0d",
            progress = 0f,
        ),
        smoking = SmokingCardState(
            count = 0,
            limit = 0,
            progress = 0f,
            headline = "0 adet",
            supportingLabel = "Limit ayarlanmadı",
            helperLabel = "İstersen profilden günlük limit ekleyebilirsin.",
            status = SmokingStatus.PASSIVE,
        ),
        steps = StepCardState(
            currentSteps = 0,
            targetSteps = 8000,
            progress = 0f,
            headline = "0 adım",
            supportingLabel = "Hedef 8000 adım",
            helperLabel = "Telefon hareket ettikçe otomatik güncellenir.",
        ),
        supplements = SupplementCardState(items = emptyList()),
    )
}

private fun com.burak.healthapp.domain.model.SmokingEntry?.toSmokingCardState(
    goals: GoalSettings,
): SmokingCardState {
    val count = this?.count ?: 0
    val limit = goals.smokeDailyLimit

    return when {
        limit <= 0 -> SmokingCardState(
            count = count,
            limit = limit,
            progress = 0f,
            headline = "$count adet",
            supportingLabel = "Limit ayarlanmadı",
            helperLabel = "İstersen profilden günlük limit ekleyebilirsin.",
            status = SmokingStatus.PASSIVE,
        )

        count == 0 -> SmokingCardState(
            count = 0,
            limit = limit,
            progress = 0f,
            headline = "0 adet",
            supportingLabel = "Limit $limit adet",
            helperLabel = "Bugün hiç sigara içilmedi.",
            status = SmokingStatus.SAFE,
        )

        count < limit -> SmokingCardState(
            count = count,
            limit = limit,
            progress = clampProgress(count.toFloat(), limit.toFloat()),
            headline = "$count adet",
            supportingLabel = "Limit $limit adet",
            helperLabel = "Limitin içindesin.",
            status = SmokingStatus.WARNING,
        )

        else -> SmokingCardState(
            count = count,
            limit = limit,
            progress = clampProgress(count.toFloat(), limit.toFloat()),
            headline = "$count adet",
            supportingLabel = "Limit $limit adet",
            helperLabel = "Günlük limit aşıldı.",
            status = SmokingStatus.DANGER,
        )
    }
}
