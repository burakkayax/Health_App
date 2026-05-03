package com.burak.healthapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "custom_foods",
    indices = [
        Index(value = ["name"]),
        Index(value = ["isFavorite"]),
        Index(value = ["updatedAt"]),
    ],
)
data class CustomFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String? = null,
    val servingName: String,
    val servingGrams: Float,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val fiberGrams: Float? = null,
    val sugarGrams: Float? = null,
    val sodiumMg: Float? = null,
    @ColumnInfo(defaultValue = "0") val isFavorite: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
