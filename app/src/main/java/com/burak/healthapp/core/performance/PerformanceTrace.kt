package com.burak.healthapp.core.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

@Composable
fun DebugRoutePerformanceTrace(routeName: String) {
    val traceKey = remember(routeName) {
        PerformanceLogger.mark("$routeName:start")
        routeName
    }

    LaunchedEffect(traceKey) {
        PerformanceLogger.mark("$routeName:first_render")
    }
}
