package com.saglik.domain.repository

import com.saglik.core.model.WaterEntry
import com.saglik.domain.water.AddWaterEntryInput
import kotlinx.coroutines.flow.Flow

interface WaterRepository {
    fun observeWaterEntries(): Flow<List<WaterEntry>>

    fun observeWaterEntriesBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<WaterEntry>>

    fun observeLatestWaterEntry(): Flow<WaterEntry?>

    suspend fun addWaterEntry(input: AddWaterEntryInput)

    suspend fun deleteWaterEntry(id: String)
}
