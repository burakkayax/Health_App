package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.step.StepDetailContent
import com.burak.healthapp.feature.detail.step.StepDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class StepDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun disabledCard_isVisibleAndEnableButtonInvokesCallbackWhenStepTrackingOff() {
        var enableClicks = 0

        composeRule.setContent {
            HealthTheme {
                StepDetailContent(
                    state = sampleState(stepTrackingEnabled = false),
                    onSelectPeriod = {},
                    onEnableStepTracking = { enableClicks++ },
                )
            }
        }

        composeRule.onNodeWithTag("step_tracking_disabled_card").assertIsDisplayed()
        composeRule.onNodeWithTag("step_tracking_enable_button").performClick()
        assertEquals(1, enableClicks)
    }

    @Test
    fun disabledCard_isHiddenWhenStepTrackingOn() {
        composeRule.setContent {
            HealthTheme {
                StepDetailContent(
                    state = sampleState(stepTrackingEnabled = true),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onAllNodesWithTag("step_tracking_disabled_card").assertCountEquals(0)
    }

    private fun sampleState(stepTrackingEnabled: Boolean): StepDetailUiState = StepDetailUiState(
        selectedPeriod = TrendsPeriod.WEEKLY,
        bars = emptyList(),
        monthDays = emptyList(),
        totalSteps = 0,
        averageSteps = 0,
        targetSteps = 8000,
        hasData = false,
        stepTrackingEnabled = stepTrackingEnabled,
    )
}
