package com.saglik.core.ui.component.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.saglik.core.ui.chart.MiniBarChart

@Composable
fun HealthBarChart(
    dataPoints: List<Float>,
    barColor: Color,
    modifier: Modifier = Modifier,
) {
    // Placeholder foundation for bar chart
    MiniBarChart(
        values = dataPoints,
        color = barColor,
        modifier = modifier
    )
}
