package com.burak.healthapp.feature.detail.mealhistory

import com.burak.healthapp.domain.model.MealType

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
