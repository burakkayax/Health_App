package com.burak.healthapp

import com.burak.healthapp.domain.calculation.WeightMeasurementSample
import com.burak.healthapp.domain.calculation.averageCalories
import com.burak.healthapp.domain.calculation.averageProtein
import com.burak.healthapp.domain.calculation.averageSleepMinutes
import com.burak.healthapp.domain.calculation.averageWaterMl
import com.burak.healthapp.domain.calculation.buildCalendarWeekDays
import com.burak.healthapp.domain.calculation.buildInterpolatedWeightTrendPoints
import com.burak.healthapp.domain.calculation.buildMonthToDateDays
import com.burak.healthapp.domain.calculation.buildWeeklyCalories
import com.burak.healthapp.domain.calculation.buildWindowDays
import com.burak.healthapp.domain.calculation.calculateBodyMassIndex
import com.burak.healthapp.domain.calculation.calculateNutritionTotals
import com.burak.healthapp.domain.calculation.classifyBodyMassIndex
import com.burak.healthapp.domain.calculation.clipWeightTrendDays
import com.burak.healthapp.domain.calculation.directionAwareProgress
import com.burak.healthapp.domain.calculation.formatSleepDuration
import com.burak.healthapp.domain.calculation.groupMealsByType
import com.burak.healthapp.domain.calculation.normalizeBodyMassIndexToGauge
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SleepSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class HealthCalculationsTest {
    @Test
    fun nutritionTotals_sumAllMealEntries() {
        val entries = listOf(
            MealEntry(
                date = LocalDate.of(2026, 4, 19),
                mealType = MealType.BREAKFAST,
                name = "Yumurta",
                calories = 220,
                carbsGrams = 2,
                fatGrams = 14,
                proteinGrams = 18,
            ),
            MealEntry(
                date = LocalDate.of(2026, 4, 19),
                mealType = MealType.DINNER,
                name = "Pirinç",
                calories = 380,
                carbsGrams = 72,
                fatGrams = 4,
                proteinGrams = 8,
            ),
        )

        val totals = calculateNutritionTotals(entries)

        assertEquals(600, totals.calories)
        assertEquals(74, totals.carbsGrams)
        assertEquals(18, totals.fatGrams)
        assertEquals(26, totals.proteinGrams)
    }

    @Test
    fun groupMealsByType_preservesMealTypeOrderAndSkipsEmptyGroups() {
        val entries = listOf(
            MealEntry(
                id = 1,
                date = LocalDate.of(2026, 4, 19),
                mealType = MealType.DINNER,
                name = "Somon",
                calories = 560,
                carbsGrams = 24,
                fatGrams = 30,
                proteinGrams = 40,
            ),
            MealEntry(
                id = 2,
                date = LocalDate.of(2026, 4, 19),
                mealType = MealType.BREAKFAST,
                name = "Omlet",
                calories = 420,
                carbsGrams = 16,
                fatGrams = 22,
                proteinGrams = 28,
            ),
        )

        val grouped = groupMealsByType(entries)

        assertEquals(listOf(MealType.BREAKFAST, MealType.DINNER), grouped.map { it.mealType })
        assertEquals(listOf("Omlet"), grouped.first().entries.map { it.name })
        assertEquals(listOf("Somon"), grouped.last().entries.map { it.name })
    }

    @Test
    fun buildCalendarWeekDays_returnsMondayToSundayContainingAnchorDate() {
        val days = buildCalendarWeekDays(LocalDate.of(2026, 4, 22))

        assertEquals(LocalDate.of(2026, 4, 20), days.first())
        assertEquals(LocalDate.of(2026, 4, 26), days.last())
    }

    @Test
    fun buildMonthToDateDays_returnsFirstDayOfMonthThroughAnchorDate() {
        val days = buildMonthToDateDays(LocalDate.of(2026, 4, 19))

        assertEquals(LocalDate.of(2026, 4, 1), days.first())
        assertEquals(LocalDate.of(2026, 4, 19), days.last())
    }

    @Test
    fun buildWeeklyCalories_clampsValuesAgainstTargetAndUsesExpectedLabels() {
        val monday = LocalDate.of(2026, 4, 20)
        val bars = buildWeeklyCalories(
            entries = listOf(
                MealEntry(
                    date = monday,
                    mealType = MealType.BREAKFAST,
                    name = "Yulaf",
                    calories = 2500,
                    carbsGrams = 0,
                    fatGrams = 0,
                    proteinGrams = 0,
                ),
            ),
            days = buildCalendarWeekDays(monday.plusDays(2)),
            targetCalories = 2200,
        )

        assertEquals(listOf("P", "S", "Ç", "P", "C", "C", "P"), bars.map { it.label })
        assertEquals(1f, bars.first().progress)
        assertEquals(0f, bars.last().progress)
    }

    @Test
    fun buildWindowDays_keepsLegacyRollingBehaviorForCallersThatStillNeedIt() {
        val days = buildWindowDays(LocalDate.of(2026, 4, 19), 30)

        assertEquals(LocalDate.of(2026, 3, 21), days.first())
        assertEquals(LocalDate.of(2026, 4, 19), days.last())
    }

    @Test
    fun directionAwareProgress_handlesLowerGoal() {
        val progress = directionAwareProgress(
            baseline = 90f,
            current = 84f,
            target = 78f,
        )

        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun directionAwareProgress_handlesHigherGoal() {
        val progress = directionAwareProgress(
            baseline = 70f,
            current = 75f,
            target = 80f,
        )

        assertEquals(0.5f, progress, 0.001f)
    }

    @Test
    fun calculateBodyMassIndex_returnsExpectedValue() {
        val bmi = calculateBodyMassIndex(weightKg = 75f, heightCm = 175f)

        assertEquals(24.49f, bmi ?: 0f, 0.01f)
    }

    @Test
    fun classifyBodyMassIndex_usesExpectedThresholds() {
        assertEquals("Zayıf", classifyBodyMassIndex(17.9f))
        assertEquals("Normal", classifyBodyMassIndex(24f))
        assertEquals("Kilolu", classifyBodyMassIndex(27f))
        assertEquals("Yüksek Kilolu", classifyBodyMassIndex(31f))
    }

    @Test
    fun normalizeBodyMassIndexToGauge_clampsOutOfRangeValues() {
        assertEquals(0f, normalizeBodyMassIndexToGauge(12f), 0.001f)
        assertEquals(1f, normalizeBodyMassIndexToGauge(45f), 0.001f)
    }

    @Test
    fun averageProtein_usesOnlyLoggedDays() {
        val monday = LocalDate.of(2026, 4, 20)
        val days = listOf(monday, monday.plusDays(1), monday.plusDays(2))

        val average = averageProtein(
            entries = listOf(
                MealEntry(
                    date = monday,
                    mealType = MealType.BREAKFAST,
                    name = "Yoğurt",
                    calories = 220,
                    carbsGrams = 12,
                    fatGrams = 8,
                    proteinGrams = 20,
                ),
                MealEntry(
                    date = monday.plusDays(2),
                    mealType = MealType.DINNER,
                    name = "Tavuk",
                    calories = 520,
                    carbsGrams = 0,
                    fatGrams = 12,
                    proteinGrams = 40,
                ),
            ),
            days = days,
        )

        assertEquals(30f, average, 0.001f)
    }

    @Test
    fun averageWaterMl_usesOnlyLoggedDays() {
        val day1 = LocalDate.of(2026, 4, 18)
        val day2 = LocalDate.of(2026, 4, 19)
        val day3 = LocalDate.of(2026, 4, 20)

        val average = averageWaterMl(
            entries = listOf(
                HydrationEntry(date = day1, amountMl = 1200),
                HydrationEntry(date = day1, amountMl = 800),
                HydrationEntry(date = day3, amountMl = 1000),
            ),
            days = listOf(day1, day2, day3),
        )

        assertEquals(1500f, average, 0.001f)
    }

    @Test
    fun averageCalories_usesOnlyLoggedDays() {
        val monday = LocalDate.of(2026, 4, 20)
        val average = averageCalories(
            entries = listOf(
                MealEntry(
                    date = monday,
                    mealType = MealType.BREAKFAST,
                    name = "Yulaf",
                    calories = 1800,
                    carbsGrams = 0,
                    fatGrams = 0,
                    proteinGrams = 0,
                ),
                MealEntry(
                    date = monday.plusDays(1),
                    mealType = MealType.DINNER,
                    name = "Pilav",
                    calories = 2200,
                    carbsGrams = 0,
                    fatGrams = 0,
                    proteinGrams = 0,
                ),
            ),
            days = buildCalendarWeekDays(monday),
        )

        assertEquals(2000f, average, 0.001f)
    }

    @Test
    fun averageSleepMinutes_usesOnlyLoggedDays() {
        val monday = LocalDate.of(2026, 4, 20)
        val average = averageSleepMinutes(
            sessions = listOf(
                SleepSession(
                    startTime = monday.minusDays(1).atTime(23, 0),
                    endTime = monday.atTime(7, 0),
                ),
                SleepSession(
                    startTime = monday.plusDays(1).atTime(0, 0),
                    endTime = monday.plusDays(1).atTime(7, 0),
                ),
            ),
            days = listOf(monday, monday.plusDays(1), monday.plusDays(2)),
        )

        assertEquals(450f, average, 0.001f)
    }

    @Test
    fun formatSleepDuration_formatsHoursAndMinutes() {
        val session = SleepSession(
            startTime = LocalDateTime.of(2026, 4, 18, 23, 30),
            endTime = LocalDateTime.of(2026, 4, 19, 7, 15),
        )

        assertEquals("7s 45d", formatSleepDuration(session))
    }

    @Test
    fun clipWeightTrendDays_dropsDatesBeforeFirstMeasurement() {
        val monday = LocalDate.of(2026, 4, 20)

        val clipped = clipWeightTrendDays(
            days = listOf(monday, monday.plusDays(1), monday.plusDays(2)),
            earliestMeasurementDate = monday.plusDays(2),
        )

        assertEquals(listOf(monday.plusDays(2)), clipped)
    }

    @Test
    fun buildInterpolatedWeightTrendPoints_fillsGapLinearlyBetweenKnownDays() {
        val days = (1L..5L).map { LocalDate.of(2026, 4, it.toInt()) }

        val points = buildInterpolatedWeightTrendPoints(
            days = days,
            measurements = listOf(
                WeightMeasurementSample(LocalDate.of(2026, 4, 1), 70f),
                WeightMeasurementSample(LocalDate.of(2026, 4, 5), 74f),
            ),
        )

        assertEquals(listOf(70f, 71f, 72f, 73f, 74f), points.map { it.value })
    }

    @Test
    fun buildInterpolatedWeightTrendPoints_carriesBackwardWhenOnlyFutureDataExists() {
        val days = listOf(
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 2),
            LocalDate.of(2026, 4, 3),
        )

        val points = buildInterpolatedWeightTrendPoints(
            days = days,
            measurements = listOf(
                WeightMeasurementSample(LocalDate.of(2026, 4, 5), 74f),
            ),
        )

        assertEquals(listOf(74f, 74f, 74f), points.map { it.value })
    }

    @Test
    fun buildInterpolatedWeightTrendPoints_carriesForwardWhenOnlyPastDataExists() {
        val days = listOf(
            LocalDate.of(2026, 4, 3),
            LocalDate.of(2026, 4, 4),
            LocalDate.of(2026, 4, 5),
        )

        val points = buildInterpolatedWeightTrendPoints(
            days = days,
            measurements = listOf(
                WeightMeasurementSample(LocalDate.of(2026, 4, 1), 70f),
            ),
        )

        assertEquals(listOf(70f, 70f, 70f), points.map { it.value })
    }

    @Test
    fun buildInterpolatedWeightTrendPoints_usesBoundaryMeasurementsOutsideWindow() {
        val days = listOf(
            LocalDate.of(2026, 4, 20),
            LocalDate.of(2026, 4, 21),
            LocalDate.of(2026, 4, 22),
        )

        val points = buildInterpolatedWeightTrendPoints(
            days = days,
            measurements = listOf(
                WeightMeasurementSample(LocalDate.of(2026, 4, 19), 70f),
                WeightMeasurementSample(LocalDate.of(2026, 4, 22), 73f),
                WeightMeasurementSample(LocalDate.of(2026, 4, 27), 78f),
            ),
        )

        assertEquals(listOf(71f, 72f, 73f), points.map { it.value })
    }

    @Test
    fun buildInterpolatedWeightTrendPoints_returnsEmptyWhenThereIsNoWeightData() {
        val points = buildInterpolatedWeightTrendPoints(
            days = buildCalendarWeekDays(LocalDate.of(2026, 4, 22)),
            measurements = emptyList(),
        )

        assertTrue(points.isEmpty())
    }
}
