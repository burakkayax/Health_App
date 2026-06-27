package com.saglik.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "steps_entries",
    indices = [
        Index(
            value = ["source", "sourcePackageName", "sourceRecordId"],
            name = "index_steps_entries_external_identity",
            unique = true,
        ),
    ],
)
data class StepsEntryEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long,
    val count: Long,
    val source: String,
    val note: String?,
    val sourceRecordId: String? = null,
    val sourcePackageName: String? = null,
    val sourceAppName: String? = null,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = endTime,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = endTime,
    val lastSyncedAt: Long? = null,
    val deletedAt: Long? = null,
)
