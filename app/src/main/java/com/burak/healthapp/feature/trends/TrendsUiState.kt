package com.burak.healthapp.feature.trends

import com.burak.healthapp.core.ui.model.WeeklyCalorieBarState
import com.burak.healthapp.core.ui.model.WeightTrendChartState
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod

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

data class WeightTrendChartCardState(
    val title: String,
    val subtitle: String,
    val chart: WeightTrendChartState,
)

data class TrendsUiState(
    val avatarInitials: String,
    val selectedPeriod: TrendsPeriod,
    val insights: List<InsightCardState>,
    val weeklyCaloriesCard: WeeklyCaloriesCardState?,
    val charts: List<TrendChartState>,
    val weightChart: WeightTrendChartCardState? = null,
)
