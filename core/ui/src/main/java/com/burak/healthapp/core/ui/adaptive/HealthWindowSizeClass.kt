package com.burak.healthapp.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

enum class HealthWindowSizeClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
}

private const val MEDIUM_WIDTH_DP = 600
private const val EXPANDED_WIDTH_DP = 840

fun healthWindowSizeClassForWidth(widthDp: Int): HealthWindowSizeClass = when {
    widthDp < MEDIUM_WIDTH_DP -> HealthWindowSizeClass.COMPACT
    widthDp < EXPANDED_WIDTH_DP -> HealthWindowSizeClass.MEDIUM
    else -> HealthWindowSizeClass.EXPANDED
}

@Composable
fun rememberHealthWindowSizeClass(): HealthWindowSizeClass =
    healthWindowSizeClassForWidth(LocalConfiguration.current.screenWidthDp)

val HealthWindowSizeClass.isCompact: Boolean
    get() = this == HealthWindowSizeClass.COMPACT

val HealthWindowSizeClass.isMedium: Boolean
    get() = this == HealthWindowSizeClass.MEDIUM

val HealthWindowSizeClass.isExpanded: Boolean
    get() = this == HealthWindowSizeClass.EXPANDED

val HealthWindowSizeClass.shouldUseNavigationRail: Boolean
    get() = this != HealthWindowSizeClass.COMPACT

val HealthWindowSizeClass.dashboardColumnCount: Int
    get() = if (this == HealthWindowSizeClass.COMPACT) 1 else 2
