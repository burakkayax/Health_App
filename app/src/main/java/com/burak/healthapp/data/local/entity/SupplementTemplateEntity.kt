package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "supplement_templates")
data class SupplementTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Float,
    val unitLabel: String,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
)
