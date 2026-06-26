package com.saglik.feature.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.ui.chart.MiniLineChart
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.core.ui.component.HealthNumberInput
import com.saglik.core.ui.component.HealthPrimaryPillButton

@Composable
fun WeightDetailScreen(
    state: WeightDetailUiState,
    onWeightInputChanged: (String) -> Unit,
    onAddWeightClick: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.cardGap),
    ) {
        item {
            WeightTrendHeroCard(state = state)
        }
        item {
            BmiSectionCard(bmi = state.bmi)
        }
        item {
            AddWeightCard(
                state = state,
                onWeightInputChanged = onWeightInputChanged,
                onAddWeightClick = onAddWeightClick,
            )
        }
        item {
            WeightHistoryCard(history = state.history)
        }
    }
}

@Composable
private fun WeightTrendHeroCard(
    state: WeightDetailUiState,
    modifier: Modifier = Modifier,
) {
    GlassHealthCard(modifier = modifier) {
        HealthCardHeader(
            title = "Weight Trend",
            trailingText = "All Time",
            accentColor = HealthColors.WeightBlue,
            icon = Icons.Rounded.AutoGraph,
            showChevron = false,
        )
        Text(
            text = state.latestWeightText,
            modifier = Modifier.padding(top = 22.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = HealthColors.Ink,
        )
        Text(
            text = state.latestEntryText,
            modifier = Modifier.padding(top = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = HealthColors.SecondaryText,
        )
        TrendStatRow(
            label = "Highest",
            value = state.highestWeightText,
            modifier = Modifier.padding(top = 18.dp),
        )
        if (state.trend.isEmpty()) {
            Text(
                text = "Add weight entries to see your all-time trend.",
                modifier = Modifier.padding(top = 18.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.SecondaryText,
            )
        } else {
            MiniLineChart(
                values = state.trend,
                color = HealthColors.WeightBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .padding(top = 18.dp),
            )
        }
        TrendStatRow(
            label = "Lowest",
            value = state.lowestWeightText,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun TrendStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = HealthColors.SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = HealthColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AddWeightCard(
    state: WeightDetailUiState,
    onWeightInputChanged: (String) -> Unit,
    onAddWeightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassHealthCard(modifier = modifier) {
        HealthCardHeader(
            title = "Add Weight",
            accentColor = HealthColors.SystemBlue,
            icon = Icons.Rounded.Add,
            showChevron = false,
        )
        HealthNumberInput(
            value = state.addWeightValue,
            onValueChange = onWeightInputChanged,
            label = "Weight",
            suffix = "kg",
            isError = state.errorMessage != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        )
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.ActivityOrange,
            )
        }
        HealthPrimaryPillButton(
            text = if (state.isSaving) "Saving" else "Add Weight",
            onClick = onAddWeightClick,
            enabled = state.canSave,
            modifier = Modifier.padding(top = 18.dp),
        )
    }
}

@Composable
private fun WeightHistoryCard(
    history: List<WeightHistoryUiState>,
    modifier: Modifier = Modifier,
) {
    GlassHealthCard(modifier = modifier) {
        HealthCardHeader(
            title = "History",
            accentColor = HealthColors.Ink,
            icon = Icons.Rounded.History,
            showChevron = false,
        )
        if (history.isEmpty()) {
            Text(
                text = "Your saved weights will appear here.",
                modifier = Modifier.padding(top = 18.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.SecondaryText,
            )
        } else {
            Column(
                modifier = Modifier.padding(top = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                history.forEachIndexed { index, item ->
                    WeightHistoryRow(item = item)
                    if (index != history.lastIndex) {
                        HorizontalDivider(
                            color = HealthColors.GlassBorder.copy(alpha = 0.58f),
                            thickness = 1.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightHistoryRow(
    item: WeightHistoryUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.dateText,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = HealthColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = item.weightText,
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = HealthColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
