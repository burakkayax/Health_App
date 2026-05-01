package com.burak.healthapp.feature.trends

import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod

data class TrendsUiState(
    val avatarInitials: String,
    val selectedPeriod: TrendsPeriod,
    val summary: PeriodSummaryState,
    val highlights: List<TrendHighlightState>,
    val goalAdherence: List<GoalAdherenceState>,
    val metricCards: List<MetricTrendCardState>,
    val insights: List<ShortInsightState>,
    val dataQuality: List<DataQualityWarningState>,
    val isLoading: Boolean = false,
)

data class PeriodSummaryState(
    val title: UiText,
    val body: UiText,
    val periodLabel: UiText,
    val hasData: Boolean,
)

data class TrendHighlightState(
    val title: UiText,
    val value: UiText,
    val description: UiText,
    val tone: TrendTone,
)

data class GoalAdherenceState(
    val metric: TrendsMetric,
    val label: UiText,
    val completedDays: Int,
    val totalDays: Int,
    val progress: Float,
    val tone: TrendTone,
)

data class MetricTrendCardState(
    val metric: TrendsMetric,
    val title: UiText,
    val primaryValue: UiText,
    val secondaryValue: UiText,
    val changeLabel: UiText,
    val chartPoints: List<TrendPoint>,
    val tone: TrendTone,
    val destination: TrendsDetailDestination?,
    val hasData: Boolean,
)

data class ShortInsightState(
    val title: UiText,
    val body: UiText,
    val severity: TrendTone,
)

data class DataQualityWarningState(
    val metric: TrendsMetric,
    val message: UiText,
    val availableDays: Int,
    val expectedDays: Int,
)

enum class TrendsMetric {
    HYDRATION,
    STEPS,
    SLEEP,
    EXERCISE,
    CAFFEINE,
    SMOKING,
    WEIGHT,
    NUTRITION,
}

enum class TrendTone {
    POSITIVE,
    WARNING,
    NEUTRAL,
}

enum class TrendsDetailDestination {
    HYDRATION,
    STEPS,
    SLEEP,
    EXERCISE,
    CAFFEINE,
    SMOKING,
    WEIGHT,
    NUTRITION,
}
