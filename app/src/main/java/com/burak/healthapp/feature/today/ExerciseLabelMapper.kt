package com.burak.healthapp.feature.today

import com.burak.healthapp.R
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType

val ExerciseType.labelResId: Int
    get() = when (this) {
        ExerciseType.WEIGHTS -> R.string.exercise_type_weights
        ExerciseType.RUN -> R.string.exercise_type_run
        ExerciseType.WALK -> R.string.exercise_type_walk
        ExerciseType.BIKE -> R.string.exercise_type_bike
        ExerciseType.YOGA -> R.string.exercise_type_yoga
    }

val ExerciseIntensity.labelResId: Int
    get() = when (this) {
        ExerciseIntensity.LOW -> R.string.exercise_intensity_low
        ExerciseIntensity.MEDIUM -> R.string.exercise_intensity_medium
        ExerciseIntensity.HIGH -> R.string.exercise_intensity_high
    }

fun ExerciseType.toUiText(): UiText = UiText.StringResource(labelResId)

fun ExerciseIntensity.toUiText(): UiText = UiText.StringResource(labelResId)
