package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
