package com.saglik.domain.sleep

import com.saglik.core.model.ChartPoint
import com.saglik.core.model.PeriodType
import com.saglik.core.model.SleepEntry

data class SleepDetail(
    val periodType: PeriodType,
    val latestDurationMinutes: Int?,
    val averageMinutes: Int?,
    val shortestMinutes: Int?,
    val longestMinutes: Int?,
    val chartPoints: List<ChartPoint>,
    val entries: List<SleepEntry>,
)
