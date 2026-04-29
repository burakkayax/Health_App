package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "step_entries",
    indices = [
        Index(value = ["date"], unique = true),
    ],
)
data class StepEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val steps: Int,
    val sensorBaseline: Int?,
    val lastSensorValue: Int?,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
