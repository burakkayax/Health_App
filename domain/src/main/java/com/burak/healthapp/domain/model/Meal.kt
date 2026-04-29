package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
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
