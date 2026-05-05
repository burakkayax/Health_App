package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SleepStabilityMetrics
import com.burak.healthapp.domain.model.SleepStabilityStatus
import java.time.Duration
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Computes sleep stability metrics from a list of sleep sessions.
 *
 * Uses a sleep-window-anchored approach for bedtime averaging to handle
 * midnight-crossing times correctly (e.g., 23:50 and 00:10 average to 00:00,
 * not 12:00).
 */
fun calculateSleepStabilityMetrics(
    sessions: List<SleepSession>,
    targetBedtime: LocalTime,
    targetWakeTime: LocalTime,
): SleepStabilityMetrics {
    val validSessions = sessions.filter { session ->
        Duration.between(session.startTime, session.endTime).toMinutes() > 0
    }

    if (validSessions.isEmpty()) {
        return SleepStabilityMetrics(
            recordCount = 0,
            averageBedtimeMinutes = null,
            averageWakeTimeMinutes = null,
            bedtimeVariabilityMinutes = null,
            wakeTimeVariabilityMinutes = null,
            averageBedtimeTargetDeviationMinutes = null,
            averageWakeTargetDeviationMinutes = null,
            status = SleepStabilityStatus.NO_DATA,
        )
    }

    val bedtimeValues = validSessions.map { session ->
        normalizeBedtimeMinutes(session.startTime.toLocalTime())
    }
    val wakeValues = validSessions.map { session ->
        session.endTime.toLocalTime().toMinuteOfDay()
    }

    val avgBedtimeRaw = bedtimeValues.average().roundToInt()
    val avgWakeRaw = wakeValues.average().roundToInt()

    val avgBedtimeNormalized = normalizeMinutesToClock(avgBedtimeRaw)
    val avgWakeNormalized = normalizeMinutesToClock(avgWakeRaw)

    if (validSessions.size == 1) {
        val targetBedtimeDeviation = abs(
            normalizeBedtimeMinutes(LocalTime.of(avgBedtimeNormalized / 60, avgBedtimeNormalized % 60)) -
                normalizeBedtimeMinutes(targetBedtime),
        )
        val targetWakeDeviation = abs(avgWakeNormalized - targetWakeTime.toMinuteOfDay())

        return SleepStabilityMetrics(
            recordCount = 1,
            averageBedtimeMinutes = avgBedtimeNormalized,
            averageWakeTimeMinutes = avgWakeNormalized,
            bedtimeVariabilityMinutes = null,
            wakeTimeVariabilityMinutes = null,
            averageBedtimeTargetDeviationMinutes = targetBedtimeDeviation,
            averageWakeTargetDeviationMinutes = targetWakeDeviation,
            status = SleepStabilityStatus.LIMITED_DATA,
        )
    }

    // Mean absolute deviation for variability (simpler and more explainable than stddev)
    val bedtimeMean = bedtimeValues.average()
    val bedtimeMAD = bedtimeValues
        .map { value -> abs(value - bedtimeMean) }
        .average()
        .roundToInt()

    val wakeMean = wakeValues.average()
    val wakeMAD = wakeValues
        .map { value -> abs(value - wakeMean) }
        .average()
        .roundToInt()

    // Target deviation: compare average to goal using anchored bedtime
    val targetBedtimeDeviation = abs(avgBedtimeRaw - normalizeBedtimeMinutes(targetBedtime))
    val targetWakeDeviation = abs(avgWakeRaw - targetWakeTime.toMinuteOfDay())

    return SleepStabilityMetrics(
        recordCount = validSessions.size,
        averageBedtimeMinutes = avgBedtimeNormalized,
        averageWakeTimeMinutes = avgWakeNormalized,
        bedtimeVariabilityMinutes = bedtimeMAD,
        wakeTimeVariabilityMinutes = wakeMAD,
        averageBedtimeTargetDeviationMinutes = targetBedtimeDeviation,
        averageWakeTargetDeviationMinutes = targetWakeDeviation,
        status = SleepStabilityStatus.READY,
    )
}

/**
 * Normalizes minutes that may exceed 1440 (from bedtime anchoring) back
 * to 0..1439 range for display as HH:mm clock time.
 */
internal fun normalizeMinutesToClock(minutes: Int): Int =
    ((minutes % 1440) + 1440) % 1440

/**
 * Formats minutes-since-midnight as HH:mm string.
 */
fun formatClockMinutes(totalMinutes: Int): String {
    val normalized = normalizeMinutesToClock(totalMinutes)
    val hours = normalized / 60
    val minutes = normalized % 60
    return "%02d:%02d".format(hours, minutes)
}
