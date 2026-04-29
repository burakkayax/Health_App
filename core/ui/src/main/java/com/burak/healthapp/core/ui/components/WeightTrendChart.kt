package com.burak.healthapp.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.burak.healthapp.core.ui.model.WeightTrendChartState
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import kotlin.math.max

@Composable
fun WeightTrendChart(
    state: WeightTrendChartState,
    startLabel: String,
    targetLabel: String,
    currentLabel: String,
    progressLabel: String,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 240.dp,
    lineColor: Color = HealthPrimary,
    targetColor: Color = MaterialTheme.colorScheme.error,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = targetLabel,
                style = MaterialTheme.typography.labelLarge,
                color = targetColor,
            )
            Text(
                text = currentLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .testTag("weight_trend_chart_canvas"),
        ) {
            if (state.points.isEmpty()) return@Canvas

            val horizontalPadding = HealthSpacing.md.toPx()
            val topPadding = HealthSpacing.md.toPx()
            val bottomPadding = HealthSpacing.md.toPx()
            val chartWidth = size.width - (horizontalPadding * 2)
            val chartHeightPx = size.height - topPadding - bottomPadding
            val rawMin = minOf(
                state.points.minOf { it.value },
                state.startWeightKg,
                state.targetWeightKg,
                state.currentWeightKg,
            )
            val rawMax = maxOf(
                state.points.maxOf { it.value },
                state.startWeightKg,
                state.targetWeightKg,
                state.currentWeightKg,
            )
            val span = max(rawMax - rawMin, 1f)
            val padding = max(span * 0.12f, 0.5f)
            val minValue = rawMin - padding
            val maxValue = rawMax + padding

            fun xAt(index: Int): Float {
                if (state.points.size == 1) return size.width / 2f
                return horizontalPadding + (chartWidth / state.points.lastIndex) * index
            }

            fun yAt(value: Float): Float {
                val normalized = (value - minValue) / (maxValue - minValue)
                return topPadding + chartHeightPx - (chartHeightPx * normalized)
            }

            val targetY = yAt(state.targetWeightKg)
            drawLine(
                color = targetColor.copy(alpha = 0.55f),
                start = Offset(horizontalPadding, targetY),
                end = Offset(size.width - horizontalPadding, targetY),
                strokeWidth = 1.dp.toPx(),
            )

            val coordinates = state.points.mapIndexed { index, point ->
                Offset(xAt(index), yAt(point.value))
            }

            val linePath = Path().apply {
                moveTo(coordinates.first().x, coordinates.first().y)
                for (index in 1 until coordinates.size) {
                    val previous = coordinates[index - 1]
                    val current = coordinates[index]
                    val midX = (previous.x + current.x) / 2f
                    cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
                }
            }

            val fillPath = Path().apply {
                moveTo(coordinates.first().x, size.height - bottomPadding)
                lineTo(coordinates.first().x, coordinates.first().y)
                for (index in 1 until coordinates.size) {
                    val previous = coordinates[index - 1]
                    val current = coordinates[index]
                    val midX = (previous.x + current.x) / 2f
                    cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
                }
                lineTo(coordinates.last().x, size.height - bottomPadding)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.24f), lineColor.copy(alpha = 0f)),
                    startY = topPadding,
                    endY = size.height,
                ),
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(
                color = surfaceColor,
                radius = 7.dp.toPx(),
                center = coordinates.last(),
            )
            drawCircle(
                color = lineColor,
                radius = 5.dp.toPx(),
                center = coordinates.last(),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = startLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = progressLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
