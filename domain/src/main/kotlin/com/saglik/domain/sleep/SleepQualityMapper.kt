package com.saglik.domain.sleep

import com.saglik.core.model.SleepQuality

class SleepQualityMapper {
    fun map(durationMinutes: Int): SleepQuality =
        when {
            durationMinutes < 5 * 60 -> SleepQuality.POOR
            durationMinutes < 6 * 60 + 30 -> SleepQuality.OKAY
            durationMinutes <= 8 * 60 + 30 -> SleepQuality.GOOD
            else -> SleepQuality.EXCELLENT
        }
}
