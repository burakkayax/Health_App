package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.model.WeeklyCalorieBarState
import com.burak.healthapp.core.ui.model.buildWeightTrendChartState
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.trends.InsightCardState
import com.burak.healthapp.feature.trends.TrendChartState
import com.burak.healthapp.feature.trends.TrendsContent
import com.burak.healthapp.feature.trends.TrendsUiState
import com.burak.healthapp.feature.trends.WeeklyCaloriesCardState
import com.burak.healthapp.feature.trends.WeightTrendChartCardState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TrendsContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun monthlySegment_invokesSelectionCallback() {
        var selectedPeriod = TrendsPeriod.WEEKLY

        composeRule.setContent {
            HealthTheme {
                TrendsContent(
                    state = sampleTrendsState(),
                    onSelectPeriod = { selectedPeriod = it },
                )
            }
        }

        composeRule.onNodeWithTag("segment_aylık").performClick()
        assertEquals(TrendsPeriod.MONTHLY, selectedPeriod)
    }

    @Test
    fun weeklyCaloriesCard_isRendered() {
        composeRule.setContent {
            HealthTheme {
                TrendsContent(
                    state = sampleTrendsState(),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onNodeWithTag("weekly_calories_card").assertIsDisplayed()
        composeRule.onNodeWithText("Haftalık Kalori").assertIsDisplayed()
    }

    @Test
    fun trends_showWeightOnly_andNoRatioCards() {
        composeRule.setContent {
            HealthTheme {
                TrendsContent(
                    state = sampleTrendsState(),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onNodeWithText("Kilo Trendi").assertIsDisplayed()
        composeRule.onAllNodesWithText("Omuz-Bel Oranı").assertCountEquals(0)
        composeRule.onAllNodesWithText("Bel-Kalça Oranı").assertCountEquals(0)
        composeRule.onNodeWithText("Ortalama Su").assertIsDisplayed()
    }

    private fun sampleTrendsState(): TrendsUiState = TrendsUiState(
        avatarInitials = "B",
        selectedPeriod = TrendsPeriod.WEEKLY,
        insights = listOf(
            InsightCardState(
                title = UiText.DynamicString("Günlük Ortalama Protein"),
                value = UiText.DynamicString("128 g"),
                subtitle = UiText.DynamicString("Bu hafta ortalaması"),
                hasData = true,
            ),
            InsightCardState(
                title = UiText.DynamicString("Ortalama Su"),
                value = UiText.DynamicString("1900 ml"),
                subtitle = UiText.DynamicString("Bu hafta günlük ortalama"),
                hasData = true,
            ),
            InsightCardState(
                title = UiText.DynamicString("Ortalama Uyku"),
                value = UiText.DynamicString("7s 10d"),
                subtitle = UiText.DynamicString("Tamamlanan uyku kayıtları baz alınır"),
                hasData = true,
            ),
        ),
        weeklyCaloriesCard = WeeklyCaloriesCardState(
            averageCaloriesLabel = UiText.DynamicString("1980 kcal"),
            subtitle = UiText.DynamicString("Pazartesi - Pazar ortalaması"),
            bars = listOf(
                WeeklyCalorieBarState("P", 2200, 1f),
                WeeklyCalorieBarState("S", 1900, 0.86f),
                WeeklyCalorieBarState("Ç", 2050, 0.93f),
                WeeklyCalorieBarState("P", 1800, 0.82f),
                WeeklyCalorieBarState("C", 2100, 0.95f),
                WeeklyCalorieBarState("C", 2000, 0.91f),
                WeeklyCalorieBarState("P", 1750, 0.8f),
            ),
        ),
        charts = listOf(
            TrendChartState(
                title = UiText.DynamicString("Adım Trendi"),
                subtitle = UiText.DynamicString("Günlük hedefe göre adım akışı."),
                points = listOf(
                    TrendPoint("Pzt", 4200f),
                    TrendPoint("Sal", 5400f),
                    TrendPoint("Çrş", 6100f),
                ),
            ),
        ),
        weightChart = WeightTrendChartCardState(
            title = UiText.DynamicString("Kilo Trendi"),
            subtitle = UiText.DynamicString("Başlangıç, hedef ve mevcut kilo birlikte izlenir."),
            chart = buildWeightTrendChartState(
                points = listOf(
                    TrendPoint("Pzt", 78f),
                    TrendPoint("Sal", 77.8f),
                    TrendPoint("Çrş", 77.4f),
                ),
                targetWeightKg = 74f,
            )!!,
        ),
    )
}
