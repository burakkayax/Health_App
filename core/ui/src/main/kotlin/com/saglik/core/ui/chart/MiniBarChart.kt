package com.saglik.core.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun MiniBarChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String = "Sleep duration chart",
) {
    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .defaultMinSize(minWidth = 126.dp, minHeight = 58.dp),
    ) {
        if (values.isEmpty()) return@Canvas
        val maxValue = max(0.01f, values.maxOrNull() ?: 1f)
        val gap = 6.dp.toPx()
        val barWidth = (size.width - gap * (values.size - 1)) / values.size
        val radius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
        values.forEachIndexed { index, value ->
            val height = (value / maxValue).coerceIn(0.12f, 1f) * size.height
            val left = index * (barWidth + gap)
            drawRoundRect(
                color = color.copy(alpha = 0.14f),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = radius,
            )
            drawRoundRect(
                color = color.copy(alpha = if (index == values.lastIndex) 0.96f else 0.56f),
                topLeft = Offset(left, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = radius,
            )
        }
    }
}
