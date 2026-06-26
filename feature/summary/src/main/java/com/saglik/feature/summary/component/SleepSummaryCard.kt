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
    com.saglik.core.ui.component.card.HealthSummaryMetricCard(
        title = "Sleep",
        icon = Icons.Rounded.NightsStay,
        accentColor = HealthColors.SleepPurple,
        mainValue = summary.duration,
        secondaryText = summary.quality.orEmpty(),
        trailingText = if (summary.hasData) "Today" else "Add data",
        modifier = modifier,
        isEmpty = !summary.hasData,
        onClick = onClick,
        contentSlot = {
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
    )
}
