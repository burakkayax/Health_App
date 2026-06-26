package com.saglik.feature.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.saglik.core.ui.FoundationPlaceholderScreen

object ProfileRoute {
    const val route = "profile"
}

fun NavGraphBuilder.profileScreen() {
    composable(ProfileRoute.route) {
        FoundationPlaceholderScreen(text = "Profile Placeholder")
    }
}
