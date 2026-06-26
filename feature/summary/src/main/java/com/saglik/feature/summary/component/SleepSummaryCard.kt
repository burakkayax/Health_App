package com.saglik.feature.summary.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.chart.MiniBarChart
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.feature.summary.SleepSummaryUiState

@Composable
fun SleepSummaryCard(
    summary: SleepSummaryUiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    GlassHealthCard(modifier = modifier, onClick = onClick) {
        HealthCardHeader(
            title = "Sleep",
            trailingText = if (summary.hasData) "Today" else "Add data",
            accentColor = HealthColors.SleepPurple,
            icon = Icons.Rounded.NightsStay,
        )
        Row(
            modifier = Modifier.padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SummaryValueText(text = summary.duration)
                SummarySecondaryText(
                    text = summary.quality.orEmpty(),
                    color = HealthColors.SleepPurple,
                )
            }
            if (summary.weeklyHours.any { it > 0f }) {
                MiniBarChart(
                    values = summary.weeklyHours,
                    color = HealthColors.SleepPurple,
                    modifier = Modifier
                        .width(128.dp)
                        .height(58.dp),
                )
            }
        }
    }
}
