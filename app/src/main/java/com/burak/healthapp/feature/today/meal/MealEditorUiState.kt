package com.burak.healthapp.feature.today.meal

import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.validation.HealthInputError

data class MealDraftFoodState(
    val draftId: Long,
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val nameError: HealthInputError? = null,
    val calorieError: HealthInputError? = null,
    val macroError: HealthInputError? = null,
)

data class MealTotalSummary(
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0,
    val foodCount: Int = 0,
    val hasInvalidDrafts: Boolean = false,
)

data class MealEditorUiState(
    val mealType: MealType = MealType.BREAKFAST,
    val draftFoods: List<MealDraftFoodState> = emptyList(),
    val canSave: Boolean = false,
    val totalSummary: MealTotalSummary = MealTotalSummary(),
)
