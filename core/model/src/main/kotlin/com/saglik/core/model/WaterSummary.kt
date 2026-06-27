package com.saglik.core.model

data class WaterSummary(
    val totalTodayMl: Int,
    val totalLast7DaysMl: Int,
    val latestEntry: WaterEntry?,
    val hasData: Boolean,
)
