package com.saglik.data.repository

internal data class LocalSyncMetadata(
    val sourceRecordId: String? = null,
    val sourcePackageName: String? = null,
    val sourceAppName: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val lastSyncedAt: Long? = null,
    val deletedAt: Long? = null,
)

internal fun manualSyncMetadata(timestampMillis: Long): LocalSyncMetadata =
    LocalSyncMetadata(
        createdAt = timestampMillis,
        updatedAt = timestampMillis,
    )
