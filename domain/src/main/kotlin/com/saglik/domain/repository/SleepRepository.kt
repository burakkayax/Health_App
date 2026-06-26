package com.saglik.domain.repository

import com.saglik.core.model.DateRange
import com.saglik.core.model.SleepEntry
import kotlinx.coroutines.flow.Flow

interface SleepRepository {
    fun observeAllSleepEntries(): Flow<List<SleepEntry>>

    fun observeLatestSleepEntry(): Flow<SleepEntry?>

    fun observeSleepEntries(range: DateRange): Flow<List<SleepEntry>>

    suspend fun addSleepEntry(entry: SleepEntry)
}
