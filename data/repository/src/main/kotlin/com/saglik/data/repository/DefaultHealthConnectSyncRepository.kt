package com.saglik.data.repository

import com.saglik.core.database.dao.ExerciseDao
import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.StepsDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.database.entity.ExerciseSessionEntity
import com.saglik.core.database.entity.SleepEntryEntity
import com.saglik.core.database.entity.StepsEntryEntity
import com.saglik.core.database.entity.WeightEntryEntity
import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.model.DataSource
import com.saglik.core.model.ExerciseType
import com.saglik.core.model.HealthConnectExerciseSessionSnapshot
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectStepsRecordSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot
import com.saglik.domain.repository.HealthConnectSyncRepository
import com.saglik.domain.usecase.HealthConnectImportCount
import java.util.UUID

class DefaultHealthConnectSyncRepository(
    private val dataSource: HealthConnectDataSource,
    private val weightDao: WeightDao,
    private val sleepDao: SleepDao,
    private val stepsDao: StepsDao,
    private val exerciseDao: ExerciseDao,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
) : HealthConnectSyncRepository {

    override suspend fun readWeightRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectWeightRecordSnapshot> =
        dataSource.readWeightRecords(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
        )

    override suspend fun readSleepSessionRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectSleepSessionSnapshot> =
        dataSource.readSleepSessionRecords(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
        )

    override suspend fun readStepsRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectStepsRecordSnapshot> =
        dataSource.readStepsRecords(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
        )

    override suspend fun readExerciseSessionRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectExerciseSessionSnapshot> =
        dataSource.readExerciseSessionRecords(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis,
        )

    override suspend fun importWeightRecords(
        records: List<HealthConnectWeightRecordSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount {
        var inserted = 0
        var updated = 0
        var skipped = 0

        records.forEach { record ->
            if (record.healthConnectId.isBlank()) {
                skipped += 1
                return@forEach
            }

            val existing = weightDao.findByExternalIdentity(
                source = DataSource.HEALTH_CONNECT.name,
                sourcePackageName = record.sourcePackageName,
                sourceRecordId = record.healthConnectId,
            )

            val entity = record.toEntity(
                existing = existing,
                lastSyncedAtMillis = lastSyncedAtMillis,
            )
            weightDao.insertWeightEntry(entity)
            if (existing == null) {
                inserted += 1
            } else {
                updated += 1
            }
        }

        return HealthConnectImportCount(
            inserted = inserted,
            updated = updated,
            skipped = skipped,
        )
    }

    override suspend fun importStepsRecords(
        records: List<HealthConnectStepsRecordSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount {
        var inserted = 0
        var updated = 0
        var skipped = 0

        records.forEach { record ->
            if (
                record.healthConnectId.isBlank() ||
                record.endTimeMillis <= record.startTimeMillis ||
                record.count <= 0L
            ) {
                skipped += 1
                return@forEach
            }

            val existing = stepsDao.findByExternalIdentity(
                source = DataSource.HEALTH_CONNECT.name,
                sourcePackageName = record.sourcePackageName,
                sourceRecordId = record.healthConnectId,
            )

            val entity = record.toEntity(
                existing = existing,
                lastSyncedAtMillis = lastSyncedAtMillis,
            )
            stepsDao.insertStepsEntry(entity)
            if (existing == null) {
                inserted += 1
            } else {
                updated += 1
            }
        }

        return HealthConnectImportCount(
            inserted = inserted,
            updated = updated,
            skipped = skipped,
        )
    }

    override suspend fun importExerciseSessionRecords(
        records: List<HealthConnectExerciseSessionSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount {
        var inserted = 0
        var updated = 0
        var skipped = 0

        records.forEach { record ->
            if (
                record.healthConnectId.isBlank() ||
                record.endTimeMillis <= record.startTimeMillis ||
                record.durationMinutes <= 0
            ) {
                skipped += 1
                return@forEach
            }

            val existing = exerciseDao.findByExternalIdentity(
                source = DataSource.HEALTH_CONNECT.name,
                sourcePackageName = record.sourcePackageName,
                sourceRecordId = record.healthConnectId,
            )

            val entity = record.toEntity(
                existing = existing,
                lastSyncedAtMillis = lastSyncedAtMillis,
            )
            exerciseDao.insertExerciseSession(entity)
            if (existing == null) {
                inserted += 1
            } else {
                updated += 1
            }
        }

        return HealthConnectImportCount(
            inserted = inserted,
            updated = updated,
            skipped = skipped,
        )
    }

    override suspend fun importSleepSessionRecords(
        records: List<HealthConnectSleepSessionSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount {
        var inserted = 0
        var updated = 0
        var skipped = 0

        records.forEach { record ->
            if (
                record.healthConnectId.isBlank() ||
                record.endTimeMillis <= record.startTimeMillis ||
                record.durationMinutes <= 0
            ) {
                skipped += 1
                return@forEach
            }

            val existing = sleepDao.findByExternalIdentity(
                source = DataSource.HEALTH_CONNECT.name,
                sourcePackageName = record.sourcePackageName,
                sourceRecordId = record.healthConnectId,
            )

            val entity = record.toEntity(
                existing = existing,
                lastSyncedAtMillis = lastSyncedAtMillis,
            )
            sleepDao.insertSleepEntry(entity)
            if (existing == null) {
                inserted += 1
            } else {
                updated += 1
            }
        }

        return HealthConnectImportCount(
            inserted = inserted,
            updated = updated,
            skipped = skipped,
        )
    }

    private fun HealthConnectWeightRecordSnapshot.toEntity(
        existing: WeightEntryEntity?,
        lastSyncedAtMillis: Long,
    ): WeightEntryEntity =
        WeightEntryEntity(
            id = existing?.id ?: idFactory(),
            weightKg = weightKg,
            recordedAt = recordedAtMillis,
            source = DataSource.HEALTH_CONNECT.name,
            note = existing?.note,
            sourceRecordId = healthConnectId,
            sourcePackageName = sourcePackageName,
            sourceAppName = sourceAppName,
            createdAt = existing?.createdAt ?: lastSyncedAtMillis,
            updatedAt = lastModifiedAtMillis ?: lastSyncedAtMillis,
            lastSyncedAt = lastSyncedAtMillis,
            deletedAt = existing?.deletedAt,
        )

    private fun HealthConnectSleepSessionSnapshot.toEntity(
        existing: SleepEntryEntity?,
        lastSyncedAtMillis: Long,
    ): SleepEntryEntity =
        SleepEntryEntity(
            id = existing?.id ?: idFactory(),
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            durationMinutes = durationMinutes,
            quality = existing?.quality,
            source = DataSource.HEALTH_CONNECT.name,
            note = existing?.note,
            sourceRecordId = healthConnectId,
            sourcePackageName = sourcePackageName,
            sourceAppName = sourceAppName,
            createdAt = existing?.createdAt ?: lastSyncedAtMillis,
            updatedAt = lastModifiedAtMillis ?: lastSyncedAtMillis,
            lastSyncedAt = lastSyncedAtMillis,
            deletedAt = existing?.deletedAt,
        )

    private fun HealthConnectStepsRecordSnapshot.toEntity(
        existing: StepsEntryEntity?,
        lastSyncedAtMillis: Long,
    ): StepsEntryEntity =
        StepsEntryEntity(
            id = existing?.id ?: idFactory(),
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            count = count,
            source = DataSource.HEALTH_CONNECT.name,
            note = existing?.note,
            sourceRecordId = healthConnectId,
            sourcePackageName = sourcePackageName,
            sourceAppName = sourceAppName,
            createdAt = existing?.createdAt ?: lastSyncedAtMillis,
            updatedAt = lastModifiedAtMillis ?: lastSyncedAtMillis,
            lastSyncedAt = lastSyncedAtMillis,
            deletedAt = existing?.deletedAt,
        )

    private fun HealthConnectExerciseSessionSnapshot.toEntity(
        existing: ExerciseSessionEntity?,
        lastSyncedAtMillis: Long,
    ): ExerciseSessionEntity =
        ExerciseSessionEntity(
            id = existing?.id ?: idFactory(),
            startTime = startTimeMillis,
            endTime = endTimeMillis,
            durationMinutes = durationMinutes,
            exerciseType = exerciseType.toDomainExerciseType().name,
            title = title ?: existing?.title,
            notes = existing?.notes ?: notes,
            source = DataSource.HEALTH_CONNECT.name,
            sourceRecordId = healthConnectId,
            sourcePackageName = sourcePackageName,
            sourceAppName = sourceAppName,
            createdAt = existing?.createdAt ?: lastSyncedAtMillis,
            updatedAt = lastModifiedAtMillis ?: lastSyncedAtMillis,
            lastSyncedAt = lastSyncedAtMillis,
            deletedAt = existing?.deletedAt,
        )

    private fun Int.toDomainExerciseType(): ExerciseType =
        when (this) {
            HealthConnectExerciseTypeWalking -> ExerciseType.WALKING
            HealthConnectExerciseTypeRunning, HealthConnectExerciseTypeRunningTreadmill -> ExerciseType.RUNNING
            HealthConnectExerciseTypeBiking, HealthConnectExerciseTypeBikingStationary -> ExerciseType.CYCLING
            HealthConnectExerciseTypeSwimmingOpenWater, HealthConnectExerciseTypeSwimmingPool -> ExerciseType.SWIMMING
            HealthConnectExerciseTypeStrengthTraining, HealthConnectExerciseTypeWeightlifting -> {
                ExerciseType.STRENGTH_TRAINING
            }
            HealthConnectExerciseTypeYoga -> ExerciseType.YOGA
            HealthConnectExerciseTypeHighIntensityIntervalTraining -> ExerciseType.HIIT
            else -> ExerciseType.OTHER
        }

    private companion object {
        private const val HealthConnectExerciseTypeBiking = 8
        private const val HealthConnectExerciseTypeBikingStationary = 9
        private const val HealthConnectExerciseTypeHighIntensityIntervalTraining = 36
        private const val HealthConnectExerciseTypeRunning = 56
        private const val HealthConnectExerciseTypeRunningTreadmill = 57
        private const val HealthConnectExerciseTypeStrengthTraining = 70
        private const val HealthConnectExerciseTypeSwimmingOpenWater = 73
        private const val HealthConnectExerciseTypeSwimmingPool = 74
        private const val HealthConnectExerciseTypeWalking = 79
        private const val HealthConnectExerciseTypeWeightlifting = 81
        private const val HealthConnectExerciseTypeYoga = 83
    }
}
