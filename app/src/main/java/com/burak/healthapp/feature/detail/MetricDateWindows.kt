package com.burak.healthapp.feature.detail

import com.burak.healthapp.domain.calculation.metricDateWindowFor
import com.burak.healthapp.domain.model.TrendsPeriod
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal fun buildTrailingWeekDays(anchorDate: LocalDate): List<LocalDate> = metricDateWindowFor(anchorDate, TrendsPeriod.WEEKLY).days()

internal fun buildTrailingDays(
    anchorDate: LocalDate,
    dayCount: Int,
): List<LocalDate> {
    if (dayCount <= 0) return emptyList()
    return ((dayCount - 1).toLong() downTo 0L).map(anchorDate::minusDays)
}

internal fun buildPeriodDays(
    anchorDate: LocalDate,
    period: TrendsPeriod,
): List<LocalDate> = metricDateWindowFor(anchorDate, period).days()

internal fun buildMonthGridDays(anchorDate: LocalDate): List<LocalDate> {
    val monthStart = anchorDate.withDayOfMonth(1)
    val monthEnd = anchorDate.withDayOfMonth(anchorDate.lengthOfMonth())
    val gridStart = monthStart.minusDays((monthStart.dayOfWeek.value - 1).toLong())
    val gridEnd = monthEnd.plusDays((7 - monthEnd.dayOfWeek.value).toLong())
    val dayCount = ChronoUnit.DAYS.between(gridStart, gridEnd).toInt() + 1
    return (0 until dayCount).map { offset -> gridStart.plusDays(offset.toLong()) }
}
