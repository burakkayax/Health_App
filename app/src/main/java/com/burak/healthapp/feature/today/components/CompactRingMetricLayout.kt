package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.burak.healthapp.core.ui.components.CircularProgressRing
import com.burak.healthapp.core.ui.theme.HealthSpacing
@Composable
internal fun CompactRingMetricLayout(
    progress: Float,
    color: Color,
    headline: String,
    supportingLabel: String,
    helperLabel: String,
    modifier: Modifier = Modifier,
    trackColor: Color = color.copy(alpha = 0.14f),
    bottomContent: (@Composable () -> Unit)? = null,
    ringContent: @Composable () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HealthSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressRing(
            progress = progress,
            color = color,
            modifier = Modifier.size(104.dp),
            strokeWidth = 10.dp,
            trackColor = trackColor,
        ) {
            ringContent()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = supportingLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (helperLabel.isNotBlank()) {
                Text(
                    text = helperLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            bottomContent?.invoke()
        }
    }
}
