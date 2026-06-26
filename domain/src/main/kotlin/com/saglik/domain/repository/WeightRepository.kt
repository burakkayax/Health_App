package com.saglik.domain.repository

import com.saglik.core.model.WeightEntry
import kotlinx.coroutines.flow.Flow

interface WeightRepository {
    fun observeLatestWeightEntry(): Flow<WeightEntry?>

    fun observeWeightEntries(): Flow<List<WeightEntry>>

    suspend fun addWeightEntry(entry: WeightEntry)
}
