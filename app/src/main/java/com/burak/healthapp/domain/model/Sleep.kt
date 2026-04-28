package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class SleepSession(
    val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    val sessionDate: LocalDate
        get() = endTime.toLocalDate()
}
