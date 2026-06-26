package com.saglik.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val sex: String,
    val age: Int?,
    val birthDate: LocalDate?,
    val heightCm: Float,
    val goal: String,
    val createdAt: Long,
    val updatedAt: Long,
)
