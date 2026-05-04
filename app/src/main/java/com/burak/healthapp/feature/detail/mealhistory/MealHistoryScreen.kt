package com.burak.healthapp.feature.detail.mealhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.adaptive.isCompact
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import java.time.LocalDate
import androidx.compose.foundation.lazy.grid.items as gridItems

@Composable
fun MealHistoryRoute(
    selectedDate: LocalDate,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    val viewModel: MealHistoryViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    MealHistoryContent(
        state = uiState,
        onDeleteMeal = viewModel::deleteMeal,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun MealHistoryContent(
    state: MealHistoryUiState,
    onDeleteMeal: (Long) -> Unit,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    if (!windowSizeClass.isCompact && state.sections.isNotEmpty()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .testTag("meal_history_adaptive_grid"),
            contentPadding = PaddingValues(
                start = HealthSpacing.sm,
                end = HealthSpacing.sm,
                top = HealthSpacing.xs,
                bottom = HealthSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            state.dailySummary?.let { summary ->
                item(
                    key = "daily_summary",
                    span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) },
                ) {
                    DailySummaryCard(summary = summary)
                }
            }
            gridItems(state.sections, key = MealHistorySectionState::titleResId) { section ->
                MealHistorySection(
                    section = section,
                    onDeleteMeal = onDeleteMeal,
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("meal_history_list"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        if (state.sections.isEmpty()) {
            item {
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.meal_history_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = stringResource(R.string.meal_history_empty_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            state.dailySummary?.let { summary ->
                item(key = "daily_summary") {
                    DailySummaryCard(summary = summary)
                }
            }
            items(state.sections, key = MealHistorySectionState::titleResId) { section ->
                MealHistorySection(
                    section = section,
                    onDeleteMeal = onDeleteMeal,
                )
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    summary: MealHistoryDailySummary,
    modifier: Modifier = Modifier,
) {
    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meal_history_daily_summary"),
    ) {
        Text(
            text = stringResource(R.string.meal_history_daily_summary_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.meal_history_total_calories, summary.totalCalories),
            style = MaterialTheme.typography.titleSmall,
            color = HealthPrimary,
        )
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = stringResource(
                R.string.meal_history_macro_distribution,
                summary.totalProtein,
                summary.totalCarbs,
                summary.totalFat,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (summary.macroDistribution != null) {
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = stringResource(
                    R.string.meal_history_macro_percent_distribution,
                    summary.macroDistribution.proteinPercent,
                    summary.macroDistribution.carbsPercent,
                    summary.macroDistribution.fatPercent,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = stringResource(
                R.string.meal_history_meal_count,
                summary.mealCount,
                summary.foodCount,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            text = stringResource(R.string.meal_history_snapshot_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun MealHistorySection(
    section: MealHistorySectionState,
    onDeleteMeal: (Long) -> Unit,
) {
    HealthCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(section.titleResId),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        section.entries.forEachIndexed { index, entry ->
            if (index > 0) {
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = stringResource(
                            R.string.meal_history_entry_macros,
                            entry.calories,
                            entry.proteinGrams,
                            entry.carbsGrams,
                            entry.fatGrams,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    modifier = Modifier.testTag("meal_history_delete_${entry.id}"),
                    onClick = { onDeleteMeal(entry.id) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = stringResource(R.string.content_description_delete_meal),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
