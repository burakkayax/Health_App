package com.saglik.feature.sleep.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.model.PeriodType
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.feature.sleep.SleepDetailUiState

@Composable
fun SleepTrendCard(
    state: SleepDetailUiState,
    onPeriodSelected: (PeriodType) -> Unit,
    modifier: Modifier = Modifier,
) {
    com.saglik.core.ui.component.card.HealthDetailHeroCard(
        title = "Sleep Trend",
        mainValue = state.latestDurationText,
        secondaryText = "Latest sleep",
        modifier = modifier,
        contentSlot = {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SleepPeriodSelector(
                    selectedPeriod = state.selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                )
            }
            SleepStatRow(
                label = "Longest",
                value = state.longestText ?: "Not available",
                modifier = Modifier.padding(top = 8.dp),
            )
            if (state.chartPoints.none { it.value > 0f }) {
                com.saglik.core.ui.component.state.HealthEmptyState(
                    message = "Your sleep trend will appear here.",
                    modifier = Modifier.padding(top = 18.dp)
                )
            } else {
                com.saglik.core.ui.component.chart.HealthChartContainer(
                    modifier = Modifier.padding(top = 18.dp)
                ) {
                    SleepBarChart(
                        points = state.chartPoints,
                        color = HealthColors.SleepPurple,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            SleepStatRow(
                label = "Shortest",
                value = state.shortestText ?: "Not available",
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    )
}

@Composable
private fun SleepPeriodSelector(
    selectedPeriod: PeriodType,
    onPeriodSelected: (PeriodType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(min = 132.dp)
            .clip(HealthShapeTokens.pill)
            .border(1.dp, HealthColors.GlassBorder, HealthShapeTokens.pill)
            .background(HealthColors.GlassSurface.copy(alpha = 0.52f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        PeriodType.entries.forEach { period ->
            val selected = selectedPeriod == period
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(HealthShapeTokens.pill)
                    .background(
                        if (selected) HealthColors.SleepPurple.copy(alpha = 0.94f) else HealthColors.GlassSurface.copy(alpha = 0f),
                        CircleShape,
                    )
                    .clickable { onPeriodSelected(period) }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = when (period) {
                        PeriodType.WEEKLY -> "Weekly"
                        PeriodType.MONTHLY -> "Monthly"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) androidx.compose.ui.graphics.Color.White else HealthColors.SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun SleepStatRow(
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
