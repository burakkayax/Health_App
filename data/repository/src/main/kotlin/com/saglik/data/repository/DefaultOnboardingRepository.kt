package com.saglik.data.repository

import com.saglik.data.local.PreferencesLocalDataSource
import com.saglik.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow

class DefaultAppPreferencesRepository(
    private val localDataSource: PreferencesLocalDataSource,
) : AppPreferencesRepository {
    override fun observeOnboardingCompleted(): Flow<Boolean> = localDataSource.onboardingCompleted

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        localDataSource.setOnboardingCompleted(completed)
    }
}
