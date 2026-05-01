package com.burak.healthapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.burak.healthapp.core.performance.DebugRoutePerformanceTrace
import com.burak.healthapp.core.performance.PerformanceLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        PerformanceLogger.mark("MainActivity:onCreate:start")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DebugRoutePerformanceTrace("MainActivity:setContent")
            com.burak.healthapp.feature.app.HealthApp()
        }
    }
}
