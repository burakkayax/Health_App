package com.saglik.domain.repository

import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot
import com.saglik.domain.usecase.HealthConnectImportCount

interface HealthConnectSyncRepository {
    suspend fun readWeightRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectWeightRecordSnapshot>

    suspend fun readSleepSessionRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectSleepSessionSnapshot>

    suspend fun importWeightRecords(
        records: List<HealthConnectWeightRecordSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount

    suspend fun importSleepSessionRecords(
        records: List<HealthConnectSleepSessionSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount
}
