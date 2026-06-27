package com.saglik.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthTheme

@Composable
fun HealthFloatingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .shadow(
                elevation = 10.dp,
                shape = CircleShape,
                ambientColor = HealthColors.Shadow,
                spotColor = HealthColors.Shadow,
            )
            .border(1.dp, HealthColors.GlassBorder, CircleShape)
            .clip(CircleShape)
            .background(HealthColors.GlassSurface),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = HealthColors.Ink,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Preview(showBackground = true, name = "Health Floating Back Button")
@Composable
fun HealthFloatingBackButtonPreview() {
    HealthTheme {
        Surface(
            color = HealthColors.ScreenBottom,
            modifier = Modifier.padding(16.dp),
        ) {
            HealthFloatingBackButton(onClick = {})
        }
    }
}
