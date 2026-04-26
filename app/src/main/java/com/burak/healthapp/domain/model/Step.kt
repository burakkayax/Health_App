package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class StepEntry(
    val id: Long = 0,
    val date: LocalDate,
    val steps: Int,
    val sensorBaseline: Int? = null,
    val lastSensorValue: Int? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
