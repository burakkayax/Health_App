package com.burak.healthapp.feature.today.meal

import com.burak.healthapp.domain.model.nutrition.NutrientProfile
import com.burak.healthapp.domain.model.nutrition.NutritionDataQuality
import com.burak.healthapp.domain.model.nutrition.NutritionDataQualityLevel
import com.burak.healthapp.domain.model.nutrition.NutritionDataSource
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import com.burak.healthapp.domain.model.nutrition.NutritionServing
import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionPresetAutofillTest {

    @Test
    fun `toMealDraftFoodState rounds values correctly`() {
        val nutrients = NutrientProfile(energyKcal = 150.4f, proteinG = 10.5f, carbsG = 20.6f, fatG = 5.49f)
        val food = NutritionPresetFood(
            id = "test_food",
            slug = "test_food",
            nameTr = "Test Food",
            nameTrOriginal = null,
            aliasesTr = emptyList(),
            searchTermsTr = emptyList(),
            nameEnSource = null,
            categoryTr = "Test",
            defaultServing = NutritionServing("Adet", 100f),
            commonServings = emptyList(),
            basis = "100g",
            source = NutritionDataSource("test", "test", null, null, null),
            nutrientsPer100g = nutrients,
            nutrientsPerDefaultServing = nutrients,
            dataQuality = NutritionDataQuality(NutritionDataQualityLevel.HIGH, ""),
        )

        val state = NutritionPresetAutofillState(food, 100f, nutrients)
        val draft = state.toMealDraftFoodState(1L)

        assertEquals("Test Food", draft.name)
        assertEquals("150", draft.calories)
        assertEquals("11", draft.protein) // 10.5 rounds to 11
        assertEquals("21", draft.carbs) // 20.6 rounds to 21
        assertEquals("5", draft.fat) // 5.49 rounds to 5
    }

    @Test
    fun `toMealDraftFoodState handles negative and NaN values safely`() {
        val nutrients = NutrientProfile(energyKcal = Float.NaN, proteinG = -10.0f, carbsG = -0.5f, fatG = Float.NaN)
        val food = NutritionPresetFood(
            id = "test_food",
            slug = "test_food",
            nameTr = "Test Food",
            nameTrOriginal = null,
            aliasesTr = emptyList(),
            searchTermsTr = emptyList(),
            nameEnSource = null,
            categoryTr = "Test",
            defaultServing = NutritionServing("Adet", 100f),
            commonServings = emptyList(),
            basis = "100g",
            source = NutritionDataSource("test", "test", null, null, null),
            nutrientsPer100g = nutrients,
            nutrientsPerDefaultServing = nutrients,
            dataQuality = NutritionDataQuality(NutritionDataQualityLevel.HIGH, ""),
        )

        val state = NutritionPresetAutofillState(food, 100f, nutrients)
        val draft = state.toMealDraftFoodState(1L)

        assertEquals("0", draft.calories)
        assertEquals("0", draft.protein)
        assertEquals("0", draft.carbs)
        assertEquals("0", draft.fat)
    }
}
