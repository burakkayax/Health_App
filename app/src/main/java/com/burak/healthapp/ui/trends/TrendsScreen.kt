package com.burak.healthapp.ui.trends

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.ui.components.HealthCard
import com.burak.healthapp.ui.components.InsightCard
import com.burak.healthapp.ui.components.SegmentedControl
import com.burak.healthapp.ui.components.SmoothTrendChart
import com.burak.healthapp.ui.components.WeeklyCaloriesBarChart
import com.burak.healthapp.ui.model.TrendsUiState
import com.burak.healthapp.ui.theme.HealthSpacing

@Composable
fun TrendsRoute(
    avatarInitials: String,
) {
    val viewModel: TrendsViewModel = viewModel(factory = TrendsViewModel.Factory)
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
        state.charts.any { chart -> chart.points.isNotEmpty() } ||
        state.insights.any { insight ->
            insight.value != "0 g" &&
                insight.value != "0 ml" &&
                insight.value != "0s 0d" &&
                insight.value != "0 adım"
        }

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
                options = listOf("Haftalık", "Aylık"),
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
                        text = "Henüz yeterli veri yok",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = "Bugün ekranından su, uyku, öğün ve kilo girişi yaptığında bu alan otomatik dolacak.",
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
                            text = "Haftalık Kalori",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = weeklyCard.averageCaloriesLabel,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = weeklyCard.subtitle,
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
            items(state.insights) { insight ->
                InsightCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = insight.title,
                    value = insight.value,
                    subtitle = insight.subtitle,
                )
            }
            items(state.charts) { chart ->
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = chart.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = chart.subtitle,
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
