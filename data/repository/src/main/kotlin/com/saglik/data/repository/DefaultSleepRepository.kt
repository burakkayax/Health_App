@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.data.repository

import com.saglik.core.database.dao.SleepDao
import com.saglik.core.model.DateRange
import com.saglik.core.model.SleepEntry
import com.saglik.domain.repository.SleepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

class DefaultSleepRepository(
    private val sleepDao: SleepDao,
) : SleepRepository {
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()

    override fun observeAllSleepEntries(): Flow<List<SleepEntry>> =
        sleepDao.observeAllSleepEntries().map { entries -> entries.map { it.toDomain() } }

    override fun observeLatestSleepEntry(): Flow<SleepEntry?> =
        sleepDao.observeLatestSleepEntry().map { it?.toDomain() }

    override fun observeSleepEntries(range: DateRange): Flow<List<SleepEntry>> {
        val start = LocalDateTime(range.start, LocalTime(0, 0))
            .toInstant(timeZone)
            .toEpochMilliseconds()
        val endExclusive = LocalDateTime(range.end.plus(1, DateTimeUnit.DAY), LocalTime(0, 0))
            .toInstant(timeZone)
            .toEpochMilliseconds()

        return sleepDao.observeSleepEntriesBetween(
            startInclusive = start,
            endExclusive = endExclusive,
        ).map { entries -> entries.map { it.toDomain() } }
    }

    override suspend fun addSleepEntry(entry: SleepEntry) {
        sleepDao.insertSleepEntry(entry.toEntity())
    }
}
