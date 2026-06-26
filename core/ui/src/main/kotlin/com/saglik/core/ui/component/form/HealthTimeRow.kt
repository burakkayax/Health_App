package com.saglik.core.ui.component.form

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.designsystem.theme.HealthTypography

@Composable
fun HealthTimeRow(
    label: String,
    timeText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(HealthShapeTokens.pill)
            .background(HealthColors.GlassSurface.copy(alpha = 0.54f))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = HealthTypography.titleMedium,
            color = HealthColors.Ink
        )
        Text(
            text = timeText,
            style = HealthTypography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = HealthColors.SystemBlue
        )
    }
}
