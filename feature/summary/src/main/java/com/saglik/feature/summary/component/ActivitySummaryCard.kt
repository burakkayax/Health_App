package com.saglik.feature.summary.component

import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.chart.RingProgressIndicator
import com.saglik.core.ui.chart.RingProgressSegment
import com.saglik.feature.summary.ActivitySummary

@Composable
fun ActivitySummaryCard(
    summary: ActivitySummary,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    GlassHealthCard(modifier = modifier, onClick = onClick) {
        HealthCardHeader(
            title = "Activity",
            trailingText = "9:41 AM",
            accentColor = HealthColors.ActivityOrange,
            icon = Icons.Rounded.Favorite,
        )
        Row(
            modifier = Modifier.padding(top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActivityMetric(label = "Move", value = summary.move)
                ActivityDivider()
                ActivityMetric(label = "Exercise", value = summary.exercise)
                ActivityDivider()
                ActivityMetric(label = "Stand", value = summary.stand)
            }
            RingProgressIndicator(
                segments = listOf(
                    RingProgressSegment(0.72f, HealthColors.ActivityOrange),
                    RingProgressSegment(0.42f, HealthColors.BmiGreen),
                    RingProgressSegment(0.28f, HealthColors.WeightBlue),
                ),
            )
        }
    }
}

@Composable
private fun ActivityMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(68.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = HealthColors.SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = HealthColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ActivityDivider() {
    Box(
        modifier = Modifier
            .height(42.dp)
            .width(1.dp)
            .background(HealthColors.Ink.copy(alpha = 0.08f)),
    )
}
