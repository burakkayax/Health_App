package com.burak.healthapp.feature.today

import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import java.time.LocalTime

data class TodayActions(
    val onAddMeal: (MealType, String, Int, Int, Int, Int) -> Unit,
    val onAddHydration: (Int) -> Unit,
    val onSaveSleep: (LocalTime, LocalTime) -> Unit,
    val onSaveWeight: (Float) -> Unit,
    val onSaveExercise: (ExerciseType, Int, ExerciseIntensity) -> Unit,
    val onSaveSmokingCount: (Int) -> Unit,
    val onIncrementSmoking: () -> Unit,
    val onSaveSupplementDoses: (List<SupplementDoseEntry>) -> Unit,
    val onDeleteHydration: (Long) -> Unit = {},
    val onDeleteSleep: () -> Unit = {},
    val onDeleteExercise: () -> Unit = {},
    val onDeleteSmoking: () -> Unit = {},
    val onDeleteSupplementDose: (Long) -> Unit = {},
    val onOpenMealHistory: () -> Unit,
    val onOpenWeightDetail: () -> Unit,
    val onOpenSleepDetail: () -> Unit,
    val onOpenStepDetail: () -> Unit = {},
    val onMealTypeChange: (MealType) -> Unit,
    val onAddMealDraft: () -> Unit,
    val onRemoveMealDraft: (Long) -> Unit,
    val onMealDraftNameChange: (Long, String) -> Unit,
    val onMealDraftCaloriesChange: (Long, String) -> Unit,
    val onMealDraftProteinChange: (Long, String) -> Unit,
    val onMealDraftCarbsChange: (Long, String) -> Unit,
    val onMealDraftFatChange: (Long, String) -> Unit,
    val onResetMealEditor: () -> Unit,
)
