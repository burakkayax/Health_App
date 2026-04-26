package com.burak.healthapp.feature.detail.mealhistory

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

data class MealHistoryEntryState(
    val id: Long,
    val mealType: MealType,
    val name: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
)

data class MealHistorySectionState(
    val title: String,
    val entries: List<MealHistoryEntryState>,
)

data class MealHistoryUiState(
    val sections: List<MealHistorySectionState>,
)
