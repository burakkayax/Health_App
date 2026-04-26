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

fun averageSteps(entries: List<StepEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .associate { entry -> entry.date to entry.steps }
    return averageByLoggedDays(totalsByDay)
}

fun buildStepTrendPoints(
    entries: List<StepEntry>,
    days: List<LocalDate>,
): List<TrendPoint> {
    if (entries.none { it.date in days }) return emptyList()
    return toTrendPoints(
        days = days,
        valuesByDate = entries
            .filter { it.date in days }
            .associate { entry -> entry.date to entry.steps.toFloat() },
    )
}
