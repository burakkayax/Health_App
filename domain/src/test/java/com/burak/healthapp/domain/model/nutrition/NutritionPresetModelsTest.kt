package com.burak.healthapp.domain.model.nutrition

import org.junit.Assert.assertEquals
import org.junit.Test

class NutritionPresetModelsTest {

    @Test
    fun `nutrientsForGrams for 100g returns identical values`() {
        val defaultNutrients = NutrientProfile(energyKcal = 150f, proteinG = 10f, carbsG = 20f, fatG = 5f)
        val food = NutritionPresetFood(
            id = "test_food",
            slug = "test_food",
            nameTr = "Test",
            nameTrOriginal = null,
            aliasesTr = emptyList(),
            searchTermsTr = emptyList(),
            nameEnSource = null,
            categoryTr = "Test",
            defaultServing = NutritionServing("Adet", 100f),
            commonServings = emptyList(),
            basis = "100g",
            source = NutritionDataSource("test", "test", null, null, null),
            nutrientsPer100g = defaultNutrients,
            nutrientsPerDefaultServing = defaultNutrients,
            dataQuality = NutritionDataQuality(NutritionDataQualityLevel.HIGH, "")
        )

        val result = food.nutrientsForGrams(100f)

        assertEquals(150f, result.energyKcal, 0.001f)
        assertEquals(10f, result.proteinG, 0.001f)
        assertEquals(20f, result.carbsG, 0.001f)
        assertEquals(5f, result.fatG, 0.001f)
    }

    @Test
    fun `nutrientsForGrams for 150g scales by 1_5x`() {
        val defaultNutrients = NutrientProfile(energyKcal = 150f, proteinG = 10f, carbsG = 20f, fatG = 5f)
        val food = NutritionPresetFood(
            id = "test_food",
            slug = "test_food",
            nameTr = "Test",
            nameTrOriginal = null,
            aliasesTr = emptyList(),
            searchTermsTr = emptyList(),
            nameEnSource = null,
            categoryTr = "Test",
            defaultServing = NutritionServing("Adet", 100f),
            commonServings = emptyList(),
            basis = "100g",
            source = NutritionDataSource("test", "test", null, null, null),
            nutrientsPer100g = defaultNutrients,
            nutrientsPerDefaultServing = defaultNutrients,
            dataQuality = NutritionDataQuality(NutritionDataQualityLevel.HIGH, "")
        )

        val result = food.nutrientsForGrams(150f)

        assertEquals(225f, result.energyKcal, 0.001f)
        assertEquals(15f, result.proteinG, 0.001f)
        assertEquals(30f, result.carbsG, 0.001f)
        assertEquals(7.5f, result.fatG, 0.001f)
    }

    @Test
    fun `nutrientsForGrams handles 0g safely`() {
        val defaultNutrients = NutrientProfile(energyKcal = 150f, proteinG = 10f, carbsG = 20f, fatG = 5f)
        val food = NutritionPresetFood(
            id = "test_food",
            slug = "test_food",
            nameTr = "Test",
            nameTrOriginal = null,
            aliasesTr = emptyList(),
            searchTermsTr = emptyList(),
            nameEnSource = null,
            categoryTr = "Test",
            defaultServing = NutritionServing("Adet", 100f),
            commonServings = emptyList(),
            basis = "100g",
            source = NutritionDataSource("test", "test", null, null, null),
            nutrientsPer100g = defaultNutrients,
            nutrientsPerDefaultServing = defaultNutrients,
            dataQuality = NutritionDataQuality(NutritionDataQualityLevel.HIGH, "")
        )

        val result = food.nutrientsForGrams(0f)

        assertEquals(0f, result.energyKcal, 0.001f)
        assertEquals(0f, result.proteinG, 0.001f)
        assertEquals(0f, result.carbsG, 0.001f)
        assertEquals(0f, result.fatG, 0.001f)
    }
}
