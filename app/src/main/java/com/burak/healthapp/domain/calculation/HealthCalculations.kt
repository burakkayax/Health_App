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

fun calculateNutritionTotals(entries: List<MealEntry>): DayNutritionTotal {
    return entries.fold(DayNutritionTotal()) { total, entry ->
        total.copy(
            calories = total.calories + entry.calories,
            carbsGrams = total.carbsGrams + entry.carbsGrams,
            fatGrams = total.fatGrams + entry.fatGrams,
            proteinGrams = total.proteinGrams + entry.proteinGrams,
        )
    }
}

fun calculateHydrationTotal(entries: List<HydrationEntry>): Int {
    return entries.sumOf { it.amountMl }
}

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
): String {
    return "${formatLocalTime(bedtime)} - ${formatLocalTime(wakeTime)}"
}

fun formatLocalTime(time: LocalTime): String {
    return time.format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun clampProgress(current: Float, target: Float): Float {
    if (target <= 0f) return 0f
    return (current / target).coerceIn(0f, 1f)
}

fun directionAwareProgress(baseline: Float, current: Float, target: Float): Float {
    if (baseline == target) return if (current == target) 1f else 0f
    return if (target > baseline) {
        ((current - baseline) / (target - baseline)).coerceIn(0f, 1f)
    } else {
        ((baseline - current) / (baseline - target)).coerceIn(0f, 1f)
    }
}

fun calculateBodyMassIndex(weightKg: Float?, heightCm: Float?): Float? {
    if (weightKg == null || heightCm == null || weightKg <= 0f || heightCm <= 0f) return null
    val heightMeters = heightCm / 100f
    return weightKg / (heightMeters * heightMeters)
}

fun classifyBodyMassIndex(bmi: Float): String {
    return when {
        bmi < 18.5f -> "Zayıf"
        bmi < 25f -> "Normal"
        bmi < 30f -> "Kilolu"
        else -> "Yüksek Kilolu"
    }
}

fun normalizeBodyMassIndexToGauge(
    bmi: Float,
    minValue: Float = 15f,
    maxValue: Float = 40f,
): Float {
    if (maxValue <= minValue) return 0f
    return ((bmi - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
}

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

fun toTrendPoints(
    days: List<LocalDate>,
    valuesByDate: Map<LocalDate, Float>,
): List<TrendPoint> {
    return days.map { date ->
        TrendPoint(
            label = date.toTrendLabel(),
            value = valuesByDate[date] ?: 0f,
        )
    }
}

fun clipWeightTrendDays(
    days: List<LocalDate>,
    earliestMeasurementDate: LocalDate?,
): List<LocalDate> {
    if (earliestMeasurementDate == null) return emptyList()
    return days.filterNot { it.isBefore(earliestMeasurementDate) }
}

fun buildInterpolatedWeightTrendPoints(
    days: List<LocalDate>,
    measurements: List<WeightMeasurementSample>,
): List<TrendPoint> {
    if (days.isEmpty() || measurements.isEmpty()) return emptyList()

    val resolvedSamples = measurements
        .groupBy(WeightMeasurementSample::date)
        .mapNotNull { (date, samples) ->
            samples.lastOrNull()?.let { sample ->
                WeightMeasurementSample(
                    date = date,
                    weightKg = sample.weightKg,
                )
            }
        }
        .sortedBy(WeightMeasurementSample::date)

    if (resolvedSamples.isEmpty()) return emptyList()

    return days.map { date ->
        TrendPoint(
            label = date.toTrendLabel(),
            value = interpolateWeightForDate(
                date = date,
                measurements = resolvedSamples,
            ),
        )
    }
}

fun averageProtein(entries: List<MealEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .groupBy(MealEntry::date)
        .mapValues { (_, mealEntries) -> mealEntries.sumOf { it.proteinGrams } }
    return averageByLoggedDays(totalsByDay)
}

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

fun averageCalories(entries: List<MealEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .groupBy(MealEntry::date)
        .mapValues { (_, mealEntries) -> mealEntries.sumOf { it.calories } }
    return averageByLoggedDays(totalsByDay)
}

fun averageWaterMl(entries: List<HydrationEntry>, days: List<LocalDate>): Float {
    if (days.isEmpty()) return 0f
    val totalsByDay = entries
        .filter { it.date in days }
        .groupBy(HydrationEntry::date)
        .mapValues { (_, hydrationEntries) -> hydrationEntries.sumOf { it.amountMl } }
    return averageByLoggedDays(totalsByDay)
}

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

fun buildWeeklyCalories(
    entries: List<MealEntry>,
    days: List<LocalDate>,
    targetCalories: Int,
): List<CalorieBarPoint> {
    val byDay = entries.groupBy { it.date }
    return days.map { date ->
        val calories = byDay[date].orEmpty().sumOf { it.calories }
        CalorieBarPoint(
            label = date.toTurkishWeekLetter(),
            calories = calories,
            progress = clampProgress(calories.toFloat(), targetCalories.toFloat()),
        )
    }
}

fun countExerciseDays(entries: List<ExerciseEntry>): Int {
    return entries.map(ExerciseEntry::date).distinct().size
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
        bedtimeDiff > 0 -> "Dün gece hedefine göre ${bedtimeDiff} dakika geç uyudun."
        bedtimeDiff < 0 -> "Dün gece hedefine göre ${-bedtimeDiff} dakika erken uyudun."
        wakeDiff > 0 -> "Bu sabah hedefinden ${wakeDiff} dakika geç uyandın."
        wakeDiff < 0 -> "Bu sabah hedefinden ${-wakeDiff} dakika erken uyandın."
        else -> "Dün gece uyku hedefini tam yakaladın."
    }
}

fun groupMealsByType(entries: List<MealEntry>): List<GroupedMealEntries> {
    return MealType.entries.mapNotNull { mealType ->
        val groupedEntries = entries.filter { it.mealType == mealType }
        if (groupedEntries.isEmpty()) {
            null
        } else {
            GroupedMealEntries(
                mealType = mealType,
                entries = groupedEntries,
            )
        }
    }
}

private fun averageByLoggedDays(valuesByDay: Map<LocalDate, Int>): Float {
    if (valuesByDay.isEmpty()) return 0f
    return valuesByDay.values.sum().toFloat() / valuesByDay.size
}

private fun LocalDate.toTurkishWeekLetter(): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY -> "P"
        DayOfWeek.TUESDAY -> "S"
        DayOfWeek.WEDNESDAY -> "Ç"
        DayOfWeek.THURSDAY -> "P"
        DayOfWeek.FRIDAY -> "C"
        DayOfWeek.SATURDAY -> "C"
        DayOfWeek.SUNDAY -> "P"
    }
}

private fun LocalDate.toTrendLabel(): String {
    return dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr"))
}

private fun interpolateWeightForDate(
    date: LocalDate,
    measurements: List<WeightMeasurementSample>,
): Float {
    measurements.firstOrNull { it.date == date }?.let { sample ->
        return sample.weightKg
    }

    val previous = measurements.lastOrNull { it.date.isBefore(date) }
    val next = measurements.firstOrNull { it.date.isAfter(date) }

    return when {
        previous != null && next != null -> {
            val totalDays = ChronoUnit.DAYS.between(previous.date, next.date).toFloat()
            val elapsedDays = ChronoUnit.DAYS.between(previous.date, date).toFloat()
            if (totalDays <= 0f) {
                previous.weightKg
            } else {
                previous.weightKg + ((next.weightKg - previous.weightKg) * (elapsedDays / totalDays))
            }
        }

        previous != null -> previous.weightKg
        next != null -> next.weightKg
        else -> measurements.first().weightKg
    }
}

data class GroupedMealEntries(
    val mealType: MealType,
    val entries: List<MealEntry>,
)

data class WeightMeasurementSample(
    val date: LocalDate,
    val weightKg: Float,
)

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

private fun LocalTime.toMinuteOfDay(): Int {
    return (toSecondOfDay() / 60)
}
