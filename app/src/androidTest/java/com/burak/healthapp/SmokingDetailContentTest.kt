package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.smoking.SmokingDayBarState
import com.burak.healthapp.feature.detail.smoking.SmokingDetailContent
import com.burak.healthapp.feature.detail.smoking.SmokingDetailUiState
import com.burak.healthapp.feature.detail.smoking.SmokingHistoryItemState
import com.burak.healthapp.feature.today.SmokingStatus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class SmokingDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun expandedSmokingDetail_rendersTwoPaneAndEntryList() {
        composeRule.setContent {
            HealthTheme {
                SmokingDetailContent(
                    state = sampleState(),
                    onSelectPeriod = {},
                    onDelete = {},
                    windowSizeClass = HealthWindowSizeClass.EXPANDED,
                )
            }
        }

        composeRule.onNodeWithTag("smoking_detail_adaptive_two_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("smoking_detail_entry_list").assertIsDisplayed()
        composeRule.onNodeWithTag("smoking_entry_delete_2026-04-30").assertIsDisplayed()
    }

    @Test
    fun smokingDeleteButton_triggersDeleteCallback() {
        var deletedDate: LocalDate? = null
        composeRule.setContent {
            HealthTheme {
                SmokingDetailContent(
                    state = sampleState(),
                    onSelectPeriod = {},
                    onDelete = { deletedDate = it },
                )
            }
        }

        composeRule.onNodeWithTag("smoking_entry_delete_2026-04-30").performClick()

        composeRule.runOnIdle {
            assertEquals(LocalDate.of(2026, 4, 30), deletedDate)
        }
    }

    private fun sampleState(): SmokingDetailUiState = SmokingDetailUiState(
        selectedPeriod = TrendsPeriod.WEEKLY,
        bars = listOf(
            SmokingDayBarState(LocalDate.of(2026, 4, 29), count = 0, progress = 0f, status = SmokingStatus.SAFE),
            SmokingDayBarState(LocalDate.of(2026, 4, 30), count = 3, progress = 1f, status = SmokingStatus.DANGER),
        ),
        averageCount = 1,
        totalCount = 3,
        limit = 2,
        loggedDays = 1,
        entries = listOf(
            SmokingHistoryItemState(
                date = LocalDate.of(2026, 4, 30),
                dateLabel = "30 Nisan 2026",
                count = 3,
            ),
        ),
        hasPeriodData = true,
    )
}
