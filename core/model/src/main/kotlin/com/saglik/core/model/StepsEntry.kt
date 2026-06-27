package com.saglik.core.model

data class StepsEntry(
    val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val count: Long,
    val source: DataSource,
    val note: String?,
    val sourceRecordId: String?,
    val sourcePackageName: String?,
    val sourceAppName: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long?,
    val deletedAt: Long?,
)
