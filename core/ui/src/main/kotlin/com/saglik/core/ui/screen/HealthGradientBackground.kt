package com.saglik.core.ui.screen

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import com.saglik.core.designsystem.theme.HealthColors

@Immutable
data class HealthGradientPalette(
    val pastelPink: Color,
    val peach: Color,
    val lavender: Color,
    val lightBlue: Color,
    val screenBottom: Color,
)

val DefaultHealthGradientPalette = HealthGradientPalette(
    pastelPink = HealthColors.PastelPink,
    peach = HealthColors.Peach,
    lavender = HealthColors.Lavender,
    lightBlue = HealthColors.LightBlue,
    screenBottom = HealthColors.ScreenBottom,
)

@Composable
fun HealthGradientBackground(
    modifier: Modifier = Modifier,
    palette: HealthGradientPalette = rememberHealthGradientPalette(),
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        palette.pastelPink.copy(alpha = 0.74f),
                        palette.peach.copy(alpha = 0.58f),
                        palette.lavender.copy(alpha = 0.48f),
                        palette.lightBlue.copy(alpha = 0.42f),
                        palette.screenBottom,
                    ),
                    endY = 1900f,
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.lightBlue.copy(alpha = 0.38f),
                            palette.lightBlue.copy(alpha = 0f),
                        ),
                        center = Offset(980f, 40f),
                        radius = 980f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            palette.lavender.copy(alpha = 0.24f),
                            palette.lavender.copy(alpha = 0f),
                        ),
                        center = Offset(160f, 260f),
                        radius = 760f,
                    ),
                ),
        )
        content()
    }
}

@Composable
fun rememberHealthGradientPalette(): HealthGradientPalette {
    val context = LocalContext.current
    return remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val dynamicScheme = dynamicLightColorScheme(context)
            DefaultHealthGradientPalette.copy(
                pastelPink = DefaultHealthGradientPalette.pastelPink.softlyInfluencedBy(
                    dynamicScheme.tertiary,
                ),
                peach = DefaultHealthGradientPalette.peach.softlyInfluencedBy(
                    dynamicScheme.secondary,
                    fraction = 0.08f,
                ),
                lavender = DefaultHealthGradientPalette.lavender.softlyInfluencedBy(
                    dynamicScheme.primary,
                ),
                lightBlue = DefaultHealthGradientPalette.lightBlue.softlyInfluencedBy(
                    dynamicScheme.primary,
                    fraction = 0.12f,
                ),
            )
        } else {
            DefaultHealthGradientPalette
        }
    }
}

private fun Color.softlyInfluencedBy(
    dynamicColor: Color,
    fraction: Float = 0.1f,
): Color = lerp(this, dynamicColor, fraction.coerceIn(0f, 0.16f))
