package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class BodyMeasurementEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float,
    val shoulderCm: Float,
    val waistCm: Float,
    val hipCm: Float,
    val recordedAt: LocalDateTime = LocalDateTime.now(),
)
