package com.saglik.feature.sleep.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import java.util.Locale

@Composable
fun SleepTimeInput(
    hour: Int,
    minute: Int,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val formattedTime = String.format(Locale.US, "%02d:%02d", hour, minute)
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = HealthColors.SecondaryText,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(56.dp)
                .clip(HealthShapeTokens.pill)
                .background(HealthColors.GlassSurface.copy(alpha = 0.54f))
                .border(
                    width = 1.dp,
                    color = if (isError) {
                        HealthColors.ActivityOrange.copy(alpha = 0.72f)
                    } else {
                        HealthColors.GlassBorder
                    },
                    shape = HealthShapeTokens.pill,
                )
                .clickable(
                    onClick = onClick,
                    onClickLabel = "Change $label",
                )
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = formattedTime,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = HealthColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}
