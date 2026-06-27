package com.saglik.domain.repository

import com.saglik.core.model.HealthConnectExerciseSessionSnapshot
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectStepsRecordSnapshot
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

    suspend fun readStepsRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectStepsRecordSnapshot>

    suspend fun readExerciseSessionRecords(
        startTimeMillis: Long,
        endTimeMillis: Long,
    ): List<HealthConnectExerciseSessionSnapshot>

    suspend fun importWeightRecords(
        records: List<HealthConnectWeightRecordSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount

    suspend fun importSleepSessionRecords(
        records: List<HealthConnectSleepSessionSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount

    suspend fun importStepsRecords(
        records: List<HealthConnectStepsRecordSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount

    suspend fun importExerciseSessionRecords(
        records: List<HealthConnectExerciseSessionSnapshot>,
        lastSyncedAtMillis: Long,
    ): HealthConnectImportCount
}
