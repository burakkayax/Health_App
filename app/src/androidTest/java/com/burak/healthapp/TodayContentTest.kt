package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.defaultDashboardCardConfig
import com.burak.healthapp.feature.today.ExerciseCardState
import com.burak.healthapp.feature.today.HydrationCardState
import com.burak.healthapp.feature.today.MacroRingState
import com.burak.healthapp.feature.today.NutritionCardState
import com.burak.healthapp.feature.today.SleepCardState
import com.burak.healthapp.feature.today.SmokingCardState
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.StepCardState
import com.burak.healthapp.feature.today.SupplementCardState
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.TodayContent
import com.burak.healthapp.feature.today.TodayUiState
import com.burak.healthapp.feature.today.WeightCardState
import com.burak.healthapp.feature.today.components.SupplementsCard
import com.burak.healthapp.feature.today.meal.MealDraftFoodState
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class TodayContentTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun quickWaterAction_invokesExpectedAmount() {
        var addedAmount = 0

        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = { addedAmount = it },
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithText("+200 ml").performClick()
        assertEquals(200, addedAmount)
    }

    @Test
    fun nutritionAddButton_opensFormOnlyMealSheet() {
        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("nutrition_add_button").performClick()

        composeRule.onNodeWithText("Öğün Ekle").assertIsDisplayed()
        composeRule.onNodeWithText("Öğe Adı").assertIsDisplayed()
        composeRule.onNodeWithText("Öğünü Kaydet").assertIsDisplayed()
        composeRule.onAllNodesWithText("Hindi Sandviç").assertCountEquals(0)
    }

    @Test
    fun hydrationCard_removesCustomEntryButton() {
        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onAllNodesWithText("Özel Giriş").assertCountEquals(0)
    }

    @Test
    fun mealHistoryFooter_invokesNavigationCallback() {
        var openedHistory = false

        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = { openedHistory = true },
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("nutrition_history_link").performClick()
        assertEquals(true, openedHistory)
    }

    @Test
    fun nutritionCard_showsKarbLabel() {
        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithText("Karb").assertIsDisplayed()
        composeRule.onAllNodesWithText("Karbonhidrat").assertCountEquals(0)
    }

    @Test
    fun supplementsAddButton_opensDoseSheet_andBodyGoalsCardIsGone() {
        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onAllNodesWithTag("body_goals_card").assertCountEquals(0)
        composeRule.onNodeWithTag("supplements_add_button").performClick()
        composeRule.onNodeWithText("Takviye Dozu Ekle").assertIsDisplayed()
        composeRule.onNodeWithText("D3 Vitamini").assertIsDisplayed()
    }

    @Test
    fun supplementsCard_emptyItemsShowsEmptyText() {
        composeRule.setContent {
            HealthTheme {
                SupplementsCard(
                    items = emptyList(),
                    onAdd = {},
                    onDeleteDose = {},
                )
            }
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.today_empty_supplements)).assertIsDisplayed()
        composeRule.onAllNodesWithTag("supplements_centered_row").assertCountEquals(0)
        composeRule.onAllNodesWithTag("supplements_lazy_row").assertCountEquals(0)
    }

    @Test
    fun supplementsCard_oneOrTwoItemsUseCenteredRow() {
        composeRule.setContent {
            HealthTheme {
                SupplementsCard(
                    items = sampleSupplementItems().take(2),
                    onAdd = {},
                    onDeleteDose = {},
                )
            }
        }

        composeRule.onNodeWithTag("supplements_centered_row").assertIsDisplayed()
        composeRule.onAllNodesWithTag("supplements_lazy_row").assertCountEquals(0)
    }

    @Test
    fun supplementsCard_threeOrMoreItemsUseLazyRow() {
        composeRule.setContent {
            HealthTheme {
                SupplementsCard(
                    items = sampleSupplementItems(),
                    onAdd = {},
                    onDeleteDose = {},
                )
            }
        }

        composeRule.onNodeWithTag("supplements_lazy_row").assertIsDisplayed()
        composeRule.onAllNodesWithTag("supplements_centered_row").assertCountEquals(0)
    }

    @Test
    fun weightCard_invokesDetailCallback() {
        var openedDetail = false

        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = { openedDetail = true },
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("weight_card").performClick()
        assertEquals(true, openedDetail)
    }

    @Test
    fun stepCard_invokesDetailCallback() {
        var openedDetail = false

        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    onOpenStepDetail = { openedDetail = true },
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("today_list").performScrollToNode(hasTestTag("steps_card"))
        composeRule.onNodeWithTag("steps_card").performClick()
        assertEquals(true, openedDetail)
    }

    @Test
    fun smokingQuickAdd_invokesIncrementCallback() {
        var incremented = false

        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = { incremented = true },
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("smoking_quick_add_button").performClick()
        assertEquals(true, incremented)
    }

    @Test
    fun customizeButton_opensDashboardCustomizationSheet() {
        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState(),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("today_list").performScrollToNode(androidx.compose.ui.test.hasTestTag("today_customize_dashboard_button"))
        composeRule.onNodeWithTag("today_customize_dashboard_button").performClick()

        composeRule.onNodeWithTag("dashboard_customization_sheet").assertIsDisplayed()
        composeRule.onNodeWithTag("dashboard_card_switch_HYDRATION").assertIsDisplayed()

        // Verify drag handle has content description
        composeRule.onNodeWithTag("dashboard_drag_handle_0").assertIsDisplayed()
        composeRule.onAllNodes(
            hasContentDescription("Sıralamak için basılı tut")
                .and(hasTestTag("dashboard_drag_handle_0")),
            useUnmergedTree = true,
        ).assertCountEquals(1)

        composeRule.onNodeWithTag("dashboard_customization_sheet_list")
            .performScrollToNode(hasTestTag("dashboard_card_switch_SMOKING"))
        composeRule.onNodeWithTag("dashboard_card_switch_SMOKING").assertIsDisplayed()

        composeRule.onNodeWithTag("dashboard_customization_sheet_list")
            .performScrollToNode(hasTestTag("dashboard_card_switch_SUPPLEMENTS"))
        composeRule.onNodeWithTag("dashboard_card_switch_SUPPLEMENTS").assertIsDisplayed()

        composeRule.onNodeWithTag("dashboard_customization_sheet_list")
            .performScrollToNode(hasTestTag("dashboard_card_switch_STEPS"))
        composeRule.onNodeWithTag("dashboard_card_switch_STEPS").assertIsDisplayed()
    }

    @Test
    fun hiddenDashboardCard_isNotRendered() {
        val hiddenHydration = defaultDashboardCardConfig().map { config ->
            if (config.type == DashboardCardType.HYDRATION) config.copy(isVisible = false) else config
        }

        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState().copy(dashboardCards = hiddenHydration),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onAllNodesWithTag("hydration_card").assertCountEquals(0)
    }

    @Test
    fun allHiddenDashboardCards_showEmptyState() {
        val allHidden = defaultDashboardCardConfig().map { config -> config.copy(isVisible = false) }

        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState().copy(dashboardCards = allHidden),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("dashboard_empty_state").assertIsDisplayed()
    }

    @Test
    fun emptyDataCards_remainRenderableInTodayList() {
        composeRule.setContent {
            HealthTheme {
                TodayContent(
                    state = sampleTodayState().copy(
                        weight = WeightCardState(
                            currentWeightKg = null,
                            targetWeightKg = 74f,
                            progress = 0f,
                            hasMeasurement = false,
                            headline = "Kayıt yok",
                            supportingLabel = "Bu tarih için kilo eklenmedi",
                            helperLabel = "Hedef 74.0 kg",
                        ),
                        exercise = ExerciseCardState(
                            type = null,
                            durationMinutes = 0,
                            intensity = null,
                            progress = 0f,
                            title = "Antrenman eklenmedi",
                            durationLabel = "Süre eklenmedi",
                            intensityLabel = "Yoğunluk seçilmedi",
                            helperLabel = "Bu hafta 0 / hedef 4 gün",
                        ),
                        sleep = SleepCardState(
                            durationLabel = "Henüz kayıt yok",
                            timeRangeLabel = "Saat ekle",
                            targetLabel = "8s 0d",
                            progress = 0f,
                        ),
                        smoking = SmokingCardState(
                            count = 0,
                            limit = 0,
                            progress = 0f,
                            headline = "0 adet",
                            supportingLabel = "Limit ayarlanmadı",
                            helperLabel = "İstersen profilden günlük limit ekleyebilirsin.",
                            status = SmokingStatus.PASSIVE,
                        ),
                        supplements = SupplementCardState(items = emptyList()),
                    ),
                    onAddMeal = { _, _, _, _, _, _ -> },
                    onAddHydration = {},
                    onSaveSleep = { _, _ -> },
                    onSaveWeight = {},
                    onSaveExercise = { _, _, _ -> },
                    onSaveSmokingCount = {},
                    onIncrementSmoking = {},
                    onSaveSupplementDoses = {},
                    onOpenMealHistory = {},
                    onOpenWeightDetail = {},
                    onOpenSleepDetail = {},
                    mealEditorState = sampleMealEditorState(),
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
            }
        }

        composeRule.onNodeWithTag("weight_card").assertIsDisplayed()
        composeRule.onNodeWithTag("today_list").performScrollToNode(hasTestTag("exercise_card"))
        composeRule.onNodeWithTag("exercise_card").assertIsDisplayed()
        composeRule.onNodeWithTag("today_list").performScrollToNode(hasTestTag("sleep_card"))
        composeRule.onNodeWithTag("sleep_card").assertIsDisplayed()
        composeRule.onNodeWithTag("today_list").performScrollToNode(hasTestTag("smoking_card"))
        composeRule.onNodeWithTag("smoking_card").assertIsDisplayed()
        composeRule.onNodeWithTag("today_list").performScrollToNode(hasTestTag("supplements_card"))
        composeRule.onNodeWithTag("supplements_card").assertIsDisplayed()
    }

    private fun sampleSupplementItems(): List<SupplementItemState> = listOf(
        SupplementItemState(1, "D3 Vitamini", 25f, 25f, "mcg", 1f),
        SupplementItemState(2, "Omega 3", 600f, 1000f, "mg", 0.6f),
        SupplementItemState(3, "Magnezyum", 200f, 300f, "mg", 0.66f),
    )

    private fun sampleTodayState(): TodayUiState {
        val goals = GoalSettings()
        val measurement = BodyMeasurementEntry(
            date = LocalDate.of(2026, 4, 19),
            weightKg = 77.4f,
            shoulderCm = 119f,
            waistCm = 86f,
            hipCm = 98f,
        )
        return TodayUiState(
            userName = "Burak",
            avatarInitials = "B",
            goalSettings = goals,
            latestMeasurement = measurement,
            nutrition = NutritionCardState(
                currentCalories = 1640,
                targetCalories = 2200,
                progress = 0.74f,
                macros = listOf(
                    MacroRingState("Karb", 145, 220, 0.66f),
                    MacroRingState("YaÄŸ", 52, 70, 0.74f),
                    MacroRingState("Protein", 128, 160, 0.8f, isEmphasized = true),
                ),
                entries = listOf(
                    MealEntry(
                        id = 1,
                        date = LocalDate.of(2026, 4, 19),
                        mealType = MealType.LUNCH,
                        name = "Hindi Sandviç",
                        calories = 540,
                        carbsGrams = 44,
                        fatGrams = 18,
                        proteinGrams = 34,
                    ),
                ),
            ),
            weight = WeightCardState(
                currentWeightKg = 77.4f,
                targetWeightKg = 74f,
                progress = 0.4f,
                hasMeasurement = true,
                headline = "77.4 kg",
                supportingLabel = "Hedef 74.0 kg",
                helperLabel = "Başlangıç 78.0 kg",
            ),
            exercise = ExerciseCardState(
                type = ExerciseType.WEIGHTS,
                durationMinutes = 45,
                intensity = ExerciseIntensity.MEDIUM,
                progress = 1f,
                title = "Ağırlık",
                durationLabel = "45 dk",
                intensityLabel = "Orta yoÄŸunluk",
                helperLabel = "Bu hafta 2 / hedef 4 gün",
            ),
            hydration = HydrationCardState(
                currentMl = 1600,
                targetMl = 2500,
                progress = 0.64f,
            ),
            sleep = SleepCardState(
                durationLabel = "7s 15d",
                timeRangeLabel = "23:45 - 07:00",
                targetLabel = "8s 0d",
                progress = 0.91f,
            ),
            smoking = SmokingCardState(
                count = 1,
                limit = 3,
                progress = 0.33f,
                headline = "1 adet",
                supportingLabel = "Günlük limit 3 adet",
                helperLabel = "Limit aşılmadı",
                status = SmokingStatus.WARNING,
            ),
            steps = StepCardState(
                currentSteps = 4200,
                targetSteps = 8000,
                progress = 0.52f,
                headline = "4200 adım",
                supportingLabel = "Hedef 8000 adım",
                helperLabel = "Bu hafta 18000 adım",
            ),
            supplements = SupplementCardState(
                items = listOf(
                    SupplementItemState(1, "D3 Vitamini", 25f, 25f, "mcg", 1f),
                    SupplementItemState(2, "Omega 3", 600f, 1000f, "mg", 0.6f),
                ),
            ),
        )
    }

    private fun sampleMealEditorState(): MealEditorUiState = MealEditorUiState(
        mealType = MealType.BREAKFAST,
        draftFoods = listOf(
            MealDraftFoodState(draftId = 1L),
        ),
        canSave = false,
    )
}
