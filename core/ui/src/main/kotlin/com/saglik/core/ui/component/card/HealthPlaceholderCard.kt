package com.saglik.core.ui.component.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthTypography
import com.saglik.core.ui.component.GlassHealthCard

@Composable
fun HealthPlaceholderCard(
    title: String,
    modifier: Modifier = Modifier,
) {
    GlassHealthCard(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = HealthTypography.titleMedium,
                color = HealthColors.Ink
            )
            Text(
                text = "Coming soon...",
                style = HealthTypography.bodyMedium,
                color = HealthColors.TertiaryText
            )
        }
    }
}
