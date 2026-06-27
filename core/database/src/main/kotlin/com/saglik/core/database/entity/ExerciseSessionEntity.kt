package com.saglik.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_sessions",
    indices = [
        Index(
            value = ["source", "sourcePackageName", "sourceRecordId"],
            name = "index_exercise_sessions_external_identity",
            unique = true,
        ),
    ],
)
data class ExerciseSessionEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val exerciseType: String,
    val title: String?,
    val notes: String?,
    val source: String,
    val sourceRecordId: String? = null,
    val sourcePackageName: String? = null,
    val sourceAppName: String? = null,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = endTime,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = endTime,
    val lastSyncedAt: Long? = null,
    val deletedAt: Long? = null,
)
