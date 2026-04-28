package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.components.SupplementsCard
import org.junit.Rule
import org.junit.Test

class SupplementsCardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun supplementsCard_zeroItems_showsEmptyState() {
        composeRule.setContent {
            HealthTheme {
                SupplementsCard(
                    items = emptyList(),
                    onAdd = {},
                    onDeleteDose = {},
                )
            }
        }

        composeRule.onNodeWithText(
            composeRule.activity.getString(R.string.today_empty_supplements),
        ).assertIsDisplayed()
    }

    @Test
    fun supplementsCard_oneItem_rendersCorrectly() {
        composeRule.setContent {
            HealthTheme {
                SupplementsCard(
                    items = listOf(
                        SupplementItemState(1, "Vitamin C", 500f, 1000f, "mg", 0.5f),
                    ),
                    onAdd = {},
                    onDeleteDose = {},
                )
            }
        }

        composeRule.onNodeWithTag("supplements_card").assertIsDisplayed()
        composeRule.onNodeWithText("Vitamin C").assertIsDisplayed()
        composeRule.onNodeWithTag("supplement_dose_delete_1").assertIsDisplayed()
    }

    @Test
    fun supplementsCard_twoItems_rendersCorrectly() {
        composeRule.setContent {
            HealthTheme {
                SupplementsCard(
                    items = listOf(
                        SupplementItemState(1, "Vitamin C", 500f, 1000f, "mg", 0.5f),
                        SupplementItemState(2, "Vitamin D", 1000f, 2000f, "IU", 0.5f),
                    ),
                    onAdd = {},
                    onDeleteDose = {},
                )
            }
        }

        composeRule.onNodeWithTag("supplements_card").assertIsDisplayed()
        composeRule.onNodeWithTag("supplements_centered_row").assertIsDisplayed()
        composeRule.onAllNodesWithTag("supplements_lazy_row").assertCountEquals(0)
        composeRule.onNodeWithText("Vitamin C").assertIsDisplayed()
        composeRule.onNodeWithText("Vitamin D").assertIsDisplayed()
    }

    @Test
    fun supplementsCard_threeItems_rendersCorrectly() {
        composeRule.setContent {
            HealthTheme {
                SupplementsCard(
                    items = listOf(
                        SupplementItemState(1, "Vitamin C", 500f, 1000f, "mg", 0.5f),
                        SupplementItemState(2, "Vitamin D", 1000f, 2000f, "IU", 0.5f),
                        SupplementItemState(3, "Zinc", 15f, 30f, "mg", 0.5f),
                    ),
                    onAdd = {},
                    onDeleteDose = {},
                )
            }
        }

        composeRule.onNodeWithTag("supplements_card").assertIsDisplayed()
        composeRule.onNodeWithTag("supplements_lazy_row").assertIsDisplayed()
        composeRule.onAllNodesWithTag("supplements_centered_row").assertCountEquals(0)
        composeRule.onNodeWithText("Vitamin C").assertIsDisplayed()
        composeRule.onNodeWithText("Vitamin D").assertIsDisplayed()
        composeRule.onNodeWithText("Zinc").assertIsDisplayed()
    }
}
