package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class StepEntry(
    val id: Long = 0,
    val date: LocalDate,
    val steps: Int,
    val sensorBaseline: Int? = null,
    val lastSensorValue: Int? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
