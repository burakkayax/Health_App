package com.burak.healthapp.core.ui.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
fun ConstrainedLargeScreenContainer(
    windowSizeClass: HealthWindowSizeClass,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 1180.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (windowSizeClass.isExpanded) {
                        Modifier.widthIn(max = maxWidth)
                    } else {
                        Modifier.fillMaxWidth()
                    },
                ),
        ) {
            content()
        }
    }
}

@Composable
fun AdaptiveTwoPane(
    windowSizeClass: HealthWindowSizeClass,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(HealthSpacing.sm),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(HealthSpacing.sm),
    firstPane: @Composable () -> Unit,
    secondPane: @Composable () -> Unit,
) {
    if (windowSizeClass.isCompact) {
        Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement,
        ) {
            firstPane()
            secondPane()
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                firstPane()
            }
            Box(modifier = Modifier.weight(1f)) {
                secondPane()
            }
        }
    }
}

@Composable
fun <T> AdaptiveDashboardGrid(
    items: List<T>,
    key: (T) -> Any,
    windowSizeClass: HealthWindowSizeClass,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(HealthSpacing.sm),
    verticalSpacing: Dp = HealthSpacing.sm,
    horizontalSpacing: Dp = HealthSpacing.sm,
    fullSpan: (T) -> Boolean = { false },
    itemContent: @Composable (T) -> Unit,
) {
    if (windowSizeClass.isCompact) {
        LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            items(items = items, key = key) { item ->
                itemContent(item)
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(windowSizeClass.dashboardColumnCount),
            modifier = modifier,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        ) {
            items(
                items = items,
                key = key,
                span = { item ->
                    if (fullSpan(item)) {
                        GridItemSpan(maxLineSpan)
                    } else {
                        GridItemSpan(1)
                    }
                },
            ) { item ->
                itemContent(item)
            }
        }
    }
}
