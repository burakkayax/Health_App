package com.saglik.core.model

data class ExerciseSession(
    val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationMinutes: Int,
    val exerciseType: ExerciseType,
    val title: String?,
    val notes: String?,
    val source: DataSource,
    val sourceRecordId: String?,
    val sourcePackageName: String?,
    val sourceAppName: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long?,
    val deletedAt: Long?,
)

enum class ExerciseType {
    WALKING,
    RUNNING,
    CYCLING,
    SWIMMING,
    STRENGTH_TRAINING,
    YOGA,
    HIIT,
    OTHER,
}

data class ExerciseSummary(
    val sessionCount: Int,
    val totalDurationMinutes: Int,
    val mostRecentSession: ExerciseSession?,
)
