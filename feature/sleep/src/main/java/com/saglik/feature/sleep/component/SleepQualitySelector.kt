package com.saglik.feature.sleep.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.model.SleepQuality

@Composable
fun SleepQualitySelector(
    selectedQuality: SleepQuality?,
    onQualitySelected: (SleepQuality?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Quality",
            style = MaterialTheme.typography.labelMedium,
            color = HealthColors.SecondaryText,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SleepQuality.entries.forEach { quality ->
                val selected = selectedQuality == quality
                Text(
                    text = quality.label,
                    modifier = Modifier
                        .weight(1f)
                        .clip(HealthShapeTokens.pill)
                        .border(
                            width = 1.dp,
                            color = if (selected) {
                                HealthColors.SleepPurple.copy(alpha = 0.78f)
                            } else {
                                HealthColors.GlassBorder
                            },
                            shape = HealthShapeTokens.pill,
                        )
                        .background(
                            if (selected) {
                                HealthColors.SleepPurple.copy(alpha = 0.92f)
                            } else {
                                HealthColors.GlassSurface.copy(alpha = 0.48f)
                            },
                        )
                        .clickable { onQualitySelected(quality) }
                        .padding(vertical = 11.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) androidx.compose.ui.graphics.Color.White else HealthColors.SecondaryText,
                    maxLines = 1,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

private val SleepQuality.label: String
    get() = when (this) {
        SleepQuality.POOR -> "Poor"
        SleepQuality.OKAY -> "Okay"
        SleepQuality.GOOD -> "Good"
        SleepQuality.EXCELLENT -> "Excellent"
    }
