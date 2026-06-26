@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.SleepEntry
import com.saglik.core.model.SleepQuality
import kotlinx.datetime.Instant

internal fun sleepEntry(
    id: String,
    start: String,
    end: String,
    durationMinutes: Int,
    quality: SleepQuality? = SleepQuality.GOOD,
): SleepEntry =
    SleepEntry(
        id = id,
        startTime = Instant.parse(start),
        endTime = Instant.parse(end),
        durationMinutes = durationMinutes,
        quality = quality,
        source = DataSource.MANUAL,
        note = null,
    )
