package com.saglik.core.ui.component.form

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.designsystem.theme.HealthTypography

@Composable
fun HealthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = HealthColors.SystemBlue,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = HealthShapeTokens.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.34f),
            disabledContentColor = Color.White.copy(alpha = 0.82f),
        ),
        contentPadding = PaddingValues(horizontal = 22.dp),
    ) {
        Text(
            text = text,
            style = HealthTypography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun HealthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = HealthShapeTokens.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = HealthColors.GlassSurface,
            contentColor = HealthColors.SystemBlue,
            disabledContainerColor = HealthColors.GlassSurface.copy(alpha = 0.5f),
            disabledContentColor = HealthColors.SystemBlue.copy(alpha = 0.5f),
        ),
        contentPadding = PaddingValues(horizontal = 22.dp),
    ) {
        Text(
            text = text,
            style = HealthTypography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
