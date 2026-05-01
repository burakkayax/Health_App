package com.burak.healthapp.feature.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.feature.onboarding.OnboardingRoute
import com.burak.healthapp.feature.root.RootViewModel

@Composable
fun HealthApp() {
    val rootViewModel: RootViewModel = hiltViewModel()
    val rootState by rootViewModel.uiState.collectAsStateWithLifecycle()

    HealthTheme(themeMode = rootState.themeMode) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .semantics { testTagsAsResourceId = true },
            color = MaterialTheme.colorScheme.background,
        ) {
            when {
                !rootState.isLoaded -> {
                    RootLoadingPlaceholder()
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

@Composable
internal fun RootLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
}
