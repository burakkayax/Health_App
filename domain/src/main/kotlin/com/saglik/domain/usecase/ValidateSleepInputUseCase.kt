@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.saglik.domain.usecase

import com.saglik.core.model.SleepQuality
import com.saglik.domain.sleep.SleepDurationCalculator
import kotlinx.datetime.Instant

data class SleepInput(
    val startTime: Instant?,
    val endTime: Instant?,
    val quality: SleepQuality?,
    val note: String?,
)

data class SleepValidationResult(
    val isValid: Boolean,
    val durationMinutes: Int?,
    val errorMessage: String?,
)

class ValidateSleepInputUseCase(
    private val calculator: SleepDurationCalculator = SleepDurationCalculator(),
    private val nowProvider: () -> Instant = {
        Instant.fromEpochMilliseconds(System.currentTimeMillis())
    },
) {
    operator fun invoke(input: SleepInput): SleepValidationResult {
        val start = input.startTime
            ?: return invalid("Add a sleep start time.")
        val end = input.endTime
            ?: return invalid("Add a wake time.")
        val now = nowProvider()

        if (start > now || end > now) {
            return invalid("Sleep entries cannot be in the future.")
        }

        val duration = calculator.calculateMinutes(start, end)
            ?: return invalid("Wake time must be after sleep start.")

        return when {
            duration < SleepDurationCalculator.MIN_DURATION_MINUTES ->
                invalid("Sleep must be at least 30 minutes.")

            duration > SleepDurationCalculator.MAX_DURATION_MINUTES ->
                invalid("Sleep must be 18 hours or less.")

            else -> SleepValidationResult(
                isValid = true,
                durationMinutes = duration,
                errorMessage = null,
            )
        }
    }

    private fun invalid(message: String): SleepValidationResult =
        SleepValidationResult(
            isValid = false,
            durationMinutes = null,
            errorMessage = message,
        )
}
