package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.feature.profile.EditableSupplementTemplateState
import com.burak.healthapp.feature.profile.ProfileContent
import com.burak.healthapp.feature.profile.ProfileGoalSummaryState
import com.burak.healthapp.feature.profile.ProfileSupplementTemplateState
import com.burak.healthapp.feature.profile.ProfileUiState
import com.burak.healthapp.feature.profile.SupplementEditorUiState
import com.burak.healthapp.feature.profile.SupplementTemplateEditorSheet
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
                    onExportData = {},
                    onImportData = {},
                    onDeleteAllHealthData = {},
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
                    onExportData = {},
                    onImportData = {},
                    onDeleteAllHealthData = {},
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
                    onExportData = {},
                    onImportData = {},
                    onDeleteAllHealthData = {},
                    onThemeModeChange = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_theme_section").assertIsDisplayed()
    }

    @Test
    fun dataManagementSection_showsExportAction() {
        var exportClicked = false

        composeRule.setContent {
            HealthTheme {
                ProfileContent(
                    state = sampleProfileState(),
                    onOpenGoals = {},
                    onManageSupplements = {},
                    onExportData = { exportClicked = true },
                    onImportData = {},
                    onDeleteAllHealthData = {},
                    onThemeModeChange = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_data_management_section").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_export_data_button").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_export_data_button").performClick()
        assertEquals(true, exportClicked)
    }

    @Test
    fun dataManagementSection_showsImportAndDeleteActions() {
        composeRule.setContent {
            HealthTheme {
                ProfileContent(
                    state = sampleProfileState(),
                    onOpenGoals = {},
                    onManageSupplements = {},
                    onExportData = {},
                    onImportData = {},
                    onDeleteAllHealthData = {},
                    onThemeModeChange = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_import_data_button").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_delete_all_health_data_button").assertIsDisplayed()
    }

    @Test
    fun preferenceCards_areVisibleOnProfile() {
        composeRule.setContent {
            HealthTheme {
                ProfileContent(
                    state = sampleProfileState(),
                    onOpenGoals = {},
                    onManageSupplements = {},
                    onExportData = {},
                    onImportData = {},
                    onDeleteAllHealthData = {},
                    onThemeModeChange = {},
                )
            }
        }

        composeRule.onNodeWithTag("profile_step_tracking_card").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_water_reminder_card").assertIsDisplayed()
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
        var validationMessage = ""
        composeRule.setContent {
            HealthTheme {
                validationMessage = UiText.StringResource(R.string.error_fix_missing_fields).asString()
                SupplementTemplateEditorSheet(
                    state = SupplementEditorUiState(
                        isVisible = true,
                        drafts = listOf(
                            EditableSupplementTemplateState(
                                draftId = 1,
                                name = "",
                                targetAmount = "",
                                unitLabel = "mg",
                                nameError = UiText.StringResource(R.string.error_supplement_name_required),
                                targetAmountError = UiText.StringResource(R.string.error_supplement_target_required),
                            ),
                        ),
                        canSave = false,
                        validationMessage = UiText.StringResource(R.string.error_fix_missing_fields),
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
        composeRule.onNodeWithText(validationMessage).assertIsDisplayed()
        composeRule.onNodeWithTag("supplement_template_save_button").assertIsNotEnabled()
    }

    private fun sampleProfileState(): ProfileUiState = ProfileUiState(
        userName = "Burak",
        avatarInitials = "BK",
        themeMode = ThemeMode.SYSTEM,
        goalSummaries = listOf(
            ProfileGoalSummaryState(
                UiText.DynamicString("Kalori / Su"),
                UiText.DynamicString("2200 kcal • 2500 ml"),
            ),
        ),
        supplementTemplates = listOf(
            ProfileSupplementTemplateState(1, "Magnezyum", 200f, "mg"),
        ),
    )
}
