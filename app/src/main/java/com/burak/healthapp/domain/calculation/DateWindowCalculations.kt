package com.burak.healthapp.domain.calculation

import com.burak.healthapp.domain.model.CalorieBarPoint
import com.burak.healthapp.domain.model.DayNutritionTotal
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.TrendPoint
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

fun buildCalendarWeekDays(anchorDate: LocalDate): List<LocalDate> {
    val startOfWeek = anchorDate.minusDays((anchorDate.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())
    return (0L..6L).map { startOfWeek.plusDays(it) }
}

fun buildWeekToDateDays(anchorDate: LocalDate): List<LocalDate> {
    return buildCalendarWeekDays(anchorDate).filterNot { it.isAfter(anchorDate) }
}

fun buildMonthToDateDays(anchorDate: LocalDate): List<LocalDate> {
    val startOfMonth = anchorDate.withDayOfMonth(1)
    val dayCount = ChronoUnit.DAYS.between(startOfMonth, anchorDate).toInt()
    return (0..dayCount).map { startOfMonth.plusDays(it.toLong()) }
}

fun buildWindowDays(endDate: LocalDate, periodLength: Long): List<LocalDate> {
    return (periodLength - 1 downTo 0).map { endDate.minusDays(it) }
}

internal fun LocalDate.toTrendLabel(): String {
    return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))
}
