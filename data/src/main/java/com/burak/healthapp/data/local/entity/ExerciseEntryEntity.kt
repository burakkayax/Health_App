package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "exercise_entries",
    indices = [
        Index(value = ["date"], unique = true),
    ],
)
data class ExerciseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: String,
    val durationMinutes: Int,
    val intensity: String,
)
