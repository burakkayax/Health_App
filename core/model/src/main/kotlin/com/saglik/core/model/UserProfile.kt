@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.core.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class UserProfile(
    val id: String,
    val sex: Sex,
    val age: Int?,
    val birthDate: LocalDate?,
    val heightCm: Float,
    val goal: HealthGoal,
    val createdAt: Instant,
    val updatedAt: Instant,
)
