package com.burak.healthapp.feature.today.meal

import com.burak.healthapp.domain.model.nutrition.CustomFood
import com.burak.healthapp.domain.model.nutrition.NutrientProfile
import com.burak.healthapp.domain.model.nutrition.NutritionDataQuality
import com.burak.healthapp.domain.model.nutrition.NutritionDataQualityLevel
import com.burak.healthapp.domain.model.nutrition.NutritionDataSource
import com.burak.healthapp.domain.model.nutrition.NutritionPresetFood
import com.burak.healthapp.domain.model.nutrition.NutritionServing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class MealFoodSearchUiStateTest {
    @Test
    fun `default state has empty results and is loading`() {
        val state = MealFoodSearchUiState()
        assertEquals("", state.query)
        assertTrue(state.isLoading)
        assertFalse(state.isError)
        assertTrue(state.presetResults.isEmpty())
        assertTrue(state.customResults.isEmpty())
        assertEquals(FoodSearchSourceFilter.ALL, state.selectedSource)
    }

    @Test
    fun `state with preset results is not empty`() {
        val state = MealFoodSearchUiState(
            presetResults = listOf(samplePresetFood()),
            isLoading = false,
        )
        assertEquals(1, state.presetResults.size)
        assertTrue(state.customResults.isEmpty())
    }

    @Test
    fun `state with custom results is not empty`() {
        val state = MealFoodSearchUiState(
            customResults = listOf(sampleCustomFood()),
            isLoading = false,
        )
        assertEquals(1, state.customResults.size)
        assertTrue(state.presetResults.isEmpty())
    }

    @Test
    fun `state with both results shows both`() {
        val state = MealFoodSearchUiState(
            presetResults = listOf(samplePresetFood()),
            customResults = listOf(sampleCustomFood()),
            isLoading = false,
        )
        assertEquals(1, state.presetResults.size)
        assertEquals(1, state.customResults.size)
    }

    @Test
    fun `custom food autofill produces correct values`() {
        val food = sampleCustomFood()
        assertEquals("Test Food", food.name)
        assertEquals(200, food.calories)
        assertEquals(25, food.proteinGrams)
        assertEquals(10, food.carbsGrams)
        assertEquals(8, food.fatGrams)
    }

    private fun samplePresetFood(): NutritionPresetFood = NutritionPresetFood(
        id = "test_preset",
        slug = "test-preset",
        nameTr = "Test Preset",
        nameTrOriginal = null,
        aliasesTr = emptyList(),
        searchTermsTr = emptyList(),
        nameEnSource = null,
        categoryTr = "Et",
        defaultServing = NutritionServing(nameTr = "porsiyon", grams = 100f),
        commonServings = emptyList(),
        basis = "100g",
        source = NutritionDataSource(
            dataset = "test",
            sourceId = "1",
            sourceCategory = null,
            accessDate = null,
            licenseNote = null,
        ),
        nutrientsPer100g = NutrientProfile(
            energyKcal = 165f,
            proteinG = 31f,
            carbsG = 0f,
            fatG = 3.6f,
        ),
        nutrientsPerDefaultServing = NutrientProfile(
            energyKcal = 165f,
            proteinG = 31f,
            carbsG = 0f,
            fatG = 3.6f,
        ),
        dataQuality = NutritionDataQuality(
            level = NutritionDataQualityLevel.HIGH,
            notesTr = "",
        ),
    )

    private fun sampleCustomFood(): CustomFood = CustomFood(
        id = 1L,
        name = "Test Food",
        brand = "Test Brand",
        servingName = "Porsiyon",
        servingGrams = 150f,
        calories = 200,
        proteinGrams = 25,
        carbsGrams = 10,
        fatGrams = 8,
        createdAt = LocalDateTime.of(2026, 5, 1, 12, 0),
        updatedAt = LocalDateTime.of(2026, 5, 1, 12, 0),
    )
}
