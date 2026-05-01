package com.burak.healthapp.feature.detail.step

import androidx.compose.runtime.Immutable
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.domain.model.TrendsPeriod

@Immutable
data class StepBarState(
    val label: String,
    val steps: Int,
    val progress: Float,
)

@Immutable
data class StepDetailUiState(
    val selectedPeriod: TrendsPeriod,
    val isLoading: Boolean = false,
    val bars: List<StepBarState>,
    val monthDays: List<MetricDayRingState>,
    val totalSteps: Int,
    val averageSteps: Int,
    val targetSteps: Int,
    val hasData: Boolean,
    val stepTrackingEnabled: Boolean,
)
