package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.CaffeineDrinkSize
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.feature.detail.caffeine.CaffeineBarState
import com.burak.healthapp.feature.detail.caffeine.CaffeineDetailContent
import com.burak.healthapp.feature.detail.caffeine.CaffeineDetailUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class CaffeineDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun expandedCaffeineDetail_usesTwoPaneLayoutAndShowsEntries() {
        composeRule.setContent {
            HealthTheme {
                CaffeineDetailContent(
                    state = sampleState(),
                    windowSizeClass = HealthWindowSizeClass.EXPANDED,
                    onSelectPeriod = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithTag("caffeine_detail_adaptive_two_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("caffeine_detail_entry_list").assertIsDisplayed()
        composeRule.onNodeWithText("Filtre kahve").assertIsDisplayed()
    }

    @Test
    fun caffeineEntryDeleteButton_triggersDeleteCallback() {
        var deletedId: Long? = null
        composeRule.setContent {
            HealthTheme {
                CaffeineDetailContent(
                    state = sampleState(),
                    onSelectPeriod = {},
                    onDelete = { deletedId = it },
                )
            }
        }

        composeRule.onNodeWithTag("caffeine_entry_delete_1").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertEquals(1L, deletedId)
        }
    }

    private fun sampleState(): CaffeineDetailUiState = CaffeineDetailUiState(
        entries = listOf(
            CaffeineEntry(
                id = 1,
                date = LocalDate.of(2026, 4, 30),
                time = LocalTime.of(10, 30),
                drinkType = CaffeineDrinkType.FILTER_COFFEE,
                size = CaffeineDrinkSize.MEDIUM,
                estimatedMg = 140,
                customName = "Filtre kahve",
                createdAt = LocalDateTime.of(2026, 4, 30, 10, 30),
            ),
        ),
        bars = listOf(
            CaffeineBarState(label = "Pzt", totalMg = 0, progress = 0f, date = LocalDate.of(2026, 4, 29)),
            CaffeineBarState(label = "Sal", totalMg = 140, progress = 0.46f, date = LocalDate.of(2026, 4, 30)),
        ),
        totalTodayMg = 140,
        periodAverageMg = 20,
        lastTimeLabel = "10:30",
        limitMg = 300,
        hasPeriodData = true,
    )
}
