package com.saglik.core.ui.component.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthTypography

@Composable
fun HealthPickerActionRow(
    title: String,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onCancel) {
            Text(
                text = "Cancel",
                style = HealthTypography.bodyLarge,
                color = HealthColors.SecondaryText
            )
        }
        Text(
            text = title,
            style = HealthTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = HealthColors.Ink
        )
        TextButton(onClick = onDone) {
            Text(
                text = "Done",
                style = HealthTypography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = HealthColors.SystemBlue
            )
        }
    }
}
