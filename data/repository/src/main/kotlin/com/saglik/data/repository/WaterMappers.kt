package com.saglik.data.repository

import com.saglik.core.database.entity.WaterEntryEntity
import com.saglik.core.model.DataSource
import com.saglik.core.model.WaterEntry

fun WaterEntryEntity.toWaterEntry(): WaterEntry {
    return WaterEntry(
        id = id,
        amountMl = amountMl,
        recordedAtMillis = recordedAt,
        source = DataSource.valueOf(source),
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
    )
}
