package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val mealType: String,
    val name: String,
    val calories: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinGrams: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
