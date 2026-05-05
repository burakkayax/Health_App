package com.burak.healthapp.feature.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burak.healthapp.R
import com.burak.healthapp.core.performance.DebugRoutePerformanceTrace
import com.burak.healthapp.core.ui.adaptive.ConstrainedLargeScreenContainer
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.adaptive.isCompact
import com.burak.healthapp.core.ui.components.EmptyGhostChart
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.SkeletonCard
import com.burak.healthapp.core.ui.components.SkeletonChart
import com.burak.healthapp.core.ui.components.SkeletonMetricCard
import com.burak.healthapp.core.ui.components.SmoothTrendChart
import com.burak.healthapp.core.ui.components.ThickMetricProgressBar
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.TrendsPeriod

@Composable
fun TrendsRoute(
    avatarInitials: String,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
    onOpenDetail: (TrendsDetailDestination) -> Unit = {},
) {
    DebugRoutePerformanceTrace("TrendsRoute")
    val viewModel: TrendsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrendsContent(
        state = uiState.copy(avatarInitials = avatarInitials),
        windowSizeClass = windowSizeClass,
        onSelectPeriod = viewModel::selectPeriod,
        onOpenDetail = onOpenDetail,
    )
}

@Composable
fun TrendsContent(
    state: TrendsUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
    onOpenDetail: (TrendsDetailDestination) -> Unit = {},
) {
    ConstrainedLargeScreenContainer(
        windowSizeClass = windowSizeClass,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("trends_screen"),
    ) {
        if (state.isLoading) {
            TrendsSkeletonContent()
            return@ConstrainedLargeScreenContainer
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = HealthSpacing.sm,
                end = HealthSpacing.sm,
                top = HealthSpacing.xs,
                bottom = HealthSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            item {
                SegmentedControl(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("trends_period_selector"),
                    options = listOf(
                        stringResource(R.string.common_weekly),
                        stringResource(R.string.common_monthly),
                    ),
                    selectedIndex = if (state.selectedPeriod == TrendsPeriod.WEEKLY) 0 else 1,
                    onSelectionChange = { index ->
                        onSelectPeriod(if (index == 0) TrendsPeriod.WEEKLY else TrendsPeriod.MONTHLY)
                    },
                )
            }

            item {
                PeriodSummaryCard(state.summary)
            }

            if (!state.summary.hasData) {
                item {
                    EmptyTrendsCard()
                }
            } else {
                item {
                    HighlightsSection(
                        highlights = state.highlights,
                        windowSizeClass = windowSizeClass,
                    )
                }
                item {
                    GoalAdherenceSection(
                        items = state.goalAdherence,
                        windowSizeClass = windowSizeClass,
                    )
                }
                item {
                    MetricCardsSection(
                        cards = state.metricCards,
                        windowSizeClass = windowSizeClass,
                        onOpenDetail = onOpenDetail,
                    )
                }
                state.sleepStability?.let { stability ->
                    item {
                        SleepStabilityCard(stability)
                    }
                }
                item {
                    ShortInsightsSection(
                        insights = state.insights,
                        windowSizeClass = windowSizeClass,
                    )
                }
                item {
                    DataQualitySection(warnings = state.dataQuality)
                }
            }
        }
    }
}

@Composable
private fun SleepStabilityCard(state: SleepStabilityCardState) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trends_sleep_stability_card"),
    ) {
        Text(
            text = stringResource(R.string.trends_sleep_stability_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (state.hasData) {
            Text(
                modifier = Modifier
                    .padding(top = HealthSpacing.xs)
                    .testTag("sleep_stability_avg_bedtime"),
                text = state.averageBedtimeLabel.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier
                    .padding(top = HealthSpacing.xs)
                    .testTag("sleep_stability_avg_wake"),
                text = state.averageWakeTimeLabel.asString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (state.status == com.burak.healthapp.domain.model.SleepStabilityStatus.READY) {
                Text(
                    modifier = Modifier
                        .padding(top = HealthSpacing.xs)
                        .testTag("sleep_stability_bedtime_variability"),
                    text = state.bedtimeVariabilityLabel.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    modifier = Modifier
                        .padding(top = HealthSpacing.xs)
                        .testTag("sleep_stability_wake_variability"),
                    text = state.wakeTimeVariabilityLabel.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = state.targetDeviationLabel.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            text = state.insight.asString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PeriodSummaryCard(summary: PeriodSummaryState) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trends_summary_card"),
    ) {
        Text(
            text = summary.title.asString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = summary.periodLabel.asString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = summary.body.asString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyTrendsCard() {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trends_empty_state"),
    ) {
        Text(
            text = stringResource(R.string.trends_empty_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.trends_empty_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        EmptyGhostChart(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            testTag = "trends_empty_ghost",
        )
    }
}

@Composable
private fun TrendsSkeletonContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("trends_skeleton"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item { SkeletonCard(lines = 1) }
        item { SkeletonCard(lines = 3) }
        item { SkeletonMetricCard() }
        item { SkeletonMetricCard() }
        item { SkeletonChart() }
        item { SkeletonCard(lines = 2) }
    }
}

@Composable
private fun HighlightsSection(
    highlights: List<TrendHighlightState>,
    windowSizeClass: HealthWindowSizeClass,
) {
    TrendsSection(
        title = stringResource(R.string.trends_highlights_title),
        testTag = "trends_highlights_section",
    ) {
        AdaptiveSmallCardGrid(windowSizeClass = windowSizeClass) {
            highlights.forEach { highlight ->
                ToneCard(tone = highlight.tone) {
                    Text(
                        text = highlight.title.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = highlight.value.asString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = highlight.description.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalAdherenceSection(
    items: List<GoalAdherenceState>,
    windowSizeClass: HealthWindowSizeClass,
) {
    TrendsSection(
        title = stringResource(R.string.trends_goal_adherence_title),
        testTag = "trends_goal_adherence_section",
    ) {
        AdaptiveSmallCardGrid(windowSizeClass = windowSizeClass) {
            items.forEach { item ->
                ToneCard(tone = item.tone) {
                    Text(
                        text = item.label.asString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = stringResource(
                            R.string.trends_goal_adherence_days,
                            item.completedDays,
                            item.totalDays,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ThickMetricProgressBar(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        progress = item.progress,
                        activeColor = item.tone.color(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCardsSection(
    cards: List<MetricTrendCardState>,
    windowSizeClass: HealthWindowSizeClass,
    onOpenDetail: (TrendsDetailDestination) -> Unit,
) {
    TrendsSection(
        title = stringResource(R.string.trends_metric_cards_title),
        testTag = "trends_metric_cards_section",
    ) {
        AdaptiveSmallCardGrid(windowSizeClass = windowSizeClass) {
            cards.forEach { card ->
                val destination = card.destination
                ToneCard(
                    tone = card.tone,
                    modifier = Modifier.then(
                        if (destination != null) {
                            Modifier.clickable { onOpenDetail(destination) }
                        } else {
                            Modifier
                        },
                    ),
                ) {
                    Text(
                        text = card.title.asString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = card.primaryValue.asString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = card.secondaryValue.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = card.changeLabel.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = card.tone.color(),
                    )
                    if (card.chartPoints.isNotEmpty()) {
                        SmoothTrendChart(
                            modifier = Modifier.padding(top = HealthSpacing.sm),
                            points = card.chartPoints,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortInsightsSection(
    insights: List<ShortInsightState>,
    windowSizeClass: HealthWindowSizeClass,
) {
    TrendsSection(
        title = stringResource(R.string.trends_short_insights_title),
        testTag = "trends_short_insights_section",
    ) {
        AdaptiveSmallCardGrid(windowSizeClass = windowSizeClass) {
            insights.forEach { insight ->
                ToneCard(tone = insight.severity) {
                    Text(
                        text = insight.title.asString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = insight.body.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun DataQualitySection(warnings: List<DataQualityWarningState>) {
    TrendsSection(
        title = stringResource(R.string.trends_data_quality_title),
        testTag = "trends_data_quality_section",
    ) {
        if (warnings.isEmpty()) {
            ToneCard(tone = TrendTone.POSITIVE) {
                Text(
                    text = stringResource(R.string.trends_data_quality_ok),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                warnings.forEach { warning ->
                    ToneCard(tone = TrendTone.WARNING) {
                        Text(
                            text = warning.message.asString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendsSection(
    title: String,
    testTag: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        content()
    }
}

@Composable
private fun AdaptiveSmallCardGrid(
    windowSizeClass: HealthWindowSizeClass,
    content: @Composable () -> Unit,
) {
    if (windowSizeClass.isCompact) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            content()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ToneCard(
    tone: TrendTone,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    HealthCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = when (tone) {
                TrendTone.POSITIVE -> stringResource(R.string.trends_tone_positive)
                TrendTone.WARNING -> stringResource(R.string.trends_tone_warning)
                TrendTone.NEUTRAL -> stringResource(R.string.trends_tone_neutral)
            },
            style = MaterialTheme.typography.labelSmall,
            color = tone.color(),
        )
        content()
    }
}

@Composable
private fun TrendTone.color() = when (this) {
    TrendTone.POSITIVE -> MaterialTheme.colorScheme.primary
    TrendTone.WARNING -> MaterialTheme.colorScheme.error
    TrendTone.NEUTRAL -> HealthPrimary
}
