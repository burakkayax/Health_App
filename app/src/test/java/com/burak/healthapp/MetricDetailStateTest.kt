package com.burak.healthapp

import com.burak.healthapp.core.ui.model.buildWeightTrendChartState
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.feature.detail.hydration.buildHydrationDetailUiState
import com.burak.healthapp.feature.detail.hydration.formatCompactWaterAmountMl
import com.burak.healthapp.feature.detail.step.buildStepMonthRingDays
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

class MetricDetailStateTest {
    @Test
    fun stepMonthRingDays_preserveCalendarOrderAndProgress() {
        withTurkishLocale {
            val anchorDate = LocalDate.of(2026, 4, 19)
            val days = buildStepMonthRingDays(
                anchorDate = anchorDate,
                entriesByDate = mapOf(
                    LocalDate.of(2026, 4, 3) to StepEntry(date = LocalDate.of(2026, 4, 3), steps = 8_000),
                    LocalDate.of(2026, 4, 4) to StepEntry(date = LocalDate.of(2026, 4, 4), steps = 4_000),
                ),
                targetSteps = 8_000,
            )

            assertEquals(35, days.size)
            assertFalse(days.first().isInCurrentMonth)
            assertEquals("1", days.first { it.isInCurrentMonth }.dayLabel)
            assertTrue(days.first { it.dayLabel == "3" && it.isInCurrentMonth }.isTargetMet)
            assertEquals(0.5f, days.first { it.dayLabel == "4" && it.isInCurrentMonth }.progress, 0.001f)
            assertEquals("4.000 adım", days.first { it.dayLabel == "4" && it.isInCurrentMonth }.valueLabel)
            assertFalse(days.first { it.dayLabel == "5" && it.isInCurrentMonth }.hasData)
        }
    }

    @Test
    fun stepMonthRingDays_usesProvidedTargetForProgress() {
        val anchorDate = LocalDate.of(2026, 4, 19)
        val entryDate = LocalDate.of(2026, 4, 4)
        val entries = mapOf(entryDate to StepEntry(date = entryDate, steps = 4_000))

        val defaultTargetDays = buildStepMonthRingDays(
            anchorDate = anchorDate,
            entriesByDate = entries,
            targetSteps = 8_000,
        )
        val customTargetDays = buildStepMonthRingDays(
            anchorDate = anchorDate,
            entriesByDate = entries,
            targetSteps = 4_000,
        )

        assertEquals(0.5f, defaultTargetDays.first { it.dayLabel == "4" && it.isInCurrentMonth }.progress, 0.001f)
        assertEquals(1f, customTargetDays.first { it.dayLabel == "4" && it.isInCurrentMonth }.progress, 0.001f)
    }

    @Test
    fun hydrationDetailState_calculatesDailyAndMonthlySummary() {
        withTurkishLocale {
            val selectedDate = LocalDate.of(2026, 4, 19)
            val entries = listOf(
                HydrationEntry(
                    id = 1,
                    date = selectedDate,
                    amountMl = 500,
                    createdAt = LocalDateTime.of(2026, 4, 19, 9, 0),
                ),
                HydrationEntry(
                    id = 2,
                    date = selectedDate,
                    amountMl = 700,
                    createdAt = LocalDateTime.of(2026, 4, 19, 12, 0),
                ),
                HydrationEntry(
                    id = 3,
                    date = selectedDate.minusDays(1),
                    amountMl = 1_000,
                    createdAt = LocalDateTime.of(2026, 4, 18, 10, 0),
                ),
            )

            val weeklyState = buildHydrationDetailUiState(
                entries = entries,
                selectedDate = selectedDate,
                period = TrendsPeriod.WEEKLY,
                targetMl = 2_000,
            )
            val monthlyState = buildHydrationDetailUiState(
                entries = entries,
                selectedDate = selectedDate,
                period = TrendsPeriod.MONTHLY,
                targetMl = 2_000,
            )

            assertEquals(1_200, weeklyState.totalMl)
            assertEquals(1_100, weeklyState.averageMl)
            assertEquals(listOf(2L, 1L), weeklyState.entries.map { it.id })
            assertEquals(7, weeklyState.periodDays.size)
            assertTrue(weeklyState.hasPeriodData)
            assertEquals(35, monthlyState.monthDays.size)
            assertEquals(0.6f, monthlyState.monthDays.first { it.dayLabel == "19" }.progress, 0.001f)
            assertEquals("1.200 ml", monthlyState.monthDays.first { it.dayLabel == "19" }.valueLabel)
        }
    }

    @Test
    fun compactWaterAmountFormatter_usesLiterLabelsForWeeklyChart() {
        assertEquals("--", formatCompactWaterAmountMl(0))
        assertEquals("0.5L", formatCompactWaterAmountMl(500))
        assertEquals("1L", formatCompactWaterAmountMl(1000))
        assertEquals("1.5L", formatCompactWaterAmountMl(1500))
        assertEquals("2.5L", formatCompactWaterAmountMl(2500))
        assertEquals("3L", formatCompactWaterAmountMl(3000))
    }

    @Test
    fun weightTrendChartState_usesOldestAndLatestWeights() {
        val state = buildWeightTrendChartState(
            points = listOf(
                TrendPoint("1 Nis", 78f),
                TrendPoint("10 Nis", 76f),
                TrendPoint("19 Nis", 75f),
            ),
            targetWeightKg = 74f,
        )

        requireNotNull(state)
        assertEquals(78f, state.startWeightKg, 0.001f)
        assertEquals(75f, state.currentWeightKg, 0.001f)
        assertEquals(74f, state.targetWeightKg, 0.001f)
        assertEquals(0.75f, state.progress, 0.001f)
    }

    private inline fun withTurkishLocale(block: () -> Unit) {
        val previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("tr-TR"))
        try {
            block()
        } finally {
            Locale.setDefault(previousLocale)
        }
    }
}
