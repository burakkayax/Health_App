package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.hydration.HydrationDetailContent
import com.burak.healthapp.feature.detail.hydration.HydrationDetailUiState
import com.burak.healthapp.feature.detail.hydration.HydrationHistoryItemState
import com.burak.healthapp.feature.detail.hydration.HydrationSummaryDayState
import org.junit.Rule
import org.junit.Test

class HydrationDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun weeklyHydrationDetail_usesBarChartAndOmitsTotalInsight() {
        composeRule.setContent {
            HealthTheme {
                HydrationDetailContent(
                    state = sampleWeeklyState(),
                    onSelectPeriod = {},
                    onDeleteHydration = {},
                )
            }
        }

        composeRule.onNodeWithTag("hydration_week_bar_chart").assertIsDisplayed()
        composeRule.onNodeWithTag("hydration_week_bar_0").assertIsDisplayed()
        composeRule.onNodeWithText("Ortalama Su").assertIsDisplayed()
        composeRule.onAllNodesWithText("Toplam Su").assertCountEquals(0)
    }

    @Test
    fun monthlyHydrationDetail_keepsMonthRingGrid() {
        composeRule.setContent {
            HealthTheme {
                HydrationDetailContent(
                    state = sampleWeeklyState().copy(
                        selectedPeriod = TrendsPeriod.MONTHLY,
                        monthDays = (1..7).map { day ->
                            MetricDayRingState(
                                dayLabel = day.toString(),
                                progress = if (day == 3) 1f else 0f,
                                hasData = day == 3,
                                isInCurrentMonth = true,
                                isTargetMet = day == 3,
                            )
                        },
                    ),
                    onSelectPeriod = {},
                    onDeleteHydration = {},
                )
            }
        }

        composeRule.onNodeWithTag("hydration_month_ring_grid").assertIsDisplayed()
    }

    private fun sampleWeeklyState(): HydrationDetailUiState {
        return HydrationDetailUiState(
            selectedPeriod = TrendsPeriod.WEEKLY,
            targetMl = 2_500,
            totalMl = 1_600,
            averageMl = 1_200,
            progress = 0.64f,
            entries = listOf(
                HydrationHistoryItemState(id = 1, amountMl = 500, timeLabel = "09:00"),
            ),
            periodDays = listOf(
                HydrationSummaryDayState("P", 0, 0f),
                HydrationSummaryDayState("S", 800, 0.32f),
                HydrationSummaryDayState("Ç", 1_200, 0.48f),
                HydrationSummaryDayState("P", 1_600, 0.64f),
                HydrationSummaryDayState("C", 2_500, 1f),
                HydrationSummaryDayState("C", 1_000, 0.4f),
                HydrationSummaryDayState("P", 500, 0.2f),
            ),
            monthDays = emptyList(),
            hasPeriodData = true,
        )
    }
}
