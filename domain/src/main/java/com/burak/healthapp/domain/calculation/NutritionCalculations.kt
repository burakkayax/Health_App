package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.CalorieBarPoint
import com.burak.healthapp.domain.model.DayNutritionTotal
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import java.time.DayOfWeek
import java.time.LocalDate

fun calculateNutritionTotals(entries: List<MealEntry>): DayNutritionTotal = entries.fold(DayNutritionTotal()) { total, entry ->
    total.copy(
        calories = total.calories + entry.calories,
        carbsGrams = total.carbsGrams + entry.carbsGrams,
        fatGrams = total.fatGrams + entry.fatGrams,
        proteinGrams = total.proteinGrams + entry.proteinGrams,
    )
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

fun groupMealsByType(entries: List<MealEntry>): List<GroupedMealEntries> = MealType.entries.mapNotNull { mealType ->
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

data class GroupedMealEntries(
    val mealType: MealType,
    val entries: List<MealEntry>,
)

private fun LocalDate.toTurkishWeekLetter(): String = when (dayOfWeek) {
    DayOfWeek.MONDAY -> "P"
    DayOfWeek.TUESDAY -> "S"
    DayOfWeek.WEDNESDAY -> "Ç"
    DayOfWeek.THURSDAY -> "P"
    DayOfWeek.FRIDAY -> "C"
    DayOfWeek.SATURDAY -> "C"
    DayOfWeek.SUNDAY -> "P"
}
