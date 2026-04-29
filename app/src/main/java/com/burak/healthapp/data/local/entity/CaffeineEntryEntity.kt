package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity(
    tableName = "caffeine_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["date", "time"]),
        Index(value = ["date", "createdAt"]),
    ],
)
data class CaffeineEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val time: LocalTime,
    val drinkType: String,
    val size: String,
    val estimatedMg: Int,
    val customName: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
