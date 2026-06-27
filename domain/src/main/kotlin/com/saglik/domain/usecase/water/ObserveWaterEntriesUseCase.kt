package com.saglik.domain.usecase.water

import com.saglik.core.model.WaterEntry
import com.saglik.domain.repository.WaterRepository
import kotlinx.coroutines.flow.Flow


class ObserveWaterEntriesUseCase (
    private val repository: WaterRepository,
) {
    operator fun invoke(): Flow<List<WaterEntry>> = repository.observeWaterEntries()
}
