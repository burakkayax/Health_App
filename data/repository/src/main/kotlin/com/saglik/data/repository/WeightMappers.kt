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

internal fun WeightEntry.toEntity(): WeightEntryEntity {
    val recordedAtEpochMillis = recordedAt.toEpochMilliseconds()
    val syncMetadata = manualSyncMetadata(recordedAtEpochMillis)

    return WeightEntryEntity(
        id = id,
        weightKg = weightKg,
        recordedAt = recordedAtEpochMillis,
        source = source.name,
        note = note,
        sourceRecordId = syncMetadata.sourceRecordId,
        sourcePackageName = syncMetadata.sourcePackageName,
        sourceAppName = syncMetadata.sourceAppName,
        createdAt = syncMetadata.createdAt,
        updatedAt = syncMetadata.updatedAt,
        lastSyncedAt = syncMetadata.lastSyncedAt,
        deletedAt = syncMetadata.deletedAt,
    )
}
