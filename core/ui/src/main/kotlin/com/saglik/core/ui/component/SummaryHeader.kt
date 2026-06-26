package com.saglik.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors

@Composable
fun SummaryHeader(
    modifier: Modifier = Modifier,
    title: String = "Summary",
    onProfileClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = HealthColors.Ink,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = HealthColors.Shadow,
                    spotColor = HealthColors.Shadow,
                )
                .border(1.dp, HealthColors.GlassBorder, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onProfileClick)
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
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
