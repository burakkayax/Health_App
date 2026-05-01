package com.burak.healthapp.feature.detail.weight

import com.burak.healthapp.core.ui.model.BmiGaugeState
import com.burak.healthapp.core.ui.model.WeightTrendChartState
import com.burak.healthapp.domain.model.TrendPoint

data class WeightHistoryItemState(
    val id: Long,
    val dateLabel: String,
    val weightLabel: String,
)

data class WeightDetailUiState(
    val chartPoints: List<TrendPoint>,
    val weightChart: WeightTrendChartState? = null,
    val historyItems: List<WeightHistoryItemState>,
    val bmiGauge: BmiGaugeState,
    val isLoading: Boolean = false,
)
