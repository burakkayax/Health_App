package com.burak.healthapp.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burak.healthapp.core.ui.R
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthSuccess
import java.time.DayOfWeek
import java.time.LocalDate

@Immutable
data class MetricDayRingState(
    val dayLabel: String,
    val progress: Float,
    val hasData: Boolean,
    val isInCurrentMonth: Boolean,
    val isTargetMet: Boolean,
    val dateLabel: String? = null,
    val valueLabel: String? = null,
    val isToday: Boolean = false,
    val contentDescription: String? = null,
)

@Composable
fun metricWeekdayLabels(): List<String> = listOf(
    stringResource(R.string.weekday_monday_short),
    stringResource(R.string.weekday_tuesday_short),
    stringResource(R.string.weekday_wednesday_short),
    stringResource(R.string.weekday_thursday_short),
    stringResource(R.string.weekday_friday_short),
    stringResource(R.string.weekday_saturday_short),
    stringResource(R.string.weekday_sunday_short),
)

@Composable
fun weekDayShortLabel(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> stringResource(R.string.weekday_monday_short)
    DayOfWeek.TUESDAY -> stringResource(R.string.weekday_tuesday_short)
    DayOfWeek.WEDNESDAY -> stringResource(R.string.weekday_wednesday_short)
    DayOfWeek.THURSDAY -> stringResource(R.string.weekday_thursday_short)
    DayOfWeek.FRIDAY -> stringResource(R.string.weekday_friday_short)
    DayOfWeek.SATURDAY -> stringResource(R.string.weekday_saturday_short)
    DayOfWeek.SUNDAY -> stringResource(R.string.weekday_sunday_short)
}

@Composable
fun MetricMonthRingGrid(
    days: List<MetricDayRingState>,
    weekdayLabels: List<String>,
    modifier: Modifier = Modifier,
    testTag: String = "metric_month_ring_grid",
    activeColor: Color = MaterialTheme.colorScheme.primary,
    targetMetColor: Color = HealthSuccess,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            weekdayLabels.forEach { label ->
                Text(
                    modifier = Modifier.weight(1f),
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        days.chunked(7).forEachIndexed { weekIndex, week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                week.forEachIndexed { dayIndex, day ->
                    DayRingCell(
                        day = day,
                        activeColor = activeColor,
                        targetMetColor = targetMetColor,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("${testTag}_day_${weekIndex}_$dayIndex"),
                    )
                }
            }
        }
    }
}

@Composable
fun DayRingCell(
    day: MetricDayRingState,
    activeColor: Color,
    targetMetColor: Color,
    modifier: Modifier = Modifier,
) {
    val percent = (day.progress.coerceIn(0f, 1f) * 100).toInt()
    val accessibleDayLabel = if (day.isToday) {
        stringResource(R.string.common_today)
    } else {
        day.dateLabel ?: day.dayLabel
    }
    val resolvedContentDescription = day.contentDescription ?: when {
        !day.isInCurrentMonth -> stringResource(R.string.metric_day_ring_outside_month, day.dayLabel)
        !day.hasData -> stringResource(R.string.metric_day_ring_no_data, accessibleDayLabel)
        day.isTargetMet -> stringResource(
            R.string.metric_day_ring_target_met,
            accessibleDayLabel,
            day.valueLabel.orEmpty(),
        )
        else -> stringResource(
            R.string.metric_day_ring_progress,
            accessibleDayLabel,
            day.valueLabel.orEmpty(),
            percent,
        )
    }
    val resolvedActiveColor = when {
        !day.isInCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        day.isTargetMet -> targetMetColor
        day.hasData -> activeColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }
    val textColor = if (day.isInCurrentMonth) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }
    val trackColor = if (day.isInCurrentMonth) {
        resolvedActiveColor.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .semantics {
                contentDescription = resolvedContentDescription
            }
            .then(
                if (day.isToday && day.isInCurrentMonth) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                        shape = androidx.compose.foundation.shape.CircleShape,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(36.dp)) {
            val strokeWidth = 3.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            if (day.hasData && day.isInCurrentMonth) {
                drawArc(
                    color = resolvedActiveColor,
                    startAngle = -90f,
                    sweepAngle = 360f * day.progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}
