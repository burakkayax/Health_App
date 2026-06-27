package com.saglik.data.repository

import com.saglik.core.database.entity.StepsEntryEntity
import com.saglik.core.model.DataSource
import com.saglik.core.model.StepsEntry

internal fun StepsEntryEntity.toDomain(): StepsEntry =
    StepsEntry(
        id = id,
        startTimeMillis = startTime,
        endTimeMillis = endTime,
        count = count.coerceAtLeast(0L),
        source = runCatching { enumValueOf<DataSource>(source) }.getOrDefault(DataSource.MANUAL),
        note = note,
        sourceRecordId = sourceRecordId,
        sourcePackageName = sourcePackageName,
        sourceAppName = sourceAppName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt,
        deletedAt = deletedAt,
    )
