package com.burak.healthapp.domain.model

data class TodaySnapshot(
    val settings: SettingsState,
    val meals: List<MealEntry>,
    val hydrationEntries: List<HydrationEntry>,
    val sleepSessionForDate: SleepSession?,
    val exerciseEntryForDate: ExerciseEntry?,
    val weekExerciseEntries: List<ExerciseEntry>,
    val smokingEntryForDate: SmokingEntry?,
    val stepEntryForDate: StepEntry?,
    val weekStepEntries: List<StepEntry>,
    val caffeineEntries: List<CaffeineEntry>,
    val supplementTemplates: List<SupplementTemplate>,
    val supplementDoseEntries: List<SupplementDoseEntry>,
    val measurementForDate: BodyMeasurementEntry?,
)
