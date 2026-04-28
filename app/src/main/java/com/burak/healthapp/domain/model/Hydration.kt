package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class HydrationEntry(
    val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
