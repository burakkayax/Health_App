package com.saglik.domain.repository

import com.saglik.core.model.StepsEntry
import kotlinx.coroutines.flow.Flow

interface StepsRepository {
    fun observeStepsEntries(): Flow<List<StepsEntry>>

    fun observeStepsEntriesBetween(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<StepsEntry>>
}
