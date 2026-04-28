package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.model.BmiGaugeState
import com.burak.healthapp.core.ui.model.buildWeightTrendChartState
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.feature.detail.weight.WeightDetailContent
import com.burak.healthapp.feature.detail.weight.WeightDetailUiState
import com.burak.healthapp.feature.detail.weight.WeightHistoryItemState
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
        composeRule.onNodeWithText("Başlangıç 77.8 kg").assertIsDisplayed()
        composeRule.onNodeWithText("Hedef 74.0 kg").assertIsDisplayed()
        composeRule.onNodeWithText("Mevcut 77.4 kg").assertIsDisplayed()
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
        val chartPoints = listOf(
            TrendPoint("18 Nis", 77.8f),
            TrendPoint("19 Nis", 77.4f),
        )
        return WeightDetailUiState(
            chartPoints = chartPoints,
            weightChart = buildWeightTrendChartState(chartPoints, targetWeightKg = 74f),
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
