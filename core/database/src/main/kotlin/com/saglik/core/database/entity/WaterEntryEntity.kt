package com.saglik.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey val id: String,
    val amountMl: Int,
    val recordedAt: Long,
    val source: String,
    val note: String?,
    @ColumnInfo(defaultValue = "0") val createdAt: Long = recordedAt,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = recordedAt,
    val deletedAt: Long? = null,
)
