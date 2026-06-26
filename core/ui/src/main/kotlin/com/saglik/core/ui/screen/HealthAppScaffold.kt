package com.saglik.core.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.ui.component.FloatingHealthBottomBar
import com.saglik.core.ui.component.HealthBottomNavItem

@Composable
fun HealthAppScaffold(
    title: String,
    selectedRoute: String,
    bottomItems: List<HealthBottomNavItem>,
    onBottomTabSelected: (String) -> Unit,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit = {},
    content: @Composable BoxScope.(PaddingValues) -> Unit,
) {
    val collapseProgress by remember(listState) {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> 1f
                else -> (listState.firstVisibleItemScrollOffset / 96f).coerceIn(0f, 1f)
            }
        }
    }

    val contentPadding = HealthAppScaffoldDefaults.contentPadding()

    GlassScaffold(
        modifier = modifier,
        bottomBar = {
            FloatingHealthBottomBar(
                items = bottomItems,
                selectedRoute = selectedRoute,
                onItemSelected = { onBottomTabSelected(it.route) },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        },
    ) {
        content(contentPadding)
        CollapsingHealthHeader(
            title = title,
            collapseProgress = collapseProgress,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

object HealthAppScaffoldDefaults {
    private val ExpandedHeaderBodyHeight = 100.dp
    private val ExpandedHeaderContentGap = 16.dp
    val BottomContentPadding = HealthSpacing.bottomControlsHeight + 48.dp

    @Composable
    fun contentPadding(
        horizontal: Dp = HealthSpacing.screenHorizontal,
    ): PaddingValues {
        val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        return PaddingValues(
            start = horizontal,
            top = statusBarTop + ExpandedHeaderBodyHeight + ExpandedHeaderContentGap,
            end = horizontal,
            bottom = BottomContentPadding,
        )
    }
}

@Composable
fun CollapsingHealthHeader(
    title: String,
    collapseProgress: Float,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FrostedTopBar(
        collapseProgress = collapseProgress,
        modifier = modifier,
    ) {
        val progress = collapseProgress.coerceIn(0f, 1f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(lerpDp(72.dp, 56.dp, progress))
                .padding(horizontal = HealthSpacing.screenHorizontal),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = lerpTextUnit(42.sp, 22.sp, progress),
                    lineHeight = lerpTextUnit(48.sp, 28.sp, progress),
                ),
                fontWeight = FontWeight.Bold,
                color = HealthColors.Ink,
            )
            Spacer(modifier = Modifier.weight(1f))
            HealthAvatarButton(
                size = lerpDp(44.dp, 34.dp, progress),
                iconSize = lerpDp(24.dp, 19.dp, progress),
                onClick = onProfileClick,
            )
        }
    }
}

@Composable
fun FrostedTopBar(
    collapseProgress: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val progress = collapseProgress.coerceIn(0f, 1f)
    val backgroundAlpha = 0.82f * progress
    val borderColor = HealthColors.GlassBorder.copy(alpha = 0.58f * progress)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HealthColors.GlassSurface.copy(alpha = backgroundAlpha))
            .drawBehind {
                if (progress > 0.01f) {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
            }
            .statusBarsPadding()
            .padding(top = lerpDp(18.dp, 4.dp, progress), bottom = lerpDp(10.dp, 4.dp, progress)),
    ) {
        content()
    }
}

@Composable
private fun HealthAvatarButton(
    size: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = HealthColors.Shadow,
                spotColor = HealthColors.Shadow,
            )
            .border(1.dp, HealthColors.GlassBorder, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        HealthColors.LightBlue.copy(alpha = 0.92f),
                        HealthColors.Lavender.copy(alpha = 0.9f),
                        HealthColors.Peach.copy(alpha = 0.72f),
                    ),
                ),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = "Profile",
            tint = HealthColors.Ink.copy(alpha = 0.72f),
            modifier = Modifier.size(iconSize),
        )
    }
}

private fun lerpDp(start: Dp, stop: Dp, fraction: Float): Dp =
    start + (stop - start) * fraction.coerceIn(0f, 1f)

private fun lerpTextUnit(start: TextUnit, stop: TextUnit, fraction: Float): TextUnit =
    (start.value + (stop.value - start.value) * fraction.coerceIn(0f, 1f)).sp
