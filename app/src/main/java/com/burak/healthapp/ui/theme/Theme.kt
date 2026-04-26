package com.burak.healthapp.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.burak.healthapp.domain.model.ThemeMode

private val HealthLightColorScheme = lightColorScheme(
    primary = HealthPrimary,
    onPrimary = HealthLightSurface,
    secondary = HealthWater,
    background = HealthLightBackground,
    onBackground = HealthLightTextPrimary,
    surface = HealthLightSurface,
    onSurface = HealthLightTextPrimary,
    surfaceVariant = HealthLightSurfaceMuted,
    onSurfaceVariant = HealthLightTextSecondary,
    outline = HealthLightDivider,
)

private val HealthDarkColorScheme = darkColorScheme(
    primary = HealthPrimary,
    onPrimary = HealthDarkBackground,
    secondary = HealthWater,
    background = HealthDarkBackground,
    onBackground = HealthDarkTextPrimary,
    surface = HealthDarkSurface,
    onSurface = HealthDarkTextPrimary,
    surfaceVariant = HealthDarkSurfaceMuted,
    onSurfaceVariant = HealthDarkTextSecondary,
    outline = HealthDarkDivider,
)

private val HealthShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(32.dp),
)

@Composable
fun HealthTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = resolveDarkTheme(
        themeMode = themeMode,
        isSystemDark = isSystemInDarkTheme(),
    )
    val view = LocalView.current

    SideEffect {
        if (!view.isInEditMode) {
            view.context.findActivity()?.window?.let { window ->
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) HealthDarkColorScheme else HealthLightColorScheme,
        typography = HealthTypography,
        shapes = HealthShapes,
        content = content,
    )
}

fun resolveDarkTheme(themeMode: ThemeMode, isSystemDark: Boolean): Boolean {
    return when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemDark
    }
}

private tailrec fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
