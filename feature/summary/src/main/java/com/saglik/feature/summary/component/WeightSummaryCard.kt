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
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.feature.summary.WeightSummary

@Composable
fun WeightSummaryCard(
    summary: WeightSummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    GlassHealthCard(modifier = modifier, onClick = onClick) {
        HealthCardHeader(
            title = "Weight",
            trailingText = "Today",
            accentColor = HealthColors.WeightBlue,
            icon = Icons.Rounded.AutoGraph,
        )
        Row(
            modifier = Modifier.padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SummaryValueText(text = summary.value)
                SummarySecondaryText(
                    text = summary.delta,
                    color = HealthColors.WeightBlue,
                )
            }
            MiniLineChart(
                values = summary.trend,
                color = HealthColors.WeightBlue,
                modifier = Modifier
                    .width(118.dp)
                    .height(58.dp),
            )
        }
    }
}
