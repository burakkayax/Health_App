package com.burak.healthapp.feature.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.feature.onboarding.OnboardingRoute
import com.burak.healthapp.feature.root.RootViewModel

@Composable
fun HealthApp() {
    val rootViewModel: RootViewModel = viewModel(factory = RootViewModel.Factory)
    val rootState by rootViewModel.uiState.collectAsStateWithLifecycle()

    HealthTheme(themeMode = rootState.themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when {
                !rootState.isLoaded -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = stringResource(R.string.common_loading))
                    }
                }

                !rootState.onboardingCompleted -> {
                    OnboardingRoute()
                }

                else -> {
                    MainShell(rootState = rootState)
                }
            }
        }
    }
}
