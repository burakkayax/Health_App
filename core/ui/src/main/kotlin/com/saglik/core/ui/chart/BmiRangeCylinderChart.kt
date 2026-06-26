package com.saglik.core.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors

@Composable
fun BmiRangeCylinderChart(
    value: Float?,
    modifier: Modifier = Modifier,
    minBmi: Float = 14f,
    maxBmi: Float = 40f,
    contentDescription: String = "BMI range indicator",
) {
    Canvas(
        modifier = modifier
            .semantics { this.contentDescription = contentDescription }
            .defaultMinSize(minHeight = 48.dp),
    ) {
        val safeMin = minOf(minBmi, maxBmi - 0.1f)
        val safeMax = maxOf(maxBmi, minBmi + 0.1f)
        val trackHeight = 18.dp.toPx()
        val trackTop = 24.dp.toPx()
        val radius = trackHeight / 2f
        val trackPath = Path().apply {
            addRoundRect(
                RoundRect(
                    left = 0f,
                    top = trackTop,
                    right = size.width,
                    bottom = trackTop + trackHeight,
                    cornerRadius = CornerRadius(radius, radius),
                ),
            )
        }

        clipPath(trackPath) {
            BmiSegment.entries.forEach { segment ->
                val left = segment.start.positionIn(safeMin, safeMax) * size.width
                val right = segment.end.positionIn(safeMin, safeMax) * size.width
                if (right > left) {
                    drawRect(
                        color = segment.color.copy(alpha = 0.88f),
                        topLeft = Offset(left, trackTop),
                        size = Size(right - left + 1f, trackHeight),
                    )
                }
            }
        }

        val markerValue = value?.takeIf { it.isFinite() } ?: return@Canvas
        val markerX = markerValue.positionIn(safeMin, safeMax) * size.width
        val markerPath = Path().apply {
            moveTo(markerX, trackTop - 3.dp.toPx())
            lineTo(markerX - 7.dp.toPx(), 9.dp.toPx())
            lineTo(markerX + 7.dp.toPx(), 9.dp.toPx())
            close()
        }
        drawPath(markerPath, HealthColors.Ink.copy(alpha = 0.82f))
    }
}

private enum class BmiSegment(
    val start: Float,
    val end: Float,
    val color: Color,
) {
    LOW(14f, 18.5f, HealthColors.WeightBlue),
    HEALTHY(18.5f, 25f, HealthColors.BmiGreen),
    HIGH(25f, 30f, Color(0xFFFFD64D)),
    VERY_HIGH(30f, 40f, Color(0xFFFF5F57)),
}

private fun Float.positionIn(min: Float, max: Float): Float =
    ((this - min) / (max - min)).coerceIn(0f, 1f)
