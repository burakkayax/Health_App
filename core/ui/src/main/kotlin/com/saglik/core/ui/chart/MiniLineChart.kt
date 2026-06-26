package com.saglik.core.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun MiniLineChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String = "Weight trend chart",
) {
    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .defaultMinSize(minWidth = 112.dp, minHeight = 52.dp),
    ) {
        if (values.isEmpty()) return@Canvas
        if (values.size == 1) {
            val point = Offset(size.width * 0.5f, size.height * 0.5f)
            drawCircle(
                color = color.copy(alpha = 0.12f),
                radius = 14.dp.toPx(),
                center = point,
            )
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
            drawCircle(color = color, radius = 3.8.dp.toPx(), center = point)
            return@Canvas
        }
        val minValue = values.minOrNull() ?: 0f
        val maxValue = values.maxOrNull() ?: 1f
        val range = max(0.01f, maxValue - minValue)
        val xStep = size.width / (values.lastIndex)
        val verticalPadding = size.height * 0.18f
        val points = values.mapIndexed { index, value ->
            val normalized = (value - minValue) / range
            Offset(
                x = index * xStep,
                y = size.height - verticalPadding - normalized * (size.height - verticalPadding * 2f),
            )
        }
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = path,
            color = color.copy(alpha = 0.14f),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
        )
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round),
        )
        points.forEachIndexed { index, point ->
            if (index == points.lastIndex) {
                drawCircle(color = Color.White, radius = 5.5.dp.toPx(), center = point)
                drawCircle(color = color, radius = 3.4.dp.toPx(), center = point)
            }
        }
    }
}
