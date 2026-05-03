package com.burak.healthapp.feature.today.meal

import com.burak.healthapp.domain.model.nutrition.NutrientProfile
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import kotlin.math.roundToInt

data class NutritionPresetAutofillState(
    val food: NutritionPresetFood,
    val grams: Float,
    val nutrients: NutrientProfile = food.nutrientsForGrams(grams.coerceAtLeast(0f)),
) {
    fun toMealDraftFoodState(draftId: Long): MealDraftFoodState = MealDraftFoodState(
        draftId = draftId,
        name = food.nameTr,
        calories = nutrients.energyKcal.toSafeRoundedString(),
        protein = nutrients.proteinG.toSafeRoundedString(),
        carbs = nutrients.carbsG.toSafeRoundedString(),
        fat = nutrients.fatG.toSafeRoundedString(),
    )

    private fun Float.toSafeRoundedString(): String {
        if (this.isNaN() || this < 0f) return "0"
        return this.roundToInt().toString()
    }
}

fun NutritionPresetFood.defaultAutofill(): NutritionPresetAutofillState = NutritionPresetAutofillState(
    food = this,
    grams = defaultServing.grams,
)
