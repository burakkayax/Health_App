package com.saglik.data.repository

import com.saglik.core.database.dao.StepsDao
import com.saglik.domain.repository.StepsRepository
import kotlinx.coroutines.flow.map

class DefaultStepsRepository(
    private val stepsDao: StepsDao,
) : StepsRepository {
    override fun observeStepsEntries() =
        stepsDao.observeStepsEntries().map { entries -> entries.map { it.toDomain() } }

    override fun observeStepsEntriesBetween(
        startInclusive: Long,
        endExclusive: Long,
    ) = stepsDao.observeStepsEntries().map { entries ->
        entries
            .filter { it.startTime < endExclusive && it.endTime > startInclusive }
            .sortedBy { it.startTime }
            .map { it.toDomain() }
    }
}
