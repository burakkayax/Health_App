package com.saglik.app

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.saglik.app.navigation.HealthNavHost
import com.saglik.core.designsystem.theme.HealthTheme

@Composable
fun HealthApp() {
    HealthTheme {
        HealthNavHost(navController = rememberNavController())
    }
}
