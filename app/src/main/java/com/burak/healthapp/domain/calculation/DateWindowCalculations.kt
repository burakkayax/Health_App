package com.burak.healthapp.domain.calculation

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

fun buildCalendarWeekDays(anchorDate: LocalDate): List<LocalDate> {
    val startOfWeek = anchorDate.minusDays((anchorDate.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    return (0L..6L).map { startOfWeek.plusDays(it) }
}

fun buildWeekToDateDays(anchorDate: LocalDate): List<LocalDate> = buildCalendarWeekDays(anchorDate).filterNot { it.isAfter(anchorDate) }

fun buildMonthToDateDays(anchorDate: LocalDate): List<LocalDate> {
    val startOfMonth = anchorDate.withDayOfMonth(1)
    val dayCount = ChronoUnit.DAYS.between(startOfMonth, anchorDate).toInt()
    return (0..dayCount).map { startOfMonth.plusDays(it.toLong()) }
}

fun buildWindowDays(endDate: LocalDate, periodLength: Long): List<LocalDate> = (periodLength - 1 downTo 0).map { endDate.minusDays(it) }

internal fun LocalDate.toTrendLabel(): String = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))
