package com.burak.healthapp.feature.detail.step

import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.domain.model.TrendsPeriod

data class StepBarState(
    val label: String,
    val steps: Int,
    val progress: Float,
)

data class StepDetailUiState(
    val selectedPeriod: TrendsPeriod,
    val bars: List<StepBarState>,
    val monthDays: List<MetricDayRingState>,
    val totalStepsLabel: String,
    val averageStepsLabel: String,
    val targetLabel: String,
    val hasData: Boolean,
)
