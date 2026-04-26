package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class BodyMeasurementEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float,
    val shoulderCm: Float,
    val waistCm: Float,
    val hipCm: Float,
    val recordedAt: LocalDateTime = LocalDateTime.now(),
)
