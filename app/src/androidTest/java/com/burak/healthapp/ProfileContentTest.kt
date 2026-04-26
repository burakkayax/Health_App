package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.ui.model.EditableSupplementTemplateState
import com.burak.healthapp.ui.model.ProfileGoalSummaryState
import com.burak.healthapp.ui.model.ProfileSupplementTemplateState
import com.burak.healthapp.ui.model.ProfileUiState
import com.burak.healthapp.ui.model.SupplementEditorUiState
import com.burak.healthapp.ui.profile.ProfileContent
import com.burak.healthapp.ui.profile.SupplementTemplateEditorSheet
import com.burak.healthapp.ui.theme.HealthTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun goalsButton_invokesCallback() {
        var openedGoals = false

        composeRule.setContent {
            HealthTheme {
                ProfileContent(
                    state = sampleProfileState(),
                    onOpenGoals = { openedGoals = true },
                    onManageSupplements = {},
                    onThemeModeChange = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_goals_button").performClick()
        assertEquals(true, openedGoals)
    }

    @Test
    fun supplementManageButton_isVisible() {
        composeRule.setContent {
            HealthTheme {
                ProfileContent(
                    state = sampleProfileState(),
                    onOpenGoals = {},
                    onManageSupplements = {},
                    onThemeModeChange = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_supplements_button").assertIsDisplayed()
    }

    @Test
    fun themeSection_isVisible() {
        composeRule.setContent {
            HealthTheme {
                ProfileContent(
                    state = sampleProfileState(),
                    onOpenGoals = {},
                    onManageSupplements = {},
                    onThemeModeChange = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_theme_section").assertIsDisplayed()
    }

    @Test
    fun supplementEditor_keepsActionButtonsVisibleForMultipleDrafts() {
        composeRule.setContent {
            HealthTheme {
                SupplementTemplateEditorSheet(
                    state = SupplementEditorUiState(
                        isVisible = true,
                        drafts = (1L..6L).map { draftId ->
                            EditableSupplementTemplateState(
                                draftId = draftId,
                                name = "Takviye $draftId",
                                targetAmount = "100",
                                unitLabel = "mg",
                            )
                        },
                        canSave = true,
                    ),
                    onNameChange = { _, _ -> },
                    onTargetAmountChange = { _, _ -> },
                    onUnitLabelChange = { _, _ -> },
                    onRemoveDraft = {},
                    onAddDraft = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithTag("supplement_template_add_button").assertIsDisplayed()
        composeRule.onNodeWithTag("supplement_template_save_button").assertIsDisplayed()
    }

    @Test
    fun supplementEditor_invalidState_showsMessageAndDisablesSave() {
        composeRule.setContent {
            HealthTheme {
                SupplementTemplateEditorSheet(
                    state = SupplementEditorUiState(
                        isVisible = true,
                        drafts = listOf(
                            EditableSupplementTemplateState(
                                draftId = 1,
                                name = "",
                                targetAmount = "",
                                unitLabel = "mg",
                                nameError = "Takviye adı gerekli.",
                                targetAmountError = "Hedef doz gerekli.",
                            ),
                        ),
                        canSave = false,
                        validationMessage = "Kaydetmeden önce eksik alanları düzelt.",
                    ),
                    onNameChange = { _, _ -> },
                    onTargetAmountChange = { _, _ -> },
                    onUnitLabelChange = { _, _ -> },
                    onRemoveDraft = {},
                    onAddDraft = {},
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithTag("supplement_editor_message").assertIsDisplayed()
        composeRule.onNodeWithText("Kaydetmeden önce eksik alanları düzelt.").assertIsDisplayed()
        composeRule.onNodeWithTag("supplement_template_save_button").assertIsNotEnabled()
    }

    private fun sampleProfileState(): ProfileUiState {
        return ProfileUiState(
            userName = "Burak",
            avatarInitials = "BK",
            themeMode = ThemeMode.SYSTEM,
            goalSummaries = listOf(
                ProfileGoalSummaryState("Kalori / Su", "2200 kcal • 2500 ml"),
            ),
            supplementTemplates = listOf(
                ProfileSupplementTemplateState(1, "Magnezyum", 200f, "mg"),
            ),
        )
    }
}
