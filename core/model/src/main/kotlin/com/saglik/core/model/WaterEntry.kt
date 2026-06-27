package com.saglik.core.model

data class WaterEntry(
    val id: String,
    val amountMl: Int,
    val recordedAtMillis: Long,
    val source: DataSource,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)
