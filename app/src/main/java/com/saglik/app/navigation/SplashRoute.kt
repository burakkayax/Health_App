package com.saglik.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.screen.GlassScaffold

object SplashRoute {
    const val route = "splash"
}

fun NavGraphBuilder.splashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSummary: () -> Unit,
) {
    composable(SplashRoute.route) {
        val viewModel: AppStartViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(state) {
            when (state) {
                AppStartUiState.GoToOnboarding -> onNavigateToOnboarding()
                AppStartUiState.GoToSummary -> onNavigateToSummary()
                AppStartUiState.Loading -> Unit
            }
        }

        SplashScreen()
    }
}

@Composable
private fun SplashScreen(
    modifier: Modifier = Modifier,
) {
    GlassScaffold(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Saglik",
                style = MaterialTheme.typography.displayLarge,
                color = HealthColors.Ink,
            )
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = HealthColors.SystemBlue,
                strokeWidth = 3.dp,
            )
        }
    }
}
