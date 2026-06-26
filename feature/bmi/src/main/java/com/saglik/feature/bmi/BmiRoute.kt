package com.saglik.feature.bmi

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.saglik.core.ui.FoundationPlaceholderScreen

object BmiRoute {
    const val route = "bmi"
}

fun NavGraphBuilder.bmiScreen() {
    composable(BmiRoute.route) {
        FoundationPlaceholderScreen(text = "BMI Placeholder")
    }
}
