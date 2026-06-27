package com.saglik.data.repository

import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.database.entity.SleepEntryEntity
import com.saglik.core.database.entity.WeightEntryEntity
import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.model.DataSource
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot
import com.saglik.domain.repository.HealthConnectSyncRepository
import com.saglik.domain.usecase.HealthConnectImportCount
import java.util.UUID

class DefaultHealthConnectSyncRepository(
    private val dataSource: HealthConnectDataSource,
    private val weightDao: WeightDao,
    private val sleepDao: SleepDao,
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
}
