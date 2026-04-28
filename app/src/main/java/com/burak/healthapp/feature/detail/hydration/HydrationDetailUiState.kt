package com.burak.healthapp.feature.detail.hydration

import androidx.compose.runtime.Immutable
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.domain.model.TrendsPeriod

@Immutable
data class HydrationHistoryItemState(
    val id: Long,
    val amountMl: Int,
    val timeLabel: String,
)

@Immutable
data class HydrationSummaryDayState(
    val label: String,
    val amountMl: Int,
    val progress: Float,
)

@Immutable
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
