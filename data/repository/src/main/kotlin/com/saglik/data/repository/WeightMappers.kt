@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.data.repository

import com.saglik.core.database.entity.WeightEntryEntity
import com.saglik.core.model.DataSource
import com.saglik.core.model.WeightEntry
import kotlinx.datetime.Instant

internal fun WeightEntryEntity.toDomain(): WeightEntry =
    WeightEntry(
        id = id,
        weightKg = weightKg,
        recordedAt = Instant.fromEpochMilliseconds(recordedAt),
        source = runCatching { enumValueOf<DataSource>(source) }.getOrDefault(DataSource.MANUAL),
        note = note,
    )

internal fun WeightEntry.toEntity(): WeightEntryEntity =
    WeightEntryEntity(
        id = id,
        weightKg = weightKg,
        recordedAt = recordedAt.toEpochMilliseconds(),
        source = source.name,
        note = note,
    )
