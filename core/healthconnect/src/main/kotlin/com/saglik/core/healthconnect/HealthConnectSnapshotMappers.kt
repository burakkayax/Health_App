package com.saglik.core.healthconnect

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.WeightRecord
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectWeightRecordSnapshot
import java.time.Duration
import java.time.Instant

internal fun WeightRecord.toSnapshot(): HealthConnectWeightRecordSnapshot? =
    weightRecordSnapshot(
        healthConnectId = metadata.id,
        sourcePackageName = metadata.dataOrigin.packageName,
        sourceAppName = null,
        weightKg = weight.inKilograms.toFloat(),
        recordedAtMillis = time.toEpochMilli(),
        lastModifiedAtMillis = metadata.lastModifiedTime.toNullableEpochMilli(),
    )

internal fun SleepSessionRecord.toSnapshot(): HealthConnectSleepSessionSnapshot? =
    sleepSessionSnapshot(
        healthConnectId = metadata.id,
        sourcePackageName = metadata.dataOrigin.packageName,
        sourceAppName = null,
        startTimeMillis = startTime.toEpochMilli(),
        endTimeMillis = endTime.toEpochMilli(),
        lastModifiedAtMillis = metadata.lastModifiedTime.toNullableEpochMilli(),
    )

internal fun weightRecordSnapshot(
    healthConnectId: String,
    sourcePackageName: String?,
    sourceAppName: String?,
    weightKg: Float,
    recordedAtMillis: Long,
    lastModifiedAtMillis: Long?,
): HealthConnectWeightRecordSnapshot? {
    if (healthConnectId.isBlank()) {
        return null
    }

    return HealthConnectWeightRecordSnapshot(
        healthConnectId = healthConnectId,
        sourcePackageName = sourcePackageName,
        sourceAppName = sourceAppName,
        weightKg = weightKg,
        recordedAtMillis = recordedAtMillis,
        lastModifiedAtMillis = lastModifiedAtMillis,
    )
}

internal fun sleepSessionSnapshot(
    healthConnectId: String,
    sourcePackageName: String?,
    sourceAppName: String?,
    startTimeMillis: Long,
    endTimeMillis: Long,
    lastModifiedAtMillis: Long?,
): HealthConnectSleepSessionSnapshot? {
    if (healthConnectId.isBlank()) {
        return null
    }

    if (endTimeMillis <= startTimeMillis) {
        return null
    }

    val durationMinutes = Duration.ofMillis(endTimeMillis - startTimeMillis).toMinutes().toInt()
    if (durationMinutes <= 0) {
        return null
    }

    return HealthConnectSleepSessionSnapshot(
        healthConnectId = healthConnectId,
        sourcePackageName = sourcePackageName,
        sourceAppName = sourceAppName,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        durationMinutes = durationMinutes,
        lastModifiedAtMillis = lastModifiedAtMillis,
    )
}

private fun Instant.toNullableEpochMilli(): Long? =
    runCatching { toEpochMilli() }.getOrNull()
