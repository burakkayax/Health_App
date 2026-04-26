package com.burak.healthapp.feature.trends

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.core.ui.model.WeeklyCalorieBarState

data class InsightCardState(
    val title: String,
    val value: String,
    val subtitle: String,
)

data class WeeklyCaloriesCardState(
    val averageCaloriesLabel: String,
    val subtitle: String,
    val bars: List<WeeklyCalorieBarState>,
)

data class TrendChartState(
    val title: String,
    val subtitle: String,
    val points: List<TrendPoint>,
)

data class TrendsUiState(
    val avatarInitials: String,
    val selectedPeriod: TrendsPeriod,
    val insights: List<InsightCardState>,
    val weeklyCaloriesCard: WeeklyCaloriesCardState?,
    val charts: List<TrendChartState>,
)
