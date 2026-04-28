package com.burak.healthapp.domain.model

import androidx.annotation.StringRes
import com.burak.healthapp.R
import java.time.LocalDate
import java.time.LocalDateTime

enum class MealType(@StringRes val labelResId: Int) {
    BREAKFAST(R.string.meal_type_breakfast),
    LUNCH(R.string.meal_type_lunch),
    DINNER(R.string.meal_type_dinner),
    SNACK(R.string.meal_type_snack),
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
