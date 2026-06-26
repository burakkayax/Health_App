package com.saglik.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens

@Immutable
data class HealthBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun FloatingHealthBottomBar(
    items: List<HealthBottomNavItem>,
    selectedRoute: String,
    onItemSelected: (HealthBottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .fillMaxWidth()
            .height(74.dp)
            .shadow(
                elevation = 22.dp,
                shape = HealthShapeTokens.bottomBar,
                ambientColor = HealthColors.Shadow,
                spotColor = HealthColors.Shadow,
            )
            .border(1.dp, HealthColors.GlassBorder, HealthShapeTokens.bottomBar)
            .clip(HealthShapeTokens.bottomBar)
            .background(HealthColors.GlassSurface.copy(alpha = 0.88f))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = item.route == selectedRoute
            val color = if (selected) HealthColors.SystemBlue else HealthColors.Ink.copy(alpha = 0.58f)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(HealthShapeTokens.pill)
                    .clickable { onItemSelected(item) }
                    .padding(vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(22.dp),
                    tint = color,
                )
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
