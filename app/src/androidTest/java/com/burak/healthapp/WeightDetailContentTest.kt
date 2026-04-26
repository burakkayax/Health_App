package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.ui.model.BmiGaugeState
import com.burak.healthapp.ui.model.WeightDetailUiState
import com.burak.healthapp.ui.model.WeightHistoryItemState
import com.burak.healthapp.ui.theme.HealthTheme
import com.burak.healthapp.ui.weightdetail.WeightDetailContent
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class WeightDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun weightDetail_rendersChartHistoryAndBmi() {
        composeRule.setContent {
            HealthTheme {
                WeightDetailContent(
                    state = sampleState(),
                    onDeleteMeasurement = {},
                )
            }
        }

        composeRule.onNodeWithTag("weight_detail_chart_card").assertIsDisplayed()
        composeRule.onNodeWithTag("weight_detail_bmi_card").assertIsDisplayed()
        composeRule.onNodeWithText("Girilenler Geçmişi").assertIsDisplayed()
        composeRule.onNodeWithText("24.5 • Normal").assertIsDisplayed()
    }

    @Test
    fun weightDetail_deleteAction_invokesCallback() {
        var deletedId = 0L

        composeRule.setContent {
            HealthTheme {
                WeightDetailContent(
                    state = sampleState(),
                    onDeleteMeasurement = { deletedId = it },
                )
            }
        }

        composeRule.onNodeWithTag("weight_history_delete_2").performClick()
        assertEquals(2L, deletedId)
    }

    @Test
    fun weightDetail_showsBmiHelperWhenHeightMissing() {
        composeRule.setContent {
            HealthTheme {
                WeightDetailContent(
                    state = sampleState().copy(
                        bmiGauge = BmiGaugeState(helperMessage = "VKİ için boyunu profilinden ekle."),
                    ),
                    onDeleteMeasurement = {},
                )
            }
        }

        composeRule.onNodeWithTag("weight_detail_bmi_helper").assertIsDisplayed()
        composeRule.onNodeWithText("VKİ için boyunu profilinden ekle.").assertIsDisplayed()
    }

    private fun sampleState(): WeightDetailUiState {
        return WeightDetailUiState(
            chartPoints = listOf(
                TrendPoint("18 Nis", 77.8f),
                TrendPoint("19 Nis", 77.4f),
            ),
            historyItems = listOf(
                WeightHistoryItemState(id = 2, dateLabel = "19 Nisan 2026", weightLabel = "77.4 kg"),
                WeightHistoryItemState(id = 1, dateLabel = "18 Nisan 2026", weightLabel = "77.8 kg"),
            ),
            bmiGauge = BmiGaugeState(
                indicatorFraction = 0.38f,
                valueLabel = "24.5 • Normal",
            ),
        )
    }
}
