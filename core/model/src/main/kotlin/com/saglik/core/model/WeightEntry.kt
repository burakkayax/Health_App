@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.core.model

import kotlinx.datetime.Instant

data class WeightEntry(
    val id: String,
    val weightKg: Float,
    val recordedAt: Instant,
    val source: DataSource,
    val note: String?,
)
