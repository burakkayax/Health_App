package com.saglik.domain.usecase.water

import com.saglik.domain.repository.WaterRepository
import com.saglik.domain.water.AddWaterEntryInput


class AddWaterEntryUseCase (
    private val repository: WaterRepository,
    private val validateWaterEntryInputUseCase: ValidateWaterEntryInputUseCase,
) {
    suspend operator fun invoke(input: AddWaterEntryInput): Result<Unit> {
        val validationResult = validateWaterEntryInputUseCase(input)
        if (validationResult.isFailure) {
            return validationResult
        }
        
        return try {
            repository.addWaterEntry(input)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
