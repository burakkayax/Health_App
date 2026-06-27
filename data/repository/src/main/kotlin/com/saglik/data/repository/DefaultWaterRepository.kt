package com.saglik.data.repository

import com.saglik.core.database.dao.WaterDao
import com.saglik.core.database.entity.WaterEntryEntity
import com.saglik.core.model.DataSource
import com.saglik.core.model.WaterEntry
import com.saglik.domain.repository.WaterRepository
import com.saglik.domain.water.AddWaterEntryInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID


class DefaultWaterRepository (
    private val waterDao: WaterDao,
) : WaterRepository {
    override fun observeWaterEntries(): Flow<List<WaterEntry>> {
        return waterDao.observeWaterEntries().map { entries ->
            entries.map { it.toWaterEntry() }
        }
    }

    override fun observeWaterEntriesBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<WaterEntry>> {
        return waterDao.observeWaterEntriesBetween(startInclusive, endExclusive).map { entries ->
            entries.map { it.toWaterEntry() }
        }
    }

    override fun observeLatestWaterEntry(): Flow<WaterEntry?> {
        return waterDao.observeLatestWaterEntry().map { it?.toWaterEntry() }
    }

    override suspend fun addWaterEntry(input: AddWaterEntryInput) {
        val currentTime = System.currentTimeMillis()
        val entity = WaterEntryEntity(
            id = UUID.randomUUID().toString(),
            amountMl = input.amountMl,
            recordedAt = input.recordedAtMillis,
            source = DataSource.MANUAL.name,
            note = input.note,
            createdAt = currentTime,
            updatedAt = currentTime,
            deletedAt = null,
        )
        waterDao.insertWaterEntry(entity)
    }

    override suspend fun deleteWaterEntry(id: String) {
        waterDao.softDeleteWaterEntry(id = id, deletedAt = System.currentTimeMillis())
    }
}
