package com.burak.healthapp.feature.today.meal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `error fields are correctly set`() {
        val state = CustomFoodEditorState(
            nameError = "Required",
            servingError = "Invalid",
            caloriesError = "Bad value",
        )
        assertEquals("Required", state.nameError)
        assertEquals("Invalid", state.servingError)
        assertEquals("Bad value", state.caloriesError)
    }
}
