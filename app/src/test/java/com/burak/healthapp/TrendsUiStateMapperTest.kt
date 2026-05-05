package com.burak.healthapp

import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SleepStabilityMetrics
import com.burak.healthapp.domain.model.SleepStabilityStatus
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.feature.trends.TrendTone
import com.burak.healthapp.feature.trends.TrendsMetric
import com.burak.healthapp.feature.trends.toTrendsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun monthlySummary_usesSnapshotPeriodDayCount() {
        val state = sampleSnapshot(period = TrendsPeriod.MONTHLY, days = 31).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(exerciseTargetDaysPerWeek = 3),
        )

        assertEquals(31, state.goalAdherence.first().totalDays)
    }

    @Test
    fun monthlySummary_usesThisMonthPeriodLabel() {
        val state = sampleSnapshot(period = TrendsPeriod.MONTHLY, days = 31).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        assertEquals(R.string.trends_period_month, state.summary.periodLabel.stringResourceId())
    }

    @Test
    fun loggedDayAverageLabel_isExplicitInTrendsMapper() {
        val state = sampleSnapshot(period = TrendsPeriod.WEEKLY).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        assertEquals(
            R.string.trends_metric_logged_goal_days,
            state.metricCards.first { it.metric == TrendsMetric.HYDRATION }.secondaryValue.stringResourceId(),
        )
        assertEquals(
            R.string.trends_metric_logged_data_days,
            state.metricCards.first { it.metric == TrendsMetric.SLEEP }.secondaryValue.stringResourceId(),
        )
        assertEquals(
            R.string.trends_metric_logged_protein_average,
            state.metricCards.first { it.metric == TrendsMetric.NUTRITION }.secondaryValue.stringResourceId(),
        )
    }

    @Test
    fun periodDayAverageLabel_isExplicitInTrendsMapper() {
        val state = sampleSnapshot(period = TrendsPeriod.WEEKLY).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        assertEquals(
            R.string.trends_metric_period_caffeine_over_days,
            state.metricCards.first { it.metric == TrendsMetric.CAFFEINE }.secondaryValue.stringResourceId(),
        )
        assertEquals(
            R.string.trends_metric_period_smoking_over_days,
            state.metricCards.first { it.metric == TrendsMetric.SMOKING }.secondaryValue.stringResourceId(),
        )
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

    @Test
    fun readyMetrics_produceNonEmptyLabels() {
        val state = sampleSnapshot(
            period = TrendsPeriod.WEEKLY,
            sleepStability = SleepStabilityMetrics(
                recordCount = 5,
                averageBedtimeMinutes = 1380, // 23:00
                averageWakeTimeMinutes = 420, // 07:00
                bedtimeVariabilityMinutes = 20,
                wakeTimeVariabilityMinutes = 15,
                averageBedtimeTargetDeviationMinutes = 10,
                averageWakeTargetDeviationMinutes = 5,
                status = SleepStabilityStatus.READY,
            ),
        ).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        val card = state.sleepStability
        assertNotNull(card)
        assertEquals(SleepStabilityStatus.READY, card!!.status)
        assertTrue(card.hasData)
    }

    @Test
    fun noDataMetrics_produceNoDataMessage() {
        val state = sampleSnapshot(
            period = TrendsPeriod.WEEKLY,
            sleepStability = SleepStabilityMetrics(
                recordCount = 0,
                averageBedtimeMinutes = null,
                averageWakeTimeMinutes = null,
                bedtimeVariabilityMinutes = null,
                wakeTimeVariabilityMinutes = null,
                averageBedtimeTargetDeviationMinutes = null,
                averageWakeTargetDeviationMinutes = null,
                status = SleepStabilityStatus.NO_DATA,
            ),
        ).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        val card = state.sleepStability
        assertNotNull(card)
        assertEquals(SleepStabilityStatus.NO_DATA, card!!.status)
        assertFalse(card.hasData)
    }

    @Test
    fun limitedDataMetrics_produceLimitedMessage() {
        val state = sampleSnapshot(
            period = TrendsPeriod.WEEKLY,
            sleepStability = SleepStabilityMetrics(
                recordCount = 1,
                averageBedtimeMinutes = 1380,
                averageWakeTimeMinutes = 420,
                bedtimeVariabilityMinutes = null,
                wakeTimeVariabilityMinutes = null,
                averageBedtimeTargetDeviationMinutes = 0,
                averageWakeTargetDeviationMinutes = 0,
                status = SleepStabilityStatus.LIMITED_DATA,
            ),
        ).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        val card = state.sleepStability
        assertNotNull(card)
        assertEquals(SleepStabilityStatus.LIMITED_DATA, card!!.status)
        assertTrue(card.hasData)
    }

    @Test
    fun insightLanguage_doesNotClaimMedicalCausation() {
        val state = sampleSnapshot(
            period = TrendsPeriod.WEEKLY,
            sleepStability = SleepStabilityMetrics(
                recordCount = 5,
                averageBedtimeMinutes = 1380,
                averageWakeTimeMinutes = 420,
                bedtimeVariabilityMinutes = 45,
                wakeTimeVariabilityMinutes = 20,
                averageBedtimeTargetDeviationMinutes = 10,
                averageWakeTargetDeviationMinutes = 5,
                status = SleepStabilityStatus.READY,
            ),
        ).toTrendsUiState(
            avatarInitials = "BK",
            goals = GoalSettings(),
        )

        val card = state.sleepStability!!
        val insightText = (card.insight as? com.burak.healthapp.core.ui.text.UiText.StringResource)
        assertNotNull(insightText)
        // The string resource IDs should map to safe language (verified via string values)
        // Not asserting on resolved strings since we don't have Context in unit tests,
        // but verify the resource ID is one of the safe ones
        val safeResIds = listOf(
            R.string.trends_sleep_stability_ready_neutral,
            R.string.trends_sleep_stability_wake_more_regular,
            R.string.trends_sleep_stability_bedtime_more_variable,
        )
        assertTrue(
            "Insight resource should be a safe language resource",
            insightText!!.resId in safeResIds,
        )
    }

    private fun sampleSnapshot(
        period: TrendsPeriod,
        days: Int = 7,
        sleepStability: SleepStabilityMetrics = SleepStabilityMetrics(
            recordCount = 5,
            averageBedtimeMinutes = 1380,
            averageWakeTimeMinutes = 420,
            bedtimeVariabilityMinutes = 20,
            wakeTimeVariabilityMinutes = 15,
            averageBedtimeTargetDeviationMinutes = 10,
            averageWakeTargetDeviationMinutes = 5,
            status = SleepStabilityStatus.READY,
        ),
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
            sleepStability = sleepStability,
        )
    }

    private fun UiText.stringResourceId(): Int = (this as UiText.StringResource).resId
}
