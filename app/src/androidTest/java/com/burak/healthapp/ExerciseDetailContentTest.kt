package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.exercise.ExerciseDayBarState
import com.burak.healthapp.feature.detail.exercise.ExerciseDetailContent
import com.burak.healthapp.feature.detail.exercise.ExerciseDetailUiState
import com.burak.healthapp.feature.detail.exercise.ExerciseHistoryItemState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ExerciseDetailContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun expandedExerciseDetail_rendersTwoPaneAndEntryList() {
        composeRule.setContent {
            HealthTheme {
                ExerciseDetailContent(
                    state = sampleState(),
                    onSelectPeriod = {},
                    onDelete = {},
                    windowSizeClass = HealthWindowSizeClass.EXPANDED,
                )
            }
        }

        composeRule.onNodeWithTag("exercise_detail_adaptive_two_pane").assertIsDisplayed()
        composeRule.onNodeWithTag("exercise_detail_entry_list").assertIsDisplayed()
        composeRule.onNodeWithTag("exercise_entry_delete_2026-04-30").assertIsDisplayed()
    }

    @Test
    fun exerciseDeleteButton_triggersDeleteCallback() {
        var deletedDate: LocalDate? = null
        composeRule.setContent {
            HealthTheme {
                ExerciseDetailContent(
                    state = sampleState(),
                    onSelectPeriod = {},
                    onDelete = { deletedDate = it },
                )
            }
        }

        composeRule.onNodeWithTag("exercise_entry_delete_2026-04-30").performClick()

        composeRule.runOnIdle {
            assertEquals(LocalDate.of(2026, 4, 30), deletedDate)
        }
    }

    private fun sampleState(): ExerciseDetailUiState = ExerciseDetailUiState(
        selectedPeriod = TrendsPeriod.WEEKLY,
        bars = listOf(
            ExerciseDayBarState(LocalDate.of(2026, 4, 29), durationMinutes = 0, progress = 0f),
            ExerciseDayBarState(LocalDate.of(2026, 4, 30), durationMinutes = 45, progress = 1f),
        ),
        averageDurationMinutes = 22,
        totalDurationMinutes = 45,
        activeDays = 1,
        entries = listOf(
            ExerciseHistoryItemState(
                date = LocalDate.of(2026, 4, 30),
                dateLabel = "30 Nisan 2026",
                type = ExerciseType.RUN,
                intensity = ExerciseIntensity.MEDIUM,
                durationMinutes = 45,
            ),
        ),
        hasPeriodData = true,
    )
}
