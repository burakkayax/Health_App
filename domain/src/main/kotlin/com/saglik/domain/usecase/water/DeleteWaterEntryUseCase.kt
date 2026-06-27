package com.saglik.domain.usecase.water

import com.saglik.domain.repository.WaterRepository


class DeleteWaterEntryUseCase (
    private val repository: WaterRepository,
) {
    suspend operator fun invoke(id: String) {
        repository.deleteWaterEntry(id)
    }
}
