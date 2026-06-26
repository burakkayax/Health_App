package com.saglik.core.ui.component.picker

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthTypography

@Composable
fun HealthWheelPickerColumn(
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    format: (Int) -> String = { it.toString().padStart(2, '0') }
) {
    val items = range.toList()
    val startIndex = items.indexOf(value).coerceAtLeast(0)
    
    // We add empty items at the top and bottom so the first/last items can be centered.
    // Assuming 3 visible items, 1 empty item at top, 1 empty item at bottom.
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    val selectedItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf -1
            
            val viewportCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
            
            var closestItem = visibleItemsInfo.first()
            var minDistance = Int.MAX_VALUE
            
            for (itemInfo in visibleItemsInfo) {
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val distance = kotlin.math.abs(itemCenter - viewportCenter)
                if (distance < minDistance) {
                    minDistance = distance
                    closestItem = itemInfo
                }
            }
            closestItem.index - 1 // subtract 1 for the empty top item
        }
    }
    
    LaunchedEffect(selectedItemIndex) {
        if (selectedItemIndex in items.indices && items[selectedItemIndex] != value) {
            onValueChange(items[selectedItemIndex])
        }
    }
    
    LaunchedEffect(value) {
        val targetIndex = items.indexOf(value)
        if (targetIndex != -1 && selectedItemIndex != targetIndex && !listState.isScrollInProgress) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        modifier = modifier.height(144.dp), // 3 items * 48dp
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(modifier = Modifier.height(48.dp))
        }
        items(items.size) { index ->
            val isSelected = index == selectedItemIndex
            val distance = kotlin.math.abs(index - selectedItemIndex)
            val alpha = when (distance) {
                0 -> 1f
                1 -> 0.4f
                else -> 0.1f
            }
            
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = format(items[index]),
                    style = HealthTypography.displayMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = HealthColors.Ink,
                    modifier = Modifier.alpha(alpha)
                )
            }
        }
        item {
            Box(modifier = Modifier.height(48.dp))
        }
    }
}
