@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.WeightEntry
import com.saglik.domain.repository.WeightRepository
import java.util.UUID
import kotlinx.datetime.Instant

class AddWeightEntryUseCase(
    private val repository: WeightRepository,
    private val nowProvider: () -> Instant = { Instant.fromEpochMilliseconds(System.currentTimeMillis()) },
    private val idProvider: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(weightKg: Float, note: String? = null): Boolean {
        if (!weightKg.isFinite() || weightKg !in MIN_WEIGHT_KG..MAX_WEIGHT_KG) return false

        repository.addWeightEntry(
            WeightEntry(
                id = idProvider(),
                weightKg = weightKg,
                recordedAt = nowProvider(),
                source = DataSource.MANUAL,
                note = note?.takeIf { it.isNotBlank() },
            ),
        )
        return true
    }

    companion object {
        const val MIN_WEIGHT_KG = 30f
        const val MAX_WEIGHT_KG = 300f
    }
}
