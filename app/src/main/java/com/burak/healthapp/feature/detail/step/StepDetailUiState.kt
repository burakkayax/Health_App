package com.burak.healthapp.feature.detail.step

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

data class StepBarState(
    val label: String,
    val steps: Int,
    val progress: Float,
)

data class StepDetailUiState(
    val selectedPeriod: TrendsPeriod,
    val bars: List<StepBarState>,
    val totalStepsLabel: String,
    val averageStepsLabel: String,
    val targetLabel: String,
    val hasData: Boolean,
)
