package com.saglik.core.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Immutable
data class RingProgressSegment(
    val progress: Float,
    val color: Color,
)

@Composable
fun RingProgressIndicator(
    segments: List<RingProgressSegment>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(82.dp)) {
        val strokeWidth = 7.dp.toPx()
        val gap = 8.dp.toPx()
        val start = -90f
        segments.forEachIndexed { index, segment ->
            val inset = index * (strokeWidth + gap)
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = segment.color.copy(alpha = 0.13f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = segment.color,
                startAngle = start,
                sweepAngle = segment.progress.coerceIn(0f, 1f) * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
            )
        }
    }
}
