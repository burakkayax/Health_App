package com.burak.healthapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "supplement_dose_entries",
    foreignKeys = [
        ForeignKey(
            entity = SupplementTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["templateId", "date"], unique = true),
    ],
)
data class SupplementDoseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val date: LocalDate,
    val amount: Float,
    val loggedAt: LocalDateTime = LocalDateTime.now(),
)
