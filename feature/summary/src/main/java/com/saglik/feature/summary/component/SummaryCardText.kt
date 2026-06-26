package com.saglik.feature.summary.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.saglik.core.designsystem.theme.HealthColors

@Composable
internal fun SummaryValueText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HealthColors.Ink,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 34.sp),
        fontWeight = FontWeight.Bold,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
internal fun SummarySecondaryText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = HealthColors.SecondaryText,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
