package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SleepSession
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

fun calculateSleepDurationMinutes(session: SleepSession?): Int {
    if (session == null) return 0
    val minutes = Duration.between(session.startTime, session.endTime).toMinutes().toInt()
    return max(minutes, 0)
}

fun formatSleepDuration(session: SleepSession?): String {
    val totalMinutes = calculateSleepDurationMinutes(session)
    if (totalMinutes == 0) return "Henüz kayıt yok"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "${hours}s ${minutes}d"
}

fun formatMinutesAsSleepLabel(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0s 0d"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "${hours}s ${minutes}d"
}

fun formatClockRange(session: SleepSession?): String {
    if (session == null) return "Saat ekle"
    return "${formatLocalTime(session.startTime.toLocalTime())} - ${formatLocalTime(session.endTime.toLocalTime())}"
}

fun formatClockRange(
    bedtime: LocalTime,
    wakeTime: LocalTime,
): String = "${formatLocalTime(bedtime)} - ${formatLocalTime(wakeTime)}"

fun formatLocalTime(time: LocalTime): String = time.format(DateTimeFormatter.ofPattern("HH:mm"))

fun averageSleepMinutes(sessions: List<SleepSession>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = sessions
        .filter { it.sessionDate in days }
        .groupBy(SleepSession::sessionDate)
        .mapValues { (_, daySessions) ->
            daySessions.maxByOrNull(SleepSession::endTime)?.let(::calculateSleepDurationMinutes) ?: 0
        }
    return averageByLoggedDays(totalsByDay)
}

fun calculateSleepRegularityStandardDeviation(
    sessions: List<SleepSession>,
): Float? {
    if (sessions.size < 3) return null
    val bedtimeValues = sessions.map { normalizeBedtimeMinutes(it.startTime.toLocalTime()).toFloat() }
    val wakeValues = sessions.map { it.endTime.toLocalTime().toMinuteOfDay().toFloat() }
    return max(
        calculateStandardDeviation(bedtimeValues),
        calculateStandardDeviation(wakeValues),
    )
}

fun buildSleepFeedback(
    session: SleepSession?,
    goals: GoalSettings,
): String {
    if (session == null) return "Bugün için uyku kaydı yok."

    val bedtimeDiff = normalizeBedtimeMinutes(session.startTime.toLocalTime()) - normalizeBedtimeMinutes(goals.sleepTargetBedtime)
    val wakeDiff = session.endTime.toLocalTime().toMinuteOfDay() - goals.sleepTargetWakeTime.toMinuteOfDay()

    return when {
        bedtimeDiff > 0 -> "Dün gece hedefine göre $bedtimeDiff dakika geç uyudun."
        bedtimeDiff < 0 -> "Dün gece hedefine göre ${-bedtimeDiff} dakika erken uyudun."
        wakeDiff > 0 -> "Bu sabah hedefinden $wakeDiff dakika geç uyandın."
        wakeDiff < 0 -> "Bu sabah hedefinden ${-wakeDiff} dakika erken uyandın."
        else -> "Dün gece uyku hedefini tam yakaladın."
    }
}

private fun calculateStandardDeviation(values: List<Float>): Float {
    if (values.isEmpty()) return 0f
    val mean = values.average().toFloat()
    val variance = values
        .map { value -> (value - mean).pow(2) }
        .average()
        .toFloat()
    return sqrt(variance)
}

private fun normalizeBedtimeMinutes(time: LocalTime): Int {
    val minutes = time.toMinuteOfDay()
    return if (minutes < 12 * 60) minutes + (24 * 60) else minutes
}

private fun LocalTime.toMinuteOfDay(): Int = (toSecondOfDay() / 60)
