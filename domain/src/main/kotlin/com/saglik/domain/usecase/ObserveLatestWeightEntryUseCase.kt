package com.saglik.domain.usecase

import com.saglik.core.model.WeightEntry
import com.saglik.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow

class ObserveLatestWeightEntryUseCase(
    private val repository: WeightRepository,
) {
    operator fun invoke(): Flow<WeightEntry?> = repository.observeLatestWeightEntry()
}
