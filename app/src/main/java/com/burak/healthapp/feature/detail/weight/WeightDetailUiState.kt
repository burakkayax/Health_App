package com.burak.healthapp.feature.detail.weight

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
import com.burak.healthapp.core.ui.model.BmiGaugeState
import com.burak.healthapp.core.ui.model.WeightTrendChartState

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
)
