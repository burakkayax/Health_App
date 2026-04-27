package com.burak.healthapp.feature.today.meal

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
import com.burak.healthapp.domain.validation.HealthInputError

data class MealDraftFoodState(
    val draftId: Long,
    val name: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val nameError: HealthInputError? = null,
    val calorieError: HealthInputError? = null,
    val macroError: HealthInputError? = null,
)

data class MealEditorUiState(
    val mealType: MealType = MealType.BREAKFAST,
    val draftFoods: List<MealDraftFoodState> = emptyList(),
    val canSave: Boolean = false,
)
