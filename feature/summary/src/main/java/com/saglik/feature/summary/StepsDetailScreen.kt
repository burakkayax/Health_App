package com.saglik.feature.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import com.saglik.core.ui.component.card.HealthDetailHeroCard
import com.saglik.core.ui.component.card.HealthHistoryCard
import com.saglik.core.ui.component.state.HealthEmptyState

@Composable
fun StepsDetailScreen(
    state: StepsDetailUiState,
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
            HealthDetailHeroCard(
                title = "Steps Today",
                mainValue = state.todayStepsText,
                secondaryText = state.todayLabelText,
            ) {
                DetailMetricRow(
                    label = "Last 7 days",
                    value = state.lastSevenDaysText,
                )
                DetailMetricRow(
                    label = "Latest entry",
                    value = state.latestEntryText,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }
        item {
            StepsHistoryCard(history = state.historyItems)
        }
    }
}

@Composable
private fun DetailMetricRow(
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
private fun StepsHistoryCard(
    history: List<StepsHistoryItemUiState>,
    modifier: Modifier = Modifier,
) {
    HealthHistoryCard(
        title = "Recent entries",
        modifier = modifier,
    ) {
        if (history.isEmpty()) {
            HealthEmptyState(message = "No steps yet\nSync Health Connect to import steps.")
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                history.forEachIndexed { index, item ->
                    StepsHistoryRow(item = item)
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
private fun StepsHistoryRow(
    item: StepsHistoryItemUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = item.dateText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = HealthColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.timeRangeText,
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.sourceText,
                style = MaterialTheme.typography.bodySmall,
                color = HealthColors.TertiaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.countText,
            modifier = Modifier.padding(start = 12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = HealthColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
