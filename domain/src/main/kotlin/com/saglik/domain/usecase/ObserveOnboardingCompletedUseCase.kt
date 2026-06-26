package com.saglik.domain.usecase

import com.saglik.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow

class ObserveOnboardingCompletedUseCase(
    private val repository: AppPreferencesRepository,
) {
    operator fun invoke(): Flow<Boolean> = repository.observeOnboardingCompleted()
}
