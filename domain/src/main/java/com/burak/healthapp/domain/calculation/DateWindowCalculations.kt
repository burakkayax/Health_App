package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.TrendsPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

data class MetricDateWindow(
    val startDate: LocalDate,
    val endDateInclusive: LocalDate,
) {
    val endDateExclusive: LocalDate
        get() = endDateInclusive.plusDays(1)

    val dayCount: Int
        get() = ChronoUnit.DAYS.between(startDate, endDateExclusive).toInt()

    fun days(): List<LocalDate> = (0 until dayCount).map { offset ->
        startDate.plusDays(offset.toLong())
    }

    operator fun contains(date: LocalDate): Boolean = !date.isBefore(startDate) && !date.isAfter(endDateInclusive)
}

fun metricDateWindowFor(
    selectedDate: LocalDate,
    period: TrendsPeriod,
): MetricDateWindow = when (period) {
    TrendsPeriod.WEEKLY -> {
        val startOfWeek = selectedDate.minusDays(
            (selectedDate.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong(),
        )
        MetricDateWindow(
            startDate = startOfWeek,
            endDateInclusive = startOfWeek.plusDays(DAYS_IN_WEEK - 1),
        )
    }
    TrendsPeriod.MONTHLY -> MetricDateWindow(
        startDate = selectedDate.withDayOfMonth(1),
        endDateInclusive = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth()),
    )
}

fun previousMetricDateWindowFor(
    selectedDate: LocalDate,
    period: TrendsPeriod,
): MetricDateWindow {
    val currentWindow = metricDateWindowFor(selectedDate, period)
    return when (period) {
        TrendsPeriod.WEEKLY -> MetricDateWindow(
            startDate = currentWindow.startDate.minusDays(DAYS_IN_WEEK),
            endDateInclusive = currentWindow.endDateInclusive.minusDays(DAYS_IN_WEEK),
        )
        TrendsPeriod.MONTHLY -> {
            val previousMonth = currentWindow.startDate.minusMonths(1)
            MetricDateWindow(
                startDate = previousMonth.withDayOfMonth(1),
                endDateInclusive = previousMonth.withDayOfMonth(previousMonth.lengthOfMonth()),
            )
        }
    }
}

fun buildCalendarWeekDays(anchorDate: LocalDate): List<LocalDate> =
    metricDateWindowFor(anchorDate, TrendsPeriod.WEEKLY).days()

fun buildCalendarMonthDays(anchorDate: LocalDate): List<LocalDate> =
    metricDateWindowFor(anchorDate, TrendsPeriod.MONTHLY).days()

fun buildWeekToDateDays(anchorDate: LocalDate): List<LocalDate> =
    buildCalendarWeekDays(anchorDate).filterNot { it.isAfter(anchorDate) }

fun buildMonthToDateDays(anchorDate: LocalDate): List<LocalDate> {
    val startOfMonth = anchorDate.withDayOfMonth(1)
    val dayCount = ChronoUnit.DAYS.between(startOfMonth, anchorDate).toInt()
    return (0..dayCount).map { startOfMonth.plusDays(it.toLong()) }
}

fun buildWindowDays(endDate: LocalDate, periodLength: Long): List<LocalDate> =
    (periodLength - 1 downTo 0).map { endDate.minusDays(it) }

internal fun LocalDate.toTrendLabel(): String = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))

private const val DAYS_IN_WEEK = 7L
