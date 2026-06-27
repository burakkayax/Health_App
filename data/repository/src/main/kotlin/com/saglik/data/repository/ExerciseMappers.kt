package com.saglik.data.repository

import com.saglik.core.database.entity.ExerciseSessionEntity
import com.saglik.core.model.DataSource
import com.saglik.core.model.ExerciseSession
import com.saglik.core.model.ExerciseType

internal fun ExerciseSessionEntity.toDomain(): ExerciseSession =
    ExerciseSession(
        id = id,
        startTimeMillis = startTime,
        endTimeMillis = endTime,
        durationMinutes = durationMinutes,
        exerciseType = runCatching { enumValueOf<ExerciseType>(exerciseType) }
            .getOrDefault(ExerciseType.OTHER),
        title = title,
        notes = notes,
        source = runCatching { enumValueOf<DataSource>(source) }.getOrDefault(DataSource.MANUAL),
        sourceRecordId = sourceRecordId,
        sourcePackageName = sourcePackageName,
        sourceAppName = sourceAppName,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastSyncedAt = lastSyncedAt,
        deletedAt = deletedAt,
    )
