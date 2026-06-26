package com.saglik.feature.sleep.component

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
import com.saglik.core.model.ChartPoint
import kotlin.math.max

@Composable
fun SleepBarChart(
    points: List<ChartPoint>,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String = "Sleep duration chart",
) {
    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .defaultMinSize(minHeight = 124.dp),
    ) {
        if (points.isEmpty()) return@Canvas

        val maxValue = max(1f, points.maxOf { it.value })
        val gap = if (points.size > 14) 3.dp.toPx() else 7.dp.toPx()
        val barWidth = ((size.width - gap * (points.size - 1)) / points.size)
            .coerceAtLeast(3.dp.toPx())
        val radius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        val baselineY = size.height - 1.dp.toPx()

        drawLine(
            color = color.copy(alpha = 0.12f),
            start = Offset(0f, baselineY),
            end = Offset(size.width, baselineY),
            strokeWidth = 1.dp.toPx(),
        )

        points.forEachIndexed { index, point ->
            val left = index * (barWidth + gap)
            drawRoundRect(
                color = color.copy(alpha = 0.08f),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = radius,
            )
            if (point.value > 0f) {
                val height = (point.value / maxValue).coerceIn(0.12f, 1f) * size.height
                drawRoundRect(
                    color = color.copy(alpha = if (index == points.lastIndex) 0.96f else 0.58f),
                    topLeft = Offset(left, size.height - height),
                    size = Size(barWidth, height),
                    cornerRadius = radius,
                )
            }
        }
    }
}
