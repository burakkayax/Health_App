package com.saglik.core.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors

@Composable
fun OrganicMoodIcon(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(88.dp)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    HealthColors.MoodTeal.copy(alpha = 0.38f),
                    HealthColors.LightBlue.copy(alpha = 0.12f),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.56f, size.height * 0.46f),
                radius = size.minDimension * 0.62f,
            ),
            radius = size.minDimension * 0.54f,
            center = Offset(size.width * 0.53f, size.height * 0.52f),
        )
        val blob = Path().apply {
            moveTo(size.width * 0.20f, size.height * 0.58f)
            cubicTo(
                size.width * 0.14f,
                size.height * 0.24f,
                size.width * 0.46f,
                size.height * 0.06f,
                size.width * 0.70f,
                size.height * 0.22f,
            )
            cubicTo(
                size.width * 0.96f,
                size.height * 0.39f,
                size.width * 0.86f,
                size.height * 0.78f,
                size.width * 0.56f,
                size.height * 0.84f,
            )
            cubicTo(
                size.width * 0.33f,
                size.height * 0.90f,
                size.width * 0.24f,
                size.height * 0.76f,
                size.width * 0.20f,
                size.height * 0.58f,
            )
            close()
        }
        drawPath(
            path = blob,
            brush = Brush.linearGradient(
                colors = listOf(
                    HealthColors.MoodTeal.copy(alpha = 0.88f),
                    HealthColors.LightBlue.copy(alpha = 0.84f),
                    HealthColors.Lavender.copy(alpha = 0.72f),
                ),
            ),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.72f),
            radius = 8.dp.toPx(),
            center = Offset(size.width * 0.40f, size.height * 0.35f),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.38f),
            radius = 5.dp.toPx(),
            center = Offset(size.width * 0.65f, size.height * 0.62f),
        )
    }
}
