package com.saglik.feature.addentry

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.saglik.core.ui.FoundationPlaceholderScreen

object AddEntryRoute {
    const val route = "add_entry"
}

fun NavGraphBuilder.addEntryScreen() {
    composable(AddEntryRoute.route) {
        FoundationPlaceholderScreen(text = "Add Entry Placeholder")
    }
}
