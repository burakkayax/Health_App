package com.burak.healthapp.domain.model

import com.burak.healthapp.domain.config.DefaultHealthGoals
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
