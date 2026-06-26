package com.saglik.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [
        Index(
            value = ["source", "sourcePackageName", "sourceRecordId"],
            name = "index_weight_entries_external_identity",
            unique = true,
        ),
    ],
)
data class WeightEntryEntity(
    @PrimaryKey val id: String,
    val weightKg: Float,
    val recordedAt: Long,
    val source: String,
    val note: String?,
    val sourceRecordId: String? = null,
    val sourcePackageName: String? = null,
    val sourceAppName: String? = null,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = recordedAt,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = recordedAt,
    val lastSyncedAt: Long? = null,
    val deletedAt: Long? = null,
)
