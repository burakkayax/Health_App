package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "hydration_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["date", "createdAt"]),
    ],
)
data class HydrationEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
