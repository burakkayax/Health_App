package com.saglik.core.model

data class HealthConnectSyncWindow(
    val startTimeMillis: Long,
    val endTimeMillis: Long,
)

data class HealthConnectWeightRecordSnapshot(
    val healthConnectId: String,
    val sourcePackageName: String?,
    val sourceAppName: String?,
    val weightKg: Float,
    val recordedAtMillis: Long,
    val lastModifiedAtMillis: Long?,
)

data class HealthConnectSleepSessionSnapshot(
    val healthConnectId: String,
    val sourcePackageName: String?,
    val sourceAppName: String?,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val lastModifiedAtMillis: Long?,
)
