package com.saglik.domain.usecase

import com.saglik.core.model.StepsEntry
import com.saglik.domain.repository.StepsRepository
import kotlinx.coroutines.flow.Flow

class ObserveStepsEntriesUseCase(
    private val repository: StepsRepository,
) {
    operator fun invoke(): Flow<List<StepsEntry>> =
        repository.observeStepsEntries()

    operator fun invoke(
        startInclusive: Long,
        endExclusive: Long,
    ): Flow<List<StepsEntry>> =
        repository.observeStepsEntriesBetween(
            startInclusive = startInclusive,
            endExclusive = endExclusive,
        )
}
