@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.DataSource
import com.saglik.core.model.SleepEntry
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.sleep.SleepQualityMapper
import java.util.UUID

class AddSleepEntryUseCase(
    private val repository: SleepRepository,
    private val validateSleepInputUseCase: ValidateSleepInputUseCase = ValidateSleepInputUseCase(),
    private val qualityMapper: SleepQualityMapper = SleepQualityMapper(),
    private val idProvider: () -> String = { UUID.randomUUID().toString() },
) {
    suspend operator fun invoke(input: SleepInput): Boolean {
        val validation = validateSleepInputUseCase(input)
        val duration = validation.durationMinutes
        val start = input.startTime
        val end = input.endTime

        if (!validation.isValid || duration == null || start == null || end == null) {
            return false
        }

        repository.addSleepEntry(
            SleepEntry(
                id = idProvider(),
                startTime = start,
                endTime = end,
                durationMinutes = duration,
                quality = input.quality ?: qualityMapper.map(duration),
                source = DataSource.MANUAL,
                note = input.note?.trim()?.takeIf { it.isNotBlank() },
            ),
        )
        return true
    }
}
