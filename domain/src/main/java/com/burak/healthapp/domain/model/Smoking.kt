package com.burak.healthapp.domain.model

import java.time.LocalDate

data class SmokingEntry(
    val id: Long = 0,
    val date: LocalDate,
    val count: Int,
)
