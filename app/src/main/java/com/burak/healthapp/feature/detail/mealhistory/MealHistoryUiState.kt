package com.burak.healthapp.feature.detail.mealhistory

import androidx.annotation.StringRes
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
    @StringRes val titleResId: Int,
    val entries: List<MealHistoryEntryState>,
)

data class MealMacroDistribution(
    val proteinPercent: Int,
    val carbsPercent: Int,
    val fatPercent: Int,
)

data class MealHistoryDailySummary(
    val totalCalories: Int,
    val totalProtein: Int,
    val totalCarbs: Int,
    val totalFat: Int,
    val mealCount: Int,
    val foodCount: Int,
    val macroDistribution: MealMacroDistribution? = null,
)

data class MealHistoryUiState(
    val sections: List<MealHistorySectionState>,
    val dailySummary: MealHistoryDailySummary? = null,
)
