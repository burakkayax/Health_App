package com.saglik.feature.summary.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.chart.MiniLineChart
import com.saglik.feature.summary.WeightSummary

@Composable
fun WeightSummaryCard(
    summary: WeightSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    com.saglik.core.ui.component.card.HealthSummaryMetricCard(
        title = "Weight",
        icon = Icons.Rounded.AutoGraph,
        accentColor = HealthColors.WeightBlue,
        mainValue = summary.value,
        secondaryText = summary.delta,
        trailingText = "Today",
        modifier = modifier,
        isEmpty = summary.trend.isEmpty(),
        onClick = onClick,
        contentSlot = {
            MiniLineChart(
                values = summary.trend,
                color = HealthColors.WeightBlue,
                modifier = Modifier
                    .width(118.dp)
                    .height(58.dp),
            )
        }
    )
}
