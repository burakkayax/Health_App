package com.burak.healthapp.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.burak.healthapp.core.ui.components.SkeletonCard
import com.burak.healthapp.core.ui.components.SkeletonChart
import com.burak.healthapp.core.ui.components.SkeletonMetricCard
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
fun DetailSkeletonContent(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("detail_skeleton"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item { SkeletonCard(lines = 1) }
        item { SkeletonChart() }
        item { SkeletonMetricCard() }
        item { SkeletonMetricCard() }
        item { SkeletonCard(lines = 2) }
    }
}
