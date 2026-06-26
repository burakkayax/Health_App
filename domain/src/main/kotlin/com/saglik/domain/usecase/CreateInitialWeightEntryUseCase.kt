@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.WeightEntry
import com.saglik.domain.repository.WeightRepository
import java.util.UUID
import kotlinx.datetime.Instant

class CreateInitialWeightEntryUseCase(
    private val repository: WeightRepository,
    private val nowProvider: () -> Instant = { Instant.fromEpochMilliseconds(System.currentTimeMillis()) },
    private val idProvider: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(weightKg: Float): WeightEntry {
        val entry = WeightEntry(
            id = idProvider(),
            weightKg = weightKg,
            recordedAt = nowProvider(),
            source = DataSource.MANUAL,
            note = null,
        )
        repository.addWeightEntry(entry)
        return entry
    }
}
