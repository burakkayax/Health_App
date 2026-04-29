package com.burak.healthapp.domain.model

import java.time.LocalDate

enum class ExerciseType {
    WEIGHTS,
    RUN,
    WALK,
    BIKE,
    YOGA,
}

enum class ExerciseIntensity {
    LOW,
    MEDIUM,
    HIGH,
}

data class ExerciseEntry(
    val id: Long = 0,
    val date: LocalDate,
    val type: ExerciseType,
    val durationMinutes: Int,
    val intensity: ExerciseIntensity,
)
