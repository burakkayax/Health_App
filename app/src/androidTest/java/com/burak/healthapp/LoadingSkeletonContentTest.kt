package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.app.RootLoadingPlaceholder
import com.burak.healthapp.feature.detail.DetailSkeletonContent
import com.burak.healthapp.feature.today.TodayActions
import com.burak.healthapp.feature.today.TodayContent
import com.burak.healthapp.feature.today.emptyUiState
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import com.burak.healthapp.feature.trends.PeriodSummaryState
import com.burak.healthapp.feature.trends.TrendsContent
import com.burak.healthapp.feature.trends.TrendsUiState
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime

class LoadingSkeletonContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rootLoadingPlaceholder_doesNotRenderLoadingText() {
        composeRule.setContent {
            HealthTheme {
                RootLoadingPlaceholder()
            }
        }

        composeRule.onAllNodesWithText(composeRule.activity.getString(R.string.common_loading)).assertCountEquals(0)
    }

    @Test
    fun todayInitialLoading_rendersSkeleton() {
        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = emptyUiState(isLoading = true),
                    actions = emptyTodayActions(),
                    mealEditorState = MealEditorUiState(),
                )
            }
        }

        composeRule.onNodeWithTag("today_skeleton").assertIsDisplayed()
    }

    @Test
    fun trendsInitialLoading_rendersSkeleton() {
        composeRule.setContent {
            HealthTheme {
                TrendsContent(
                    state = emptyTrendsUiState(isLoading = true),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onNodeWithTag("trends_skeleton").assertIsDisplayed()
    }

    @Test
    fun trendsLoadedEmptyState_rendersGhostEmptyState() {
        composeRule.setContent {
            HealthTheme {
                TrendsContent(
                    state = emptyTrendsUiState(isLoading = false),
                    onSelectPeriod = {},
                )
            }
        }

        composeRule.onNodeWithTag("trends_empty_state").assertIsDisplayed()
        composeRule.onNodeWithTag("trends_empty_ghost").assertIsDisplayed()
    }

    @Test
    fun detailSkeleton_rendersCommonSkeleton() {
        composeRule.setContent {
            HealthTheme {
                DetailSkeletonContent()
            }
        }

        composeRule.onNodeWithTag("detail_skeleton").assertIsDisplayed()
    }

    private fun emptyTodayActions(): TodayActions = TodayActions(
        onAddMeal = { _, _, _, _, _, _ -> },
        onAddHydration = {},
        onSaveSleep = { _: LocalTime, _: LocalTime -> },
        onSaveWeight = {},
        onSaveExercise = { _, _, _ -> },
        onSaveSmokingCount = {},
        onIncrementSmoking = {},
        onSaveSupplementDoses = {},
        onOpenMealHistory = {},
        onOpenWeightDetail = {},
        onOpenSleepDetail = {},
        onMealTypeChange = {},
        onAddMealDraft = {},
        onRemoveMealDraft = {},
        onMealDraftNameChange = { _, _ -> },
        onMealDraftCaloriesChange = { _, _ -> },
        onMealDraftProteinChange = { _, _ -> },
        onMealDraftCarbsChange = { _, _ -> },
        onMealDraftFatChange = { _, _ -> },
        onResetMealEditor = {},
    )

    private fun emptyTrendsUiState(isLoading: Boolean): TrendsUiState = TrendsUiState(
        avatarInitials = "B",
        selectedPeriod = TrendsPeriod.WEEKLY,
        summary = PeriodSummaryState(
            title = UiText.DynamicString("Dönem Özeti"),
            body = UiText.DynamicString("Kayıt ekledikçe dönem özetin burada görünecek."),
            periodLabel = UiText.DynamicString("Son 7 gün"),
            hasData = false,
        ),
        highlights = emptyList(),
        goalAdherence = emptyList(),
        metricCards = emptyList(),
        insights = emptyList(),
        dataQuality = emptyList(),
        isLoading = isLoading,
    )
}
