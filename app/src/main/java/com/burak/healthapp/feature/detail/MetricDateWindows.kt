package com.burak.healthapp.feature.detail

import com.burak.healthapp.domain.model.TrendsPeriod
import java.time.LocalDate
import java.time.temporal.ChronoUnit

internal fun buildTrailingWeekDays(anchorDate: LocalDate): List<LocalDate> = buildTrailingDays(anchorDate = anchorDate, dayCount = 7)

internal fun buildTrailingDays(
    anchorDate: LocalDate,
    dayCount: Int,
): List<LocalDate> {
    if (dayCount <= 0) return emptyList()
    return ((dayCount - 1).toLong() downTo 0L).map(anchorDate::minusDays)
}

internal fun buildMonthToDateDays(anchorDate: LocalDate): List<LocalDate> {
    val monthStart = anchorDate.withDayOfMonth(1)
    return (0L..ChronoUnit.DAYS.between(monthStart, anchorDate)).map(monthStart::plusDays)
}

internal fun buildPeriodDays(
    anchorDate: LocalDate,
    period: TrendsPeriod,
): List<LocalDate> = when (period) {
    TrendsPeriod.WEEKLY -> buildTrailingWeekDays(anchorDate)
    TrendsPeriod.MONTHLY -> buildMonthToDateDays(anchorDate)
}

internal fun buildMonthGridDays(anchorDate: LocalDate): List<LocalDate> {
    val monthStart = anchorDate.withDayOfMonth(1)
    val monthEnd = anchorDate.withDayOfMonth(anchorDate.lengthOfMonth())
    val gridStart = monthStart.minusDays((monthStart.dayOfWeek.value - 1).toLong())
    val gridEnd = monthEnd.plusDays((7 - monthEnd.dayOfWeek.value).toLong())
    val dayCount = ChronoUnit.DAYS.between(gridStart, gridEnd).toInt() + 1
    return (0 until dayCount).map { offset -> gridStart.plusDays(offset.toLong()) }
}
