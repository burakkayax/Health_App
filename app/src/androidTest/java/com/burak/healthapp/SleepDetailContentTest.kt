package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.sleep.SleepBarState
import com.burak.healthapp.feature.detail.sleep.SleepCalendarDayState
import com.burak.healthapp.feature.detail.sleep.SleepCalendarWeekState
import com.burak.healthapp.feature.detail.sleep.SleepDetailUiState
import com.burak.healthapp.feature.detail.sleep.SleepRegularityState
import com.burak.healthapp.feature.detail.sleep.SleepRegularityStatus
import com.burak.healthapp.feature.detail.sleep.SleepDetailContent
import com.burak.healthapp.core.ui.theme.HealthTheme
import org.junit.Rule
import org.junit.Test

class SleepDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun weeklySleepDetail_rendersBarChart() {
        composeRule.setContent {
            HealthTheme {
                SleepDetailContent(
                    state = sampleState(period = TrendsPeriod.WEEKLY),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onNodeWithTag("sleep_detail_chart").assertIsDisplayed()
    }

    @Test
    fun monthlySleepDetail_rendersCalendarGrid() {
        composeRule.setContent {
            HealthTheme {
                SleepDetailContent(
                    state = sampleState(period = TrendsPeriod.MONTHLY),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onNodeWithTag("sleep_month_calendar").assertIsDisplayed()
    }

    private fun sampleState(period: TrendsPeriod): SleepDetailUiState {
        return SleepDetailUiState(
            selectedPeriod = period,
            bars = listOf(
                SleepBarState("P", 0.8f, "6s 30d"),
                SleepBarState("S", 1f, "8s 0d"),
            ),
            regularity = SleepRegularityState(
                title = "Düzenli",
                subtitle = "Son 7 günde uyku saatlerin stabil.",
                helperLabel = "Hedefe yakın uyudun.",
                progress = 1f,
                status = SleepRegularityStatus.REGULAR,
            ),
            hasData = true,
            targetLabel = "8s 0d",
            bedtimeLabel = "23:00",
            wakeLabel = "07:00",
            calendarWeeks = listOf(
                SleepCalendarWeekState(
                    days = listOf(
                        SleepCalendarDayState("1", 1f, hasData = true, isInCurrentMonth = true, isTargetMet = true),
                        SleepCalendarDayState("2", 0.5f, hasData = true, isInCurrentMonth = true, isTargetMet = false),
                        SleepCalendarDayState("3", 0f, hasData = false, isInCurrentMonth = true, isTargetMet = false),
                        SleepCalendarDayState("4", 0f, hasData = false, isInCurrentMonth = true, isTargetMet = false),
                        SleepCalendarDayState("5", 0f, hasData = false, isInCurrentMonth = true, isTargetMet = false),
                        SleepCalendarDayState("6", 0f, hasData = false, isInCurrentMonth = true, isTargetMet = false),
                        SleepCalendarDayState("7", 0f, hasData = false, isInCurrentMonth = true, isTargetMet = false),
                    ),
                ),
            ),
        )
    }
}
