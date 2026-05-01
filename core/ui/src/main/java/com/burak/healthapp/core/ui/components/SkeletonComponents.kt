package com.burak.healthapp.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
            .clearAndSetSemantics {},
    )
}

@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    width: Dp = 160.dp,
    height: Dp = 14.dp,
) {
    SkeletonBox(
        modifier = modifier.width(width),
        height = height,
    )
}

@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    lines: Int = 3,
) {
    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
    ) {
        repeat(lines) { index ->
            SkeletonText(
                width = when (index) {
                    0 -> 160.dp
                    1 -> 220.dp
                    else -> 120.dp
                },
                height = if (index == 0) 18.dp else 14.dp,
                modifier = Modifier.padding(top = if (index == 0) 0.dp else HealthSpacing.xs),
            )
        }
    }
}

@Composable
fun SkeletonMetricCard(
    modifier: Modifier = Modifier,
) {
    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                SkeletonText(width = 110.dp, height = 16.dp)
                SkeletonText(
                    width = 150.dp,
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                )
                SkeletonText(
                    width = 92.dp,
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                )
            }
            SkeletonBox(
                modifier = Modifier.width(72.dp),
                height = 72.dp,
            )
        }
    }
}

@Composable
fun SkeletonChart(
    modifier: Modifier = Modifier,
) {
    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
    ) {
        SkeletonText(width = 140.dp, height = 18.dp)
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            height = 150.dp,
        )
    }
}

@Composable
fun EmptyGhostChart(
    modifier: Modifier = Modifier,
    testTag: String = "detail_empty_ghost",
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics {}
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
    ) {
        SkeletonBox(
            modifier = Modifier.fillMaxWidth(),
            height = 88.dp,
        )
        SkeletonText(width = 180.dp)
        SkeletonText(width = 120.dp)
    }
}
