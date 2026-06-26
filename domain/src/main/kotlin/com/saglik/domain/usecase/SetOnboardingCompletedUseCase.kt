package com.saglik.domain.usecase

import com.saglik.domain.repository.AppPreferencesRepository

class SetOnboardingCompletedUseCase(
    private val repository: AppPreferencesRepository,
) {
    suspend operator fun invoke(completed: Boolean) {
        repository.setOnboardingCompleted(completed)
    }
}
