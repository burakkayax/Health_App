package com.saglik.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey val id: String,
    val weightKg: Float,
    val recordedAt: Long,
    val source: String,
    val note: String?,
)
