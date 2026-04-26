package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class HydrationEntry(
    val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
