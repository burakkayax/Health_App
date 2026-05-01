package com.burak.healthapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.model.BmiGaugeState
import com.burak.healthapp.core.ui.model.WeeklyCalorieBarState
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthFat
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthSuccess
import com.burak.healthapp.core.ui.theme.HealthWater
import com.burak.healthapp.domain.model.TrendPoint

@Composable
fun SmoothTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    chartHeight: Dp = 180.dp,
) {
    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight),
    ) {
        if (points.isEmpty()) return@Canvas

        val horizontalPadding = HealthSpacing.sm.toPx()
        val topPadding = HealthSpacing.sm.toPx()
        val bottomPadding = HealthSpacing.md.toPx()
        val chartWidth = size.width - (horizontalPadding * 2)
        val chartHeightPx = size.height - topPadding - bottomPadding
        val minValue = points.minOf { it.value }
        val maxValue = kotlin.math.max(points.maxOf { it.value }, minValue + 1f)

        fun xAt(index: Int): Float {
            if (points.size == 1) return size.width / 2f
            return horizontalPadding + (chartWidth / points.lastIndex) * index
        }

        fun yAt(value: Float): Float {
            val normalized = (value - minValue) / (maxValue - minValue)
            return topPadding + chartHeightPx - (chartHeightPx * normalized)
        }

        val coordinates = points.mapIndexed { index, point ->
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
                colors = listOf(color.copy(alpha = 0.26f), color.copy(alpha = 0f)),
                startY = topPadding,
                endY = size.height,
            ),
        )
        drawPath(
            path = linePath,
            color = color,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
        )
        if (coordinates.size == 1) {
            drawCircle(
                color = color,
                radius = 5.dp.toPx(),
                center = coordinates.first(),
            )
        }
    }
}

@Composable
fun BmiGaugeChart(
    state: BmiGaugeState,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
    ) {
        if (state.helperMessage != null || state.indicatorFraction == null || state.valueLabel == null) {
            Text(
                text = state.helperMessage ?: "VKİ hesaplanamıyor.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val clusterWidth = 112.dp
            val rawOffset = (maxWidth * state.indicatorFraction) - (clusterWidth / 2)
            val indicatorOffset = rawOffset.coerceIn(0.dp, maxWidth - clusterWidth)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                Column(
                    modifier = Modifier
                        .width(clusterWidth)
                        .offset(x = indicatorOffset),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.valueLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    GaugeSegment(segmentWeight = 3.5f, color = HealthWater, isFirst = true)
                    GaugeSegment(segmentWeight = 6.5f, color = HealthSuccess)
                    GaugeSegment(segmentWeight = 5f, color = HealthCarbs)
                    GaugeSegment(segmentWeight = 10f, color = HealthFat, isLast = true)
                }
            }
        }
    }
}

@Composable
private fun RowScope.GaugeSegment(
    segmentWeight: Float,
    color: Color,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    Box(
        modifier = Modifier
            .weight(segmentWeight)
            .fillMaxHeight()
            .background(
                color = color,
                shape = RoundedCornerShape(
                    topStart = if (isFirst) 999.dp else 6.dp,
                    bottomStart = if (isFirst) 999.dp else 6.dp,
                    topEnd = if (isLast) 999.dp else 6.dp,
                    bottomEnd = if (isLast) 999.dp else 6.dp,
                ),
            ),
    )
}

@Composable
fun WeeklyCaloriesBarChart(
    bars: List<WeeklyCalorieBarState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(190.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        bars.forEach { bar ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = formatWholeNumber(bar.calories),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(22.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(999.dp),
                        )
                        .padding(2.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(bar.progress.coerceIn(0f, 1f))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.66f),
                                    ),
                                ),
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
