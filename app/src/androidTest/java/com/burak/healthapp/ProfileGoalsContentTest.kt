package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.feature.profile.goals.ProfileGoalsUiState
import com.burak.healthapp.feature.profile.goals.ProfileGoalsContent
import com.burak.healthapp.core.ui.theme.HealthTheme
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ProfileGoalsContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun saveButton_invokesCallback() {
        var saveCount = 0

        composeRule.setContent {
            HealthTheme {
                ProfileGoalsContent(
                    state = sampleState(),
                    onSave = { _, _, _, _ -> saveCount++ },
                )
            }
        }

        composeRule.onNodeWithText("Hedefleri Kaydet").assertIsDisplayed()
        composeRule.onNodeWithText("Hedefleri Kaydet").performClick()
        assertEquals(1, saveCount)
    }

    @Test
    fun targetMeasurementFields_areRemoved() {
        composeRule.setContent {
            HealthTheme {
                ProfileGoalsContent(
                    state = sampleState(),
                    onSave = { _, _, _, _ -> },
                )
            }
        }

        composeRule.onAllNodesWithText("Hedef Omuz").assertCountEquals(0)
        composeRule.onAllNodesWithText("Hedef Bel").assertCountEquals(0)
        composeRule.onAllNodesWithText("Hedef Kalça").assertCountEquals(0)
        composeRule.onNodeWithText("Boy (cm)").assertIsDisplayed()
    }

    @Test
    fun stepGoalAndWaterReminderFields_areVisible() {
        composeRule.setContent {
            HealthTheme {
                ProfileGoalsContent(
                    state = sampleState(),
                    onSave = { _, _, _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText("Adım Hedefi").assertIsDisplayed()
        composeRule.onNodeWithText("Su Hatırlatıcısı").assertIsDisplayed()
        composeRule.onNodeWithText("Sıklık (dk)").assertIsDisplayed()
    }

    private fun sampleState(): ProfileGoalsUiState {
        return ProfileGoalsUiState(
            userName = "Burak",
            avatarInitials = "BK",
            goalSettings = GoalSettings(),
            latestMeasurement = BodyMeasurementEntry(
                date = LocalDate.of(2026, 4, 19),
                weightKg = 77.4f,
                shoulderCm = 119f,
                waistCm = 86f,
                hipCm = 98f,
            ),
            heightCm = 175f,
        )
    }
}
