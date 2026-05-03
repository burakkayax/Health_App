package com.burak.healthapp.feature.today.meal

import com.burak.healthapp.domain.model.nutrition.NutrientProfile
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import kotlin.math.roundToInt

data class NutritionPresetAutofillState(
    val food: NutritionPresetFood,
    val grams: Float,
    val nutrients: NutrientProfile = food.nutrientsForGrams(grams),
) {
    fun toMealDraftFoodState(draftId: Long): MealDraftFoodState = MealDraftFoodState(
        draftId = draftId,
        name = food.nameTr,
        calories = nutrients.energyKcal.roundToInt().toString(),
        protein = nutrients.proteinG.roundToInt().toString(),
        carbs = nutrients.carbsG.roundToInt().toString(),
        fat = nutrients.fatG.roundToInt().toString(),
    )
}

fun NutritionPresetFood.defaultAutofill(): NutritionPresetAutofillState = NutritionPresetAutofillState(
    food = this,
    grams = defaultServing.grams,
)
