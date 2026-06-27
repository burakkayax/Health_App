package com.saglik.data.repository

import com.saglik.core.database.dao.ExerciseDao
import com.saglik.core.database.entity.ExerciseSessionEntity
import com.saglik.core.model.DataSource
import com.saglik.domain.repository.AddExerciseSessionInput
import com.saglik.domain.repository.ExerciseRepository
import java.util.UUID
import kotlinx.coroutines.flow.map

class DefaultExerciseRepository(
    private val exerciseDao: ExerciseDao,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) : ExerciseRepository {
    override fun observeExerciseSessions() =
        exerciseDao.observeExerciseSessions().map { sessions -> sessions.map { it.toDomain() } }

    override fun observeExerciseSessionsBetween(
        startInclusive: Long,
        endExclusive: Long,
    ) = exerciseDao.observeExerciseSessionsBetween(
        startInclusive = startInclusive,
        endExclusive = endExclusive,
    ).map { sessions -> sessions.map { it.toDomain() } }

    override suspend fun addExerciseSession(input: AddExerciseSessionInput) {
        exerciseDao.insertExerciseSession(input.toManualEntity())
    }

    private fun AddExerciseSessionInput.toManualEntity(): ExerciseSessionEntity {
        val durationMinutes = ((endTimeMillis - startTimeMillis) / MillisPerMinute).toInt()
        val syncMetadata = manualSyncMetadata(endTimeMillis)

        return ExerciseSessionEntity(
            id = idFactory(),
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            durationMinutes = durationMinutes,
            exerciseType = exerciseType.name,
            title = title,
            notes = notes,
            source = DataSource.MANUAL.name,
            sourceRecordId = syncMetadata.sourceRecordId,
            sourcePackageName = syncMetadata.sourcePackageName,
            sourceAppName = syncMetadata.sourceAppName,
            createdAt = syncMetadata.createdAt,
            updatedAt = syncMetadata.updatedAt,
            lastSyncedAt = syncMetadata.lastSyncedAt,
            deletedAt = syncMetadata.deletedAt,
        )
    }

    private companion object {
        private const val MillisPerMinute = 60_000L
    }
}
