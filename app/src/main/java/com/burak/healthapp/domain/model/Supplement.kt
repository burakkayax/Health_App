package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class SupplementTemplate(
    val id: Long = 0,
    val name: String,
    val targetAmount: Float,
    val unitLabel: String,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
)

data class SupplementDoseEntry(
    val id: Long = 0,
    val templateId: Long,
    val date: LocalDate,
    val amount: Float,
    val loggedAt: LocalDateTime = LocalDateTime.now(),
)
