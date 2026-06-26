package com.saglik.core.ui.component.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.saglik.core.ui.chart.MiniLineChart

@Composable
fun HealthMiniLineChart(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    MiniLineChart(
        values = dataPoints,
        color = lineColor,
        modifier = modifier
    )
}

@Composable
fun HealthLineChart(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    // Placeholder wrapper for future fully featured line chart
    MiniLineChart(
        values = dataPoints,
        color = lineColor,
        modifier = modifier
    )
}
