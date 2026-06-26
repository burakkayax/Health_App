package com.saglik.data.repository

import com.saglik.core.database.dao.WeightDao
import com.saglik.core.model.WeightEntry
import com.saglik.domain.repository.WeightRepository
import kotlinx.coroutines.flow.map

class DefaultWeightRepository(
    private val weightDao: WeightDao,
) : WeightRepository {
    override fun observeLatestWeightEntry() =
        weightDao.observeLatestWeightEntry().map { it?.toDomain() }

    override fun observeWeightEntries() =
        weightDao.observeWeightEntries().map { entries -> entries.map { it.toDomain() } }

    override suspend fun addWeightEntry(entry: WeightEntry) {
        weightDao.insertWeightEntry(entry.toEntity())
    }
}
