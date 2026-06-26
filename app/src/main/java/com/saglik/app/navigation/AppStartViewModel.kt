package com.saglik.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saglik.domain.usecase.ObserveOnboardingCompletedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppStartViewModel @Inject constructor(
    observeOnboardingCompletedUseCase: ObserveOnboardingCompletedUseCase,
) : ViewModel() {
    val uiState: StateFlow<AppStartUiState> = observeOnboardingCompletedUseCase()
        .map { completed ->
            if (completed) {
                AppStartUiState.GoToSummary
            } else {
                AppStartUiState.GoToOnboarding
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppStartUiState.Loading,
        )
}

sealed interface AppStartUiState {
    data object Loading : AppStartUiState
    data object GoToOnboarding : AppStartUiState
    data object GoToSummary : AppStartUiState
}
