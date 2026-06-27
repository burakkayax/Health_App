package com.saglik.domain.steps

data class StepsSummary(
    val totalStepsToday: Long,
    val totalStepsLast7Days: Long,
    val latestEntryCount: Long?,
    val hasData: Boolean,
)
