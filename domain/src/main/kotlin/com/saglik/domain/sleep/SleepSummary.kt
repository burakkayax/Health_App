package com.saglik.domain.sleep

import com.saglik.core.model.ChartPoint
import com.saglik.core.model.SleepQuality

data class SleepSummary(
    val latestDurationMinutes: Int?,
    val latestQuality: SleepQuality?,
    val weeklyDurations: List<ChartPoint>,
    val hasData: Boolean,
)
