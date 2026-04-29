package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "sleep_sessions",
    indices = [
        Index(value = ["sessionDate"], unique = true),
    ],
)
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionDate: LocalDate,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
