package com.saglik.core.ui.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.designsystem.theme.HealthTypography
import com.saglik.core.ui.component.GlassHealthCard

@Composable
fun HealthSummaryMetricCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    mainValue: String,
    secondaryText: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    showChevron: Boolean = true,
    isEmpty: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentSlot: (@Composable () -> Unit)? = null,
) {
    GlassHealthCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = HealthTypography.titleMedium,
                color = HealthColors.Ink,
                modifier = Modifier.weight(1f)
            )
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = HealthTypography.bodyMedium,
                    color = HealthColors.SecondaryText
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (showChevron) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "Details",
                    tint = HealthColors.TertiaryText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(HealthSpacing.cardVerticalSpacing))
        
        if (isEmpty) {
            Text(
                text = "No data yet",
                style = HealthTypography.bodyLarge,
                color = HealthColors.TertiaryText
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = mainValue,
                        style = HealthTypography.displayMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = HealthColors.Ink
                    )
                    Text(
                        text = secondaryText,
                        style = HealthTypography.bodyMedium,
                        color = HealthColors.SecondaryText
                    )
                }
                if (contentSlot != null) {
                    contentSlot()
                }
            }
        }
    }
}
