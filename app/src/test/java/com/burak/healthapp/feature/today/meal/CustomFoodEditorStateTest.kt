package com.burak.healthapp.feature.today.meal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomFoodEditorStateTest {
    @Test
    fun `canSave is true when name, servingGrams and calories are valid`() {
        val state = CustomFoodEditorState(
            name = "Tavuk göğsü",
            servingGrams = "100",
            calories = "165",
        )
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave is false when name is blank`() {
        val state = CustomFoodEditorState(
            name = "  ",
            servingGrams = "100",
            calories = "165",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when servingGrams is zero`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "0",
            calories = "165",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when servingGrams is empty`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "",
            calories = "165",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when calories is empty`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave accepts comma decimal in servingGrams`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "12,5",
            calories = "100",
        )
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave accepts dot decimal in servingGrams`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "12.5",
            calories = "100",
        )
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave is false when servingGrams is negative`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "-10",
            calories = "100",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when calories is negative`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "-1",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when servingGrams is not a number`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "abc",
            calories = "100",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when protein is negative`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "100",
            protein = "-5",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when carbs is invalid`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "100",
            carbs = "abc",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when fat is negative`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "100",
            fat = "-1",
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave accepts blank protein as zero`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "100",
            protein = "",
        )
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave accepts comma decimal in fat`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "100",
            fat = "12,5",
        )
        assertTrue(state.canSave)
    }

    @Test
    fun `isEditing is true when id is set`() {
        val state = CustomFoodEditorState(id = 5L)
        assertTrue(state.isEditing)
    }

    @Test
    fun `isEditing is false when id is null`() {
        val state = CustomFoodEditorState(id = null)
        assertFalse(state.isEditing)
    }

    @Test
    fun `canSave accepts zero calories`() {
        val state = CustomFoodEditorState(
            name = "Water",
            servingGrams = "250",
            calories = "0",
        )
        assertTrue(state.canSave)
    }

    @Test
    fun `canSave is false when isSaving`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "100",
            isSaving = true,
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `canSave is false when isDeleting`() {
        val state = CustomFoodEditorState(
            name = "Test",
            servingGrams = "100",
            calories = "100",
            isDeleting = true,
        )
        assertFalse(state.canSave)
    }

    @Test
    fun `error enum fields are correctly set`() {
        val state = CustomFoodEditorState(
            nameError = CustomFoodFieldError.NAME_REQUIRED,
            servingError = CustomFoodFieldError.SERVING_REQUIRED,
            caloriesError = CustomFoodFieldError.NEGATIVE_VALUE,
        )
        assertEquals(CustomFoodFieldError.NAME_REQUIRED, state.nameError)
        assertEquals(CustomFoodFieldError.SERVING_REQUIRED, state.servingError)
        assertEquals(CustomFoodFieldError.NEGATIVE_VALUE, state.caloriesError)
        assertTrue(state.hasFieldError)
    }

    @Test
    fun `hasFieldError is false when no errors`() {
        val state = CustomFoodEditorState(name = "Test")
        assertFalse(state.hasFieldError)
    }

    // ── Validation helper tests ──

    @Test
    fun `validateRequiredPositiveFloat returns null for valid value`() {
        assertNull(validateRequiredPositiveFloat("12.5"))
        assertNull(validateRequiredPositiveFloat("12,5"))
        assertNull(validateRequiredPositiveFloat("100"))
    }

    @Test
    fun `validateRequiredPositiveFloat returns SERVING_REQUIRED for blank`() {
        assertEquals(CustomFoodFieldError.SERVING_REQUIRED, validateRequiredPositiveFloat(""))
        assertEquals(CustomFoodFieldError.SERVING_REQUIRED, validateRequiredPositiveFloat("  "))
    }

    @Test
    fun `validateRequiredPositiveFloat returns SERVING_REQUIRED for zero`() {
        assertEquals(CustomFoodFieldError.SERVING_REQUIRED, validateRequiredPositiveFloat("0"))
    }

    @Test
    fun `validateRequiredPositiveFloat returns NEGATIVE_VALUE for negative`() {
        assertEquals(CustomFoodFieldError.NEGATIVE_VALUE, validateRequiredPositiveFloat("-10"))
    }

    @Test
    fun `validateRequiredPositiveFloat returns INVALID_NUMBER for text`() {
        assertEquals(CustomFoodFieldError.INVALID_NUMBER, validateRequiredPositiveFloat("abc"))
    }

    @Test
    fun `validateRequiredPositiveFloat returns VALUE_TOO_LARGE for huge value`() {
        assertEquals(CustomFoodFieldError.VALUE_TOO_LARGE, validateRequiredPositiveFloat("100000"))
    }

    @Test
    fun `validateRequiredNonNegativeInt returns null for valid value`() {
        assertNull(validateRequiredNonNegativeInt("100"))
        assertNull(validateRequiredNonNegativeInt("0"))
    }

    @Test
    fun `validateRequiredNonNegativeInt returns CALORIES_REQUIRED for blank`() {
        assertEquals(CustomFoodFieldError.CALORIES_REQUIRED, validateRequiredNonNegativeInt(""))
    }

    @Test
    fun `validateRequiredNonNegativeInt returns NEGATIVE_VALUE for negative`() {
        assertEquals(CustomFoodFieldError.NEGATIVE_VALUE, validateRequiredNonNegativeInt("-1"))
    }

    @Test
    fun `validateOptionalNonNegativeInt returns null for blank`() {
        assertNull(validateOptionalNonNegativeInt(""))
    }

    @Test
    fun `validateOptionalNonNegativeInt returns NEGATIVE_VALUE for negative`() {
        assertEquals(CustomFoodFieldError.NEGATIVE_VALUE, validateOptionalNonNegativeInt("-5"))
    }

    @Test
    fun `validateOptionalNonNegativeInt returns INVALID_NUMBER for text`() {
        assertEquals(CustomFoodFieldError.INVALID_NUMBER, validateOptionalNonNegativeInt("abc"))
    }

    @Test
    fun `parseDecimalInput parses comma and dot decimals`() {
        assertEquals(12.5f, parseDecimalInput("12,5"))
        assertEquals(12.5f, parseDecimalInput("12.5"))
        assertEquals(100f, parseDecimalInput("100"))
        assertNull(parseDecimalInput("abc"))
        assertNull(parseDecimalInput(""))
    }
}
