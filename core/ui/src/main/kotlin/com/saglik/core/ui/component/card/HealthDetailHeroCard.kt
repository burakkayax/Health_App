package com.saglik.core.ui.component.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.designsystem.theme.HealthTypography
import com.saglik.core.ui.component.GlassHealthCard

@Composable
fun HealthDetailHeroCard(
    title: String,
    mainValue: String,
    secondaryText: String,
    modifier: Modifier = Modifier,
    contentSlot: (@Composable ColumnScope.() -> Unit)? = null,
) {
    GlassHealthCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = HealthTypography.bodyMedium,
                color = HealthColors.SecondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mainValue,
                style = HealthTypography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthColors.Ink
            )
            Text(
                text = secondaryText,
                style = HealthTypography.bodyMedium,
                color = HealthColors.SecondaryText
            )
            
            if (contentSlot != null) {
                Spacer(modifier = Modifier.height(HealthSpacing.cardVerticalSpacing))
                contentSlot()
            }
        }
    }
}
