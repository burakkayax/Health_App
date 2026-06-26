@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.core.model

import kotlinx.datetime.Instant

data class SleepEntry(
    val id: String,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int,
    val quality: SleepQuality?,
    val source: DataSource,
    val note: String?,
)
