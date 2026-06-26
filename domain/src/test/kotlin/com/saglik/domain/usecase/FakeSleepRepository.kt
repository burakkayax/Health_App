@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DateRange
import com.saglik.core.model.SleepEntry
import com.saglik.domain.repository.SleepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class FakeSleepRepository(
    entries: List<SleepEntry> = emptyList(),
) : SleepRepository {
    val savedEntries = mutableListOf<SleepEntry>()
    private val entriesFlow = MutableStateFlow(entries)

    override fun observeAllSleepEntries(): Flow<List<SleepEntry>> =
        entriesFlow

    override fun observeLatestSleepEntry(): Flow<SleepEntry?> =
        entriesFlow.map { entries ->
            entries.maxByOrNull { it.endTime.toEpochMilliseconds() }
        }

    override fun observeSleepEntries(range: DateRange): Flow<List<SleepEntry>> =
        entriesFlow.map { entries ->
            entries.filter { entry ->
                val date = entry.endTime.toLocalDateTime(TimeZone.UTC).date
                date in range.start..range.end
            }
        }

    override suspend fun addSleepEntry(entry: SleepEntry) {
        savedEntries += entry
        entriesFlow.value = (entriesFlow.value + entry)
            .sortedByDescending { it.endTime.toEpochMilliseconds() }
    }
}
