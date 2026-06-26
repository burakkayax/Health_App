package com.saglik.core.designsystem.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

object HealthMotion {
    fun <T> fastTween() = tween<T>(durationMillis = 200, easing = FastOutSlowInEasing)
    fun <T> standardTween() = tween<T>(durationMillis = 350, easing = FastOutSlowInEasing)
    fun <T> slowTween() = tween<T>(durationMillis = 500, easing = FastOutSlowInEasing)
    
    fun <T> cardPress() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    fun <T> chipSelection() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    fun <T> sheetEnter() = tween<T>(durationMillis = 350, easing = FastOutSlowInEasing)
    fun <T> sheetExit() = tween<T>(durationMillis = 250, easing = FastOutSlowInEasing)
}
