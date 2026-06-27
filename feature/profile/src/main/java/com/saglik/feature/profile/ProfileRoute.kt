package com.saglik.feature.profile

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue

object ProfileRoute {
    const val route = "profile"
}

fun NavGraphBuilder.profileScreen(
    onBackClick: () -> Unit = {},
) {
    composable(ProfileRoute.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        SettingsScreen(
            state = state,
            onBackClick = onBackClick,
        )
    }
}
