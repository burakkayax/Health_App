package com.saglik.domain.usecase.water

import com.saglik.domain.water.AddWaterEntryInput


class ValidateWaterEntryInputUseCase () {
    operator fun invoke(input: AddWaterEntryInput): Result<Unit> {
        if (input.amountMl <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than 0 ml."))
        }
        if (input.amountMl > 5000) {
            return Result.failure(IllegalArgumentException("Amount cannot exceed 5000 ml."))
        }
        if (input.recordedAtMillis <= 0) {
            return Result.failure(IllegalArgumentException("Invalid recorded time."))
        }
        return Result.success(Unit)
    }
}
