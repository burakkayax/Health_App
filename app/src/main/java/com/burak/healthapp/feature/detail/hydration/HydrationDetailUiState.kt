package com.burak.healthapp.feature.detail.hydration

import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.domain.model.TrendsPeriod

data class HydrationHistoryItemState(
    val id: Long,
    val amountMl: Int,
    val timeLabel: String,
)

data class HydrationSummaryDayState(
    val label: String,
    val amountMl: Int,
    val progress: Float,
)

data class HydrationDetailUiState(
    val selectedPeriod: TrendsPeriod,
    val targetMl: Int,
    val totalMl: Int,
    val averageMl: Int,
    val progress: Float,
    val entries: List<HydrationHistoryItemState>,
    val periodDays: List<HydrationSummaryDayState>,
    val monthDays: List<MetricDayRingState>,
    val hasPeriodData: Boolean,
)
