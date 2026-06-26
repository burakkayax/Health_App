package com.saglik.core.ui.component.card

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.designsystem.theme.HealthTypography
import com.saglik.core.ui.component.GlassHealthCard

@Composable
fun HealthHistoryCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassHealthCard(modifier = modifier) {
        Text(
            text = title,
            style = HealthTypography.titleMedium,
            color = HealthColors.Ink
        )
        Spacer(modifier = Modifier.height(HealthSpacing.cardVerticalSpacing))
        content()
    }
}
