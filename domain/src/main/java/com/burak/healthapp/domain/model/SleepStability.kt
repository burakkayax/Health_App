package com.burak.healthapp.domain.model

data class SleepStabilityMetrics(
    val recordCount: Int,
    val averageBedtimeMinutes: Int?,
    val averageWakeTimeMinutes: Int?,
    val bedtimeVariabilityMinutes: Int?,
    val wakeTimeVariabilityMinutes: Int?,
    val averageBedtimeTargetDeviationMinutes: Int?,
    val averageWakeTargetDeviationMinutes: Int?,
    val status: SleepStabilityStatus,
)

enum class SleepStabilityStatus {
    NO_DATA,
    LIMITED_DATA,
    READY,
}
