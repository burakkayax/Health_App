package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class MealType(val label: String) {
    BREAKFAST("Kahvaltı"),
    LUNCH("Öğle"),
    DINNER("AkÅŸam"),
    SNACK("Ara Öğün"),
}

data class MealEntry(
    val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val name: String,
    val calories: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinGrams: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class DayNutritionTotal(
    val calories: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0,
    val proteinGrams: Int = 0,
)
