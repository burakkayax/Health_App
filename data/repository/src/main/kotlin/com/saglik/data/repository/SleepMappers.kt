@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.data.repository

import com.saglik.core.database.entity.SleepEntryEntity
import com.saglik.core.model.DataSource
import com.saglik.core.model.SleepEntry
import com.saglik.core.model.SleepQuality
import kotlinx.datetime.Instant

internal fun SleepEntryEntity.toDomain(): SleepEntry =
    SleepEntry(
        id = id,
        startTime = Instant.fromEpochMilliseconds(startTime),
        endTime = Instant.fromEpochMilliseconds(endTime),
        durationMinutes = durationMinutes,
        quality = quality?.let { raw ->
            runCatching { enumValueOf<SleepQuality>(raw) }.getOrNull()
        },
        source = runCatching { enumValueOf<DataSource>(source) }.getOrDefault(DataSource.MANUAL),
        note = note,
    )

internal fun SleepEntry.toEntity(): SleepEntryEntity =
    SleepEntryEntity(
        id = id,
        startTime = startTime.toEpochMilliseconds(),
        endTime = endTime.toEpochMilliseconds(),
        durationMinutes = durationMinutes,
        quality = quality?.name,
        source = source.name,
        note = note,
    )
