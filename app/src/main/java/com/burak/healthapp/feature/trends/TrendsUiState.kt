package com.burak.healthapp.feature.trends

import com.burak.healthapp.core.ui.model.WeeklyCalorieBarState
import com.burak.healthapp.core.ui.model.WeightTrendChartState
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod

data class InsightCardState(
    val title: UiText,
    val value: UiText,
    val subtitle: UiText,
    val hasData: Boolean,
)

data class WeeklyCaloriesCardState(
    val averageCaloriesLabel: UiText,
    val subtitle: UiText,
    val bars: List<WeeklyCalorieBarState>,
)

data class TrendChartState(
    val title: UiText,
    val subtitle: UiText,
    val points: List<TrendPoint>,
)

data class WeightTrendChartCardState(
    val title: UiText,
    val subtitle: UiText,
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
