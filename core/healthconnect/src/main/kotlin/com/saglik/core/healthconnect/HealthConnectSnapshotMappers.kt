package com.saglik.core.healthconnect

import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.WeightRecord
import com.saglik.core.model.HealthConnectExerciseSessionSnapshot
import com.saglik.core.model.HealthConnectSleepSessionSnapshot
import com.saglik.core.model.HealthConnectStepsRecordSnapshot
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

internal fun StepsRecord.toSnapshot(): HealthConnectStepsRecordSnapshot? =
    stepsRecordSnapshot(
        healthConnectId = metadata.id,
        sourcePackageName = metadata.dataOrigin.packageName,
        sourceAppName = null,
        startTimeMillis = startTime.toEpochMilli(),
        endTimeMillis = endTime.toEpochMilli(),
        count = count,
        lastModifiedAtMillis = metadata.lastModifiedTime.toNullableEpochMilli(),
    )

internal fun ExerciseSessionRecord.toSnapshot(): HealthConnectExerciseSessionSnapshot? =
    exerciseSessionSnapshot(
        healthConnectId = metadata.id,
        sourcePackageName = metadata.dataOrigin.packageName,
        sourceAppName = null,
        startTimeMillis = startTime.toEpochMilli(),
        endTimeMillis = endTime.toEpochMilli(),
        exerciseType = exerciseType,
        title = title,
        notes = notes,
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

internal fun stepsRecordSnapshot(
    healthConnectId: String,
    sourcePackageName: String?,
    sourceAppName: String?,
    startTimeMillis: Long,
    endTimeMillis: Long,
    count: Long,
    lastModifiedAtMillis: Long?,
): HealthConnectStepsRecordSnapshot? {
    if (healthConnectId.isBlank()) {
        return null
    }

    if (endTimeMillis <= startTimeMillis) {
        return null
    }

    if (count <= 0L) {
        return null
    }

    return HealthConnectStepsRecordSnapshot(
        healthConnectId = healthConnectId,
        sourcePackageName = sourcePackageName,
        sourceAppName = sourceAppName,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        count = count,
        lastModifiedAtMillis = lastModifiedAtMillis,
    )
}

internal fun exerciseSessionSnapshot(
    healthConnectId: String,
    sourcePackageName: String?,
    sourceAppName: String?,
    startTimeMillis: Long,
    endTimeMillis: Long,
    exerciseType: Int,
    title: String?,
    notes: String?,
    lastModifiedAtMillis: Long?,
): HealthConnectExerciseSessionSnapshot? {
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

    return HealthConnectExerciseSessionSnapshot(
        healthConnectId = healthConnectId,
        sourcePackageName = sourcePackageName,
        sourceAppName = sourceAppName,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        durationMinutes = durationMinutes,
        exerciseType = exerciseType,
        title = title,
        notes = notes,
        lastModifiedAtMillis = lastModifiedAtMillis,
    )
}

private fun Instant.toNullableEpochMilli(): Long? =
    runCatching { toEpochMilli() }.getOrNull()
