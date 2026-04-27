package com.burak.healthapp.core.reminder

import com.burak.healthapp.domain.model.WaterReminderSettings
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

fun calculateNextWaterReminderDelay(
    now: LocalDateTime,
    settings: WaterReminderSettings,
): Duration {
    val intervalMinutes = settings.intervalMinutes.coerceAtLeast(ReminderConstants.MIN_INTERVAL_MINUTES).toLong()
    val candidates = listOf(
        now.toLocalDate().minusDays(1),
        now.toLocalDate(),
        now.toLocalDate().plusDays(1),
    ).flatMap { date -> reminderSlotsForWindow(date, settings, intervalMinutes) }
        .sorted()

    val nextRun = candidates.firstOrNull { candidate -> !candidate.isBefore(now) }
        ?: LocalDateTime.of(now.toLocalDate().plusDays(1), settings.startTime)

    return Duration.between(now, nextRun).coerceAtLeast(Duration.ZERO)
}

fun isInsideWaterReminderWindow(
    time: LocalTime,
    settings: WaterReminderSettings,
): Boolean {
    return if (settings.startTime <= settings.endTime) {
        time >= settings.startTime && time <= settings.endTime
    } else {
        time >= settings.startTime || time <= settings.endTime
    }
}

fun shouldShowWaterReminder(
    today: LocalDate,
    snoozedDate: LocalDate?,
    currentMl: Int,
    targetMl: Int,
): Boolean {
    return snoozedDate != today && currentMl < targetMl
}

private fun reminderSlotsForWindow(
    date: LocalDate,
    settings: WaterReminderSettings,
    intervalMinutes: Long,
): List<LocalDateTime> {
    val start = LocalDateTime.of(date, settings.startTime)
    val endDate = if (settings.endTime < settings.startTime) date.plusDays(1) else date
    val end = LocalDateTime.of(endDate, settings.endTime)
    val slots = mutableListOf<LocalDateTime>()
    var cursor = start

    while (!cursor.isAfter(end)) {
        slots += cursor
        cursor = cursor.plusMinutes(intervalMinutes)
    }

    return slots
}
