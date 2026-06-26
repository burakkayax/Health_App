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

internal fun SleepEntry.toEntity(): SleepEntryEntity {
    val endTimeEpochMillis = endTime.toEpochMilliseconds()
    val syncMetadata = manualSyncMetadata(endTimeEpochMillis)

    return SleepEntryEntity(
        id = id,
        startTime = startTime.toEpochMilliseconds(),
        endTime = endTimeEpochMillis,
        durationMinutes = durationMinutes,
        quality = quality?.name,
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
