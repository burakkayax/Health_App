package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class SleepSession(
    val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    val sessionDate: LocalDate
        get() = endTime.toLocalDate()
}
