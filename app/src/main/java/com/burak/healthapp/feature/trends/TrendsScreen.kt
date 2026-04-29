package com.burak.healthapp.feature.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.InsightCard
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.SmoothTrendChart
import com.burak.healthapp.core.ui.components.WeeklyCaloriesBarChart
import com.burak.healthapp.core.ui.components.WeightTrendChart
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.TrendsPeriod

@Composable
fun TrendsRoute(
    avatarInitials: String,
) {
    val viewModel: TrendsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrendsContent(
        state = uiState.copy(avatarInitials = avatarInitials),
        onSelectPeriod = viewModel::selectPeriod,
    )
}

@Composable
fun TrendsContent(
    state: TrendsUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
) {
    val hasMeaningfulData = state.weeklyCaloriesCard?.bars?.any { it.calories > 0 } == true ||
        state.weightChart?.chart?.points?.isNotEmpty() == true ||
        state.charts.any { chart -> chart.points.isNotEmpty() } ||
        state.insights.any { insight -> insight.hasData }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
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
                modifier = Modifier.fillMaxWidth(),
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

        if (!hasMeaningfulData) {
            item {
                HealthCard(modifier = Modifier.fillMaxWidth()) {
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
                }
            }
        } else {
            state.weeklyCaloriesCard?.let { weeklyCard ->
                item {
                    HealthCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weekly_calories_card"),
                    ) {
                        Text(
                            text = stringResource(R.string.trends_weekly_calories_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = weeklyCard.averageCaloriesLabel.asString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = weeklyCard.subtitle.asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        WeeklyCaloriesBarChart(
                            modifier = Modifier.padding(top = HealthSpacing.sm),
                            bars = weeklyCard.bars,
                        )
                    }
                }
            }
            items(
                items = state.insights,
                key = { insight -> insight.title.toString() },
            ) { insight ->
                InsightCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = insight.title.asString(),
                    value = insight.value.asString(),
                    subtitle = insight.subtitle.asString(),
                )
            }
            state.weightChart?.let { weightChart ->
                item {
                    HealthCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trends_weight_chart_card"),
                    ) {
                        Text(
                            text = weightChart.title.asString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = weightChart.subtitle.asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        WeightTrendChart(
                            state = weightChart.chart,
                            startLabel = stringResource(
                                R.string.weight_chart_start,
                                weightChart.chart.startWeightKg,
                            ),
                            targetLabel = stringResource(
                                R.string.weight_chart_target,
                                weightChart.chart.targetWeightKg,
                            ),
                            currentLabel = stringResource(
                                R.string.weight_chart_current,
                                weightChart.chart.currentWeightKg,
                            ),
                            progressLabel = stringResource(
                                R.string.weight_chart_progress,
                                (weightChart.chart.progress * 100).toInt(),
                            ),
                            modifier = Modifier
                                .padding(top = HealthSpacing.sm)
                                .testTag("trends_weight_chart"),
                        )
                    }
                }
            }
            items(
                items = state.charts,
                key = { chart -> chart.title.toString() },
            ) { chart ->
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = chart.title.asString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = chart.subtitle.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    SmoothTrendChart(
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        points = chart.points,
                    )
                }
            }
        }
    }
}
