package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.trends.DataQualityWarningState
import com.burak.healthapp.feature.trends.GoalAdherenceState
import com.burak.healthapp.feature.trends.MetricTrendCardState
import com.burak.healthapp.feature.trends.PeriodSummaryState
import com.burak.healthapp.feature.trends.ShortInsightState
import com.burak.healthapp.feature.trends.TrendHighlightState
import com.burak.healthapp.feature.trends.TrendTone
import com.burak.healthapp.feature.trends.TrendsContent
import com.burak.healthapp.feature.trends.TrendsDetailDestination
import com.burak.healthapp.feature.trends.TrendsMetric
import com.burak.healthapp.feature.trends.TrendsUiState
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
    fun redesignedSections_areRendered() {
        composeRule.setContent {
            HealthTheme {
                TrendsContent(
                    state = sampleTrendsState(),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onNodeWithTag("trends_summary_card").assertIsDisplayed()
        composeRule.onNodeWithTag("trends_highlights_section").assertIsDisplayed()
        composeRule.onNodeWithTag("trends_goal_adherence_section").assertIsDisplayed()
        composeRule.onNodeWithTag("trends_metric_cards_section").assertIsDisplayed()
        composeRule.onNodeWithTag("trends_short_insights_section").assertIsDisplayed()
        composeRule.onNodeWithTag("trends_data_quality_section").assertIsDisplayed()
        composeRule.onNodeWithText("Dönem Özeti").assertIsDisplayed()
        composeRule.onNodeWithText("Hedef Uyumu").assertIsDisplayed()
    }

    @Test
    fun metricCardClick_invokesDetailNavigation() {
        var opened: TrendsDetailDestination? = null

        composeRule.setContent {
            HealthTheme {
                TrendsContent(
                    state = sampleTrendsState(),
                    onSelectPeriod = {},
                    onOpenDetail = { opened = it },
                )
            }
        }

        composeRule.onNodeWithText("Adım").performClick()
        assertEquals(TrendsDetailDestination.STEPS, opened)
    }

    private fun sampleTrendsState(): TrendsUiState = TrendsUiState(
        avatarInitials = "B",
        selectedPeriod = TrendsPeriod.WEEKLY,
        summary = PeriodSummaryState(
            title = UiText.DynamicString("Dönem Özeti"),
            body = UiText.DynamicString("Bu hafta su hedefini 5/7 gün tamamladın."),
            periodLabel = UiText.DynamicString("Son 7 gün"),
            hasData = true,
        ),
        highlights = listOf(
            TrendHighlightState(
                title = UiText.DynamicString("En iyi gelişme"),
                value = UiText.DynamicString("Su"),
                description = UiText.DynamicString("Su hedefin 5/7 gün tamamlanmış."),
                tone = TrendTone.POSITIVE,
            ),
        ),
        goalAdherence = listOf(
            GoalAdherenceState(
                metric = TrendsMetric.HYDRATION,
                label = UiText.DynamicString("Su"),
                completedDays = 5,
                totalDays = 7,
                progress = 5f / 7f,
                tone = TrendTone.POSITIVE,
            ),
        ),
        metricCards = listOf(
            MetricTrendCardState(
                metric = TrendsMetric.STEPS,
                title = UiText.DynamicString("Adım"),
                primaryValue = UiText.DynamicString("8.200 adım"),
                secondaryValue = UiText.DynamicString("Hedef 4/7 gün"),
                changeLabel = UiText.DynamicString("Önceki döneme göre arttı"),
                chartPoints = emptyList(),
                tone = TrendTone.POSITIVE,
                destination = TrendsDetailDestination.STEPS,
                hasData = true,
            ),
        ),
        insights = listOf(
            ShortInsightState(
                title = UiText.DynamicString("Su hedefi"),
                body = UiText.DynamicString("Bu dönem su hedefini 5/7 gün tamamladın."),
                severity = TrendTone.POSITIVE,
            ),
        ),
        dataQuality = listOf(
            DataQualityWarningState(
                metric = TrendsMetric.SLEEP,
                message = UiText.DynamicString("Uyku verin bu dönem 3/7 gün kayıtlı."),
                availableDays = 3,
                expectedDays = 7,
            ),
        ),
    )
}
