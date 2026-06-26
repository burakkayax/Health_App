package com.saglik.feature.onboarding

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

object OnboardingRoute {
    const val route = "onboarding"
}

fun NavGraphBuilder.onboardingScreen(
    onOnboardingComplete: () -> Unit,
) {
    composable(OnboardingRoute.route) {
        val viewModel: OnboardingViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(viewModel) {
            viewModel.navigationEvents.collect {
                onOnboardingComplete()
            }
        }

        OnboardingScreen(
            state = state,
            onEvent = viewModel::onEvent,
        )
    }
}
