package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.CalorieBarPoint
import com.burak.healthapp.domain.model.DayNutritionTotal
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.TrendPoint
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

fun calculateNutritionTotals(entries: List<MealEntry>): DayNutritionTotal {
    return entries.fold(DayNutritionTotal()) { total, entry ->
        total.copy(
            calories = total.calories + entry.calories,
            carbsGrams = total.carbsGrams + entry.carbsGrams,
            fatGrams = total.fatGrams + entry.fatGrams,
            proteinGrams = total.proteinGrams + entry.proteinGrams,
        )
    }
}

fun averageProtein(entries: List<MealEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .groupBy(MealEntry::date)
        .mapValues { (_, mealEntries) -> mealEntries.sumOf { it.proteinGrams } }
    return averageByLoggedDays(totalsByDay)
}

fun averageCalories(entries: List<MealEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .groupBy(MealEntry::date)
        .mapValues { (_, mealEntries) -> mealEntries.sumOf { it.calories } }
    return averageByLoggedDays(totalsByDay)
}

fun buildWeeklyCalories(
    entries: List<MealEntry>,
    days: List<LocalDate>,
    targetCalories: Int,
): List<CalorieBarPoint> {
    val byDay = entries.groupBy { it.date }
    return days.map { date ->
        val calories = byDay[date].orEmpty().sumOf { it.calories }
        CalorieBarPoint(
            label = date.toTurkishWeekLetter(),
            calories = calories,
            progress = clampProgress(calories.toFloat(), targetCalories.toFloat()),
        )
    }
}

fun groupMealsByType(entries: List<MealEntry>): List<GroupedMealEntries> {
    return MealType.entries.mapNotNull { mealType ->
        val groupedEntries = entries.filter { it.mealType == mealType }
        if (groupedEntries.isEmpty()) {
            null
        } else {
            GroupedMealEntries(
                mealType = mealType,
                entries = groupedEntries,
            )
        }
    }
}

data class GroupedMealEntries(
    val mealType: MealType,
    val entries: List<MealEntry>,
)

private fun LocalDate.toTurkishWeekLetter(): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "P"
        DayOfWeek.TUESDAY -> "S"
        DayOfWeek.WEDNESDAY -> "Ç"
        DayOfWeek.THURSDAY -> "P"
        DayOfWeek.FRIDAY -> "C"
        DayOfWeek.SATURDAY -> "C"
        DayOfWeek.SUNDAY -> "P"
    }
}
