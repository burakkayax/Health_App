package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.ui.mealhistory.MealHistoryContent
import com.burak.healthapp.ui.model.MealHistoryEntryState
import com.burak.healthapp.ui.model.MealHistorySectionState
import com.burak.healthapp.ui.model.MealHistoryUiState
import com.burak.healthapp.ui.theme.HealthTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MealHistoryContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mealSections_areGroupedByTypeAndRendered() {
        composeRule.setContent {
            HealthTheme {
                MealHistoryContent(
                    state = sampleMealHistoryState(),
                    onDeleteMeal = {},
                )
            }
        }

        composeRule.onNodeWithText("Kahvaltı").assertIsDisplayed()
        composeRule.onNodeWithText("Omlet").assertIsDisplayed()
        composeRule.onNodeWithText("Akşam").assertIsDisplayed()
        composeRule.onNodeWithText("Somon").assertIsDisplayed()
    }

    @Test
    fun deleteAction_invokesCallback() {
        var deletedId = -1L

        composeRule.setContent {
            HealthTheme {
                MealHistoryContent(
                    state = sampleMealHistoryState(),
                    onDeleteMeal = { deletedId = it },
                )
            }
        }

        composeRule.onNodeWithTag("meal_history_delete_2").performClick()
        assertEquals(2L, deletedId)
    }

    private fun sampleMealHistoryState(): MealHistoryUiState {
        return MealHistoryUiState(
            sections = listOf(
                MealHistorySectionState(
                    title = "Kahvaltı",
                    entries = listOf(
                        MealHistoryEntryState(
                            id = 1,
                            mealType = MealType.BREAKFAST,
                            name = "Omlet",
                            calories = 420,
                            proteinGrams = 28,
                            carbsGrams = 16,
                            fatGrams = 22,
                        ),
                    ),
                ),
                MealHistorySectionState(
                    title = "Akşam",
                    entries = listOf(
                        MealHistoryEntryState(
                            id = 2,
                            mealType = MealType.DINNER,
                            name = "Somon",
                            calories = 560,
                            proteinGrams = 40,
                            carbsGrams = 24,
                            fatGrams = 30,
                        ),
                    ),
                ),
            ),
        )
    }
}
