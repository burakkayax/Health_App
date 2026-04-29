package com.burak.healthapp.domain.model

import java.time.LocalDate

enum class ExerciseType(val label: String) {
    WEIGHTS("Ağırlık"),
    RUN("KoÅŸu"),
    WALK("Yürüyüş"),
    BIKE("Bisiklet"),
    YOGA("Yoga"),
}

enum class ExerciseIntensity(val label: String) {
    LOW("Düşük"),
    MEDIUM("Orta"),
    HIGH("Yüksek"),
}

data class ExerciseEntry(
    val id: Long = 0,
    val date: LocalDate,
    val type: ExerciseType,
    val durationMinutes: Int,
    val intensity: ExerciseIntensity,
)
