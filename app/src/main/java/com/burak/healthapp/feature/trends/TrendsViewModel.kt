package com.burak.healthapp.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.R
import com.burak.healthapp.core.performance.PerformanceLogger
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.repository.TrendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.math.abs

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val trendsRepository: TrendsRepository,
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = selectedPeriod
        .flatMapLatest { period ->
            combine(
                settingsRepository.settings,
                trendsRepository.observeTrends(period),
            ) { settings, snapshot ->
                PerformanceLogger.measure("Trends:state_build") {
                    snapshot.toTrendsUiState(
                        avatarInitials = settings.userProfile.avatarInitials,
                        goals = settings.goalSettings,
                    )
                }
            }
                .flowOn(Dispatchers.Default)
                .distinctUntilChanged()
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyUiState(),
        )

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }
}

internal fun TrendsSnapshot.toTrendsUiState(
    avatarInitials: String,
    goals: GoalSettings,
): TrendsUiState {
    val totalDays = days.size.coerceAtLeast(1)
    val periodLabel = periodLabel(period)
    val hasAnyData = listOf(
        nutritionLoggedDays,
        hydrationLoggedDays,
        sleepLoggedDays,
        stepLoggedDays,
        caffeineLoggedDays,
        smokingLoggedDays,
        exerciseLoggedDays,
        weightRecordCount,
    ).any { it > 0 }

    val goalAdherence = buildGoalAdherence(totalDays, goals)
    val dataQuality = buildDataQuality(totalDays)
    val metricCards = buildMetricCards(goals)
    val insights = buildInsights(totalDays, goals)

    return TrendsUiState(
        avatarInitials = avatarInitials,
        selectedPeriod = period,
        summary = PeriodSummaryState(
            title = stringRes(R.string.trends_summary_title),
            body = if (hasAnyData) {
                stringRes(
                    R.string.trends_summary_body,
                    waterGoalMetDays,
                    totalDays,
                    caffeineAfterCutoffDays,
                )
            } else {
                stringRes(R.string.trends_empty_helper)
            },
            periodLabel = periodLabel,
            hasData = hasAnyData,
        ),
        highlights = buildHighlights(totalDays, dataQuality),
        goalAdherence = goalAdherence,
        metricCards = metricCards,
        insights = insights,
        dataQuality = dataQuality,
    )
}

private fun TrendsSnapshot.buildGoalAdherence(
    totalDays: Int,
    goals: GoalSettings,
): List<GoalAdherenceState> = listOf(
    adherence(TrendsMetric.HYDRATION, R.string.trends_metric_hydration, waterGoalMetDays, totalDays, positiveIsHigh = true),
    adherence(TrendsMetric.STEPS, R.string.trends_metric_steps, stepGoalMetDays, totalDays, positiveIsHigh = true),
    adherence(TrendsMetric.SLEEP, R.string.trends_metric_sleep, sleepGoalMetDays, totalDays, positiveIsHigh = true),
    adherence(
        metric = TrendsMetric.EXERCISE,
        labelRes = R.string.trends_metric_exercise,
        completedDays = exerciseActiveDays,
        totalDays = goals.exerciseTargetDaysPerWeek.coerceAtLeast(1),
        positiveIsHigh = true,
    ),
    adherence(TrendsMetric.CAFFEINE, R.string.trends_metric_caffeine, caffeineUnderLimitDays, totalDays, positiveIsHigh = true),
    adherence(TrendsMetric.SMOKING, R.string.trends_metric_smoking, smokingUnderLimitDays, totalDays, positiveIsHigh = true),
)

private fun adherence(
    metric: TrendsMetric,
    labelRes: Int,
    completedDays: Int,
    totalDays: Int,
    positiveIsHigh: Boolean,
): GoalAdherenceState {
    val safeTotal = totalDays.coerceAtLeast(1)
    val progress = (completedDays.toFloat() / safeTotal).coerceIn(0f, 1f)
    val tone = when {
        progress >= 0.7f -> if (positiveIsHigh) TrendTone.POSITIVE else TrendTone.WARNING
        progress < 0.4f -> if (positiveIsHigh) TrendTone.WARNING else TrendTone.POSITIVE
        else -> TrendTone.NEUTRAL
    }
    return GoalAdherenceState(
        metric = metric,
        label = stringRes(labelRes),
        completedDays = completedDays,
        totalDays = safeTotal,
        progress = progress,
        tone = tone,
    )
}

private fun TrendsSnapshot.buildHighlights(
    totalDays: Int,
    dataQuality: List<DataQualityWarningState>,
): List<TrendHighlightState> {
    val best = when {
        averageSteps > previousAverageSteps && previousAverageSteps > 0f -> TrendHighlightState(
            title = stringRes(R.string.trends_highlight_best_change),
            value = stringRes(R.string.trends_metric_steps),
            description = stringRes(R.string.trends_highlight_steps_up),
            tone = TrendTone.POSITIVE,
        )
        waterGoalMetDays >= (totalDays * 0.7f).toInt() -> TrendHighlightState(
            title = stringRes(R.string.trends_highlight_best_change),
            value = stringRes(R.string.trends_metric_hydration),
            description = stringRes(R.string.trends_highlight_water_goal, waterGoalMetDays, totalDays),
            tone = TrendTone.POSITIVE,
        )
        else -> TrendHighlightState(
            title = stringRes(R.string.trends_highlight_best_change),
            value = stringRes(R.string.trends_highlight_neutral_value),
            description = stringRes(R.string.trends_highlight_neutral_description),
            tone = TrendTone.NEUTRAL,
        )
    }

    val attention = when {
        caffeineAfterCutoffDays >= 2 -> TrendHighlightState(
            title = stringRes(R.string.trends_highlight_attention),
            value = stringRes(R.string.trends_metric_caffeine),
            description = stringRes(R.string.trends_insight_caffeine_cutoff_body, caffeineAfterCutoffDays),
            tone = TrendTone.WARNING,
        )
        smokingOverLimitDays >= 2 -> TrendHighlightState(
            title = stringRes(R.string.trends_highlight_attention),
            value = stringRes(R.string.trends_metric_smoking),
            description = stringRes(R.string.trends_insight_smoking_limit_body, smokingOverLimitDays),
            tone = TrendTone.WARNING,
        )
        else -> TrendHighlightState(
            title = stringRes(R.string.trends_highlight_attention),
            value = stringRes(R.string.trends_highlight_neutral_value),
            description = stringRes(R.string.trends_highlight_attention_clear),
            tone = TrendTone.NEUTRAL,
        )
    }

    return listOfNotNull(
        best,
        attention,
        dataQuality.firstOrNull()?.let { warning ->
            TrendHighlightState(
                title = stringRes(R.string.trends_highlight_data_quality),
                value = warning.label(),
                description = warning.message,
                tone = TrendTone.WARNING,
            )
        },
    )
}

private fun TrendsSnapshot.buildMetricCards(goals: GoalSettings): List<MetricTrendCardState> = listOf(
    MetricTrendCardState(
        metric = TrendsMetric.HYDRATION,
        title = stringRes(R.string.trends_metric_hydration),
        primaryValue = stringRes(R.string.format_ml_count, formatWholeNumber(averageWaterMl.toInt())),
        secondaryValue = stringRes(R.string.trends_metric_goal_days, waterGoalMetDays, days.size),
        changeLabel = changeLabel(averageWaterMl, previousAverageWaterMl),
        chartPoints = emptyList(),
        tone = toneFromGoalDays(waterGoalMetDays, days.size),
        destination = TrendsDetailDestination.HYDRATION,
        hasData = hydrationLoggedDays > 0,
    ),
    MetricTrendCardState(
        metric = TrendsMetric.STEPS,
        title = stringRes(R.string.trends_metric_steps),
        primaryValue = stringRes(R.string.format_steps_count, formatWholeNumber(averageSteps.toInt())),
        secondaryValue = stringRes(R.string.trends_metric_goal_days, stepGoalMetDays, days.size),
        changeLabel = changeLabel(averageSteps, previousAverageSteps),
        chartPoints = stepPoints,
        tone = toneFromGoalDays(stepGoalMetDays, days.size),
        destination = TrendsDetailDestination.STEPS,
        hasData = stepLoggedDays > 0,
    ),
    MetricTrendCardState(
        metric = TrendsMetric.SLEEP,
        title = stringRes(R.string.trends_metric_sleep),
        primaryValue = sleepAverageText(averageSleepMinutes),
        secondaryValue = stringRes(R.string.trends_metric_data_days, sleepLoggedDays, days.size),
        changeLabel = changeLabel(averageSleepMinutes, previousAverageSleepMinutes),
        chartPoints = emptyList(),
        tone = toneFromGoalDays(sleepGoalMetDays, days.size),
        destination = TrendsDetailDestination.SLEEP,
        hasData = sleepLoggedDays > 0,
    ),
    MetricTrendCardState(
        metric = TrendsMetric.CAFFEINE,
        title = stringRes(R.string.trends_metric_caffeine),
        primaryValue = stringRes(R.string.format_mg_count, formatWholeNumber(averageCaffeineMg.toInt())),
        secondaryValue = stringRes(R.string.trends_metric_caffeine_over_days, caffeineOverLimitDays, days.size),
        changeLabel = changeLabel(averageCaffeineMg, previousAverageCaffeineMg),
        chartPoints = emptyList(),
        tone = if (caffeineOverLimitDays > 0) TrendTone.WARNING else TrendTone.NEUTRAL,
        destination = TrendsDetailDestination.CAFFEINE,
        hasData = caffeineLoggedDays > 0,
    ),
    MetricTrendCardState(
        metric = TrendsMetric.EXERCISE,
        title = stringRes(R.string.trends_metric_exercise),
        primaryValue = stringRes(R.string.trends_metric_minutes, formatWholeNumber(exerciseTotalMinutes)),
        secondaryValue = stringRes(R.string.trends_metric_active_days, exerciseActiveDays, goals.exerciseTargetDaysPerWeek),
        changeLabel = changeLabel(exerciseTotalMinutes.toFloat(), previousExerciseTotalMinutes.toFloat()),
        chartPoints = emptyList(),
        tone = if (exerciseActiveDays >= goals.exerciseTargetDaysPerWeek) TrendTone.POSITIVE else TrendTone.NEUTRAL,
        destination = TrendsDetailDestination.EXERCISE,
        hasData = exerciseLoggedDays > 0,
    ),
    MetricTrendCardState(
        metric = TrendsMetric.SMOKING,
        title = stringRes(R.string.trends_metric_smoking),
        primaryValue = stringRes(R.string.trends_metric_count_average, averageSmokingCount),
        secondaryValue = stringRes(R.string.trends_metric_smoking_over_days, smokingOverLimitDays, days.size),
        changeLabel = changeLabel(averageSmokingCount, previousAverageSmokingCount),
        chartPoints = emptyList(),
        tone = if (smokingOverLimitDays > 0) TrendTone.WARNING else TrendTone.NEUTRAL,
        destination = TrendsDetailDestination.SMOKING,
        hasData = smokingLoggedDays > 0,
    ),
    MetricTrendCardState(
        metric = TrendsMetric.WEIGHT,
        title = stringRes(R.string.trends_metric_weight),
        primaryValue = weightChangeText(),
        secondaryValue = stringRes(R.string.trends_metric_data_days, weightRecordCount, days.size),
        changeLabel = if (weightRecordCount >= 2) stringRes(R.string.trends_change_tracked) else stringRes(R.string.trends_change_no_previous),
        chartPoints = weightPoints,
        tone = TrendTone.NEUTRAL,
        destination = TrendsDetailDestination.WEIGHT,
        hasData = weightRecordCount > 0,
    ),
    MetricTrendCardState(
        metric = TrendsMetric.NUTRITION,
        title = stringRes(R.string.trends_metric_nutrition),
        primaryValue = stringRes(R.string.format_kcal_count, formatWholeNumber(averageCalories.toInt())),
        secondaryValue = stringRes(R.string.trends_metric_protein_average, formatWholeNumber(averageProteinGrams.toInt())),
        changeLabel = changeLabel(averageCalories, previousAverageCalories),
        chartPoints = weeklyCalories.map { point -> TrendPoint(point.label, point.calories.toFloat()) },
        tone = TrendTone.NEUTRAL,
        destination = TrendsDetailDestination.NUTRITION,
        hasData = nutritionLoggedDays > 0,
    ),
)

private fun TrendsSnapshot.buildInsights(totalDays: Int, goals: GoalSettings): List<ShortInsightState> = buildList {
    val waterRatio = waterGoalMetDays.toFloat() / totalDays.coerceAtLeast(1)
    when {
        waterRatio >= 0.7f -> add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_water_title),
                body = stringRes(R.string.trends_insight_water_positive_body, waterGoalMetDays, totalDays),
                severity = TrendTone.POSITIVE,
            ),
        )
        waterRatio < 0.4f -> add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_water_title),
                body = stringRes(R.string.trends_insight_water_warning_body, waterGoalMetDays, totalDays),
                severity = TrendTone.WARNING,
            ),
        )
    }
    when {
        previousAverageSteps > 0f && averageSteps > previousAverageSteps * 1.05f -> add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_steps_title),
                body = stringRes(R.string.trends_insight_steps_up_body),
                severity = TrendTone.POSITIVE,
            ),
        )
        previousAverageSteps > 0f && averageSteps < previousAverageSteps * 0.9f -> add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_steps_title),
                body = stringRes(R.string.trends_insight_steps_down_body),
                severity = TrendTone.WARNING,
            ),
        )
    }
    if (caffeineAfterCutoffDays >= 2) {
        add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_caffeine_title),
                body = stringRes(R.string.trends_insight_caffeine_cutoff_body, caffeineAfterCutoffDays),
                severity = TrendTone.WARNING,
            ),
        )
    }
    if (smokingOverLimitDays >= 2) {
        add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_smoking_title),
                body = stringRes(R.string.trends_insight_smoking_limit_body, smokingOverLimitDays),
                severity = TrendTone.WARNING,
            ),
        )
    }
    if (exerciseActiveDays >= goals.exerciseTargetDaysPerWeek) {
        add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_exercise_title),
                body = stringRes(R.string.trends_insight_exercise_positive_body, exerciseActiveDays),
                severity = TrendTone.POSITIVE,
            ),
        )
    }
    if (isEmpty()) {
        add(
            ShortInsightState(
                title = stringRes(R.string.trends_insight_neutral_title),
                body = stringRes(R.string.trends_insight_neutral_body),
                severity = TrendTone.NEUTRAL,
            ),
        )
    }
}

private fun TrendsSnapshot.buildDataQuality(totalDays: Int): List<DataQualityWarningState> = buildList {
    val threshold = totalDays * 0.5f
    if (sleepLoggedDays < threshold) {
        add(
            DataQualityWarningState(
                metric = TrendsMetric.SLEEP,
                message = stringRes(R.string.trends_data_quality_sleep, sleepLoggedDays, totalDays),
                availableDays = sleepLoggedDays,
                expectedDays = totalDays,
            ),
        )
    }
    if (weightRecordCount < 2) {
        add(
            DataQualityWarningState(
                metric = TrendsMetric.WEIGHT,
                message = stringRes(R.string.trends_data_quality_weight),
                availableDays = weightRecordCount,
                expectedDays = 2,
            ),
        )
    }
    if (nutritionLoggedDays < threshold) {
        add(
            DataQualityWarningState(
                metric = TrendsMetric.NUTRITION,
                message = stringRes(R.string.trends_data_quality_nutrition, nutritionLoggedDays, totalDays),
                availableDays = nutritionLoggedDays,
                expectedDays = totalDays,
            ),
        )
    }
}

private fun changeLabel(current: Float, previous: Float): UiText = when {
    previous <= 0f -> stringRes(R.string.trends_change_no_previous)
    current > previous * 1.05f -> stringRes(R.string.trends_change_increased)
    current < previous * 0.95f -> stringRes(R.string.trends_change_decreased)
    abs(current - previous) <= previous * 0.05f -> stringRes(R.string.trends_change_stable)
    else -> stringRes(R.string.trends_change_stable)
}

private fun toneFromGoalDays(completedDays: Int, totalDays: Int): TrendTone {
    val ratio = completedDays.toFloat() / totalDays.coerceAtLeast(1)
    return when {
        ratio >= 0.7f -> TrendTone.POSITIVE
        ratio < 0.4f -> TrendTone.WARNING
        else -> TrendTone.NEUTRAL
    }
}

private fun sleepAverageText(minutes: Float): UiText {
    val totalMinutes = minutes.toInt()
    return stringRes(R.string.format_sleep_short, totalMinutes / 60, totalMinutes % 60)
}

private fun TrendsSnapshot.weightChangeText(): UiText {
    val start = weightStartKg
    val end = weightEndKg
    return if (start != null && end != null && weightRecordCount >= 2) {
        stringRes(R.string.trends_metric_weight_change, end - start)
    } else {
        stringRes(R.string.trends_metric_weight_not_enough)
    }
}

private fun DataQualityWarningState.label(): UiText = when (metric) {
    TrendsMetric.HYDRATION -> stringRes(R.string.trends_metric_hydration)
    TrendsMetric.STEPS -> stringRes(R.string.trends_metric_steps)
    TrendsMetric.SLEEP -> stringRes(R.string.trends_metric_sleep)
    TrendsMetric.EXERCISE -> stringRes(R.string.trends_metric_exercise)
    TrendsMetric.CAFFEINE -> stringRes(R.string.trends_metric_caffeine)
    TrendsMetric.SMOKING -> stringRes(R.string.trends_metric_smoking)
    TrendsMetric.WEIGHT -> stringRes(R.string.trends_metric_weight)
    TrendsMetric.NUTRITION -> stringRes(R.string.trends_metric_nutrition)
}

private fun periodLabel(period: TrendsPeriod): UiText = stringRes(
    if (period == TrendsPeriod.WEEKLY) {
        R.string.trends_period_last_7_days
    } else {
        R.string.trends_period_last_30_days
    },
)

private fun emptyUiState(): TrendsUiState = TrendsUiState(
    avatarInitials = "M",
    selectedPeriod = TrendsPeriod.WEEKLY,
    summary = PeriodSummaryState(
        title = stringRes(R.string.trends_summary_title),
        body = stringRes(R.string.trends_empty_helper),
        periodLabel = stringRes(R.string.trends_period_last_7_days),
        hasData = false,
    ),
    highlights = emptyList(),
    goalAdherence = emptyList(),
    metricCards = emptyList(),
    insights = emptyList(),
    dataQuality = emptyList(),
)

private fun stringRes(resId: Int, vararg args: Any): UiText = UiText.StringResource(resId = resId, args = args.toList())
