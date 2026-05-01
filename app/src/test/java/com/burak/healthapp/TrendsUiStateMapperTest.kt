package com.burak.healthapp

import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.feature.trends.TrendTone
import com.burak.healthapp.feature.trends.TrendsMetric
import com.burak.healthapp.feature.trends.toTrendsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrendsUiStateMapperTest {
    @Test
    fun weeklySummary_buildsGoalAdherenceAndInsights() {
        val state = sampleSnapshot(period = TrendsPeriod.WEEKLY).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(exerciseTargetDaysPerWeek = 3),
        )

        assertEquals(TrendsPeriod.WEEKLY, state.selectedPeriod)
        assertEquals(5, state.goalAdherence.first { it.metric == TrendsMetric.HYDRATION }.completedDays)
        assertTrue(state.insights.any { it.severity == TrendTone.POSITIVE })
    }

    @Test
    fun monthlySummary_usesThirtyDayPeriod() {
        val state = sampleSnapshot(period = TrendsPeriod.MONTHLY, days = 30).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(exerciseTargetDaysPerWeek = 3),
        )

        assertEquals(30, state.goalAdherence.first().totalDays)
    }

    @Test
    fun insufficientSleepAndWeight_produceDataQualityWarnings() {
        val state = sampleSnapshot(period = TrendsPeriod.WEEKLY).copy(
            sleepLoggedDays = 2,
            weightRecordCount = 1,
        ).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        assertTrue(state.dataQuality.any { it.metric == TrendsMetric.SLEEP })
        assertTrue(state.dataQuality.any { it.metric == TrendsMetric.WEIGHT })
    }

    private fun sampleSnapshot(
        period: TrendsPeriod,
        days: Int = 7,
    ): TrendsSnapshot {
        val end = LocalDate.of(2026, 5, 1)
        val currentDays = (days - 1 downTo 0).map { end.minusDays(it.toLong()) }
        val previousDays = (days * 2 - 1 downTo days).map { end.minusDays(it.toLong()) }
        return TrendsSnapshot(
            period = period,
            days = currentDays,
            previousDays = previousDays,
            averageProteinGrams = 120f,
            previousAverageProteinGrams = 100f,
            averageSleepMinutes = 420f,
            previousAverageSleepMinutes = 410f,
            averageWaterMl = 2_100f,
            previousAverageWaterMl = 1_800f,
            averageSteps = 8_200f,
            previousAverageSteps = 6_000f,
            averageCalories = 2_000f,
            previousAverageCalories = 1_900f,
            averageCaffeineMg = 140f,
            previousAverageCaffeineMg = 120f,
            averageSmokingCount = 1f,
            previousAverageSmokingCount = 2f,
            exerciseTotalMinutes = 150,
            previousExerciseTotalMinutes = 90,
            exerciseActiveDays = 3,
            previousExerciseActiveDays = 2,
            waterGoalMetDays = 5,
            stepGoalMetDays = 4,
            sleepGoalMetDays = 3,
            caffeineUnderLimitDays = days - 1,
            caffeineOverLimitDays = 1,
            caffeineAfterCutoffDays = 2,
            smokingUnderLimitDays = days - 1,
            smokingOverLimitDays = 1,
            smokingZeroDays = 4,
            nutritionLoggedDays = days,
            hydrationLoggedDays = days,
            sleepLoggedDays = days,
            stepLoggedDays = days,
            caffeineLoggedDays = 4,
            smokingLoggedDays = 3,
            exerciseLoggedDays = 3,
            weightRecordCount = 2,
            weightStartKg = 78f,
            weightEndKg = 77f,
            weeklyCalories = emptyList(),
            weightPoints = emptyList(),
            stepPoints = emptyList(),
        )
    }
}
