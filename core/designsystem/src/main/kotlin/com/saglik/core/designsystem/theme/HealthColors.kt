package com.saglik.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object HealthColors {
    val SystemBlue = Color(0xFF007AFF)
    val PastelPink = Color(0xFFFFB7A8)
    val Peach = Color(0xFFFFC4A3)
    val Lavender = Color(0xFFE5C7FF)
    val LightBlue = Color(0xFFC8E1FF)
    val ScreenBottom = Color(0xFFF8F8FA)

    val Ink = Color(0xFF111318)
    val SecondaryText = Color(0xFF727783)
    val TertiaryText = Color(0xFF9EA3AD)

    val GlassSurface = Color.White.copy(alpha = 0.78f)
    val GlassBorder = Color.White.copy(alpha = 0.62f)
    val Shadow = Color(0xFF182033).copy(alpha = 0.12f)

    val WeightBlue = Color(0xFF1E88FF)
    val BmiGreen = Color(0xFF34C759)
    val SleepPurple = Color(0xFF8E5CF7)
    val ActivityOrange = Color(0xFFFF6B45)
    val MoodTeal = Color(0xFF16B8A6)
    val InsightIndigo = Color(0xFF6E72F6)
}

internal val HealthLightColorScheme = lightColorScheme(
    primary = HealthColors.SystemBlue,
    onPrimary = Color.White,
    background = HealthColors.ScreenBottom,
    onBackground = HealthColors.Ink,
    surface = Color.White,
    onSurface = HealthColors.Ink,
    secondary = HealthColors.MoodTeal,
    tertiary = HealthColors.SleepPurple,
)

internal val HealthDarkColorScheme = darkColorScheme(
    primary = Color(0xFF7AB8FF),
    onPrimary = Color(0xFF001E36),
    background = Color(0xFF111318),
    onBackground = Color(0xFFF1F3F6),
    surface = Color(0xFF1B1D22),
    onSurface = Color(0xFFF1F3F6),
    secondary = Color(0xFF65DCD0),
    tertiary = Color(0xFFB59BFF),
)
