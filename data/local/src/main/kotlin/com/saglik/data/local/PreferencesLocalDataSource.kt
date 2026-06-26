package com.saglik.data.local

import com.saglik.core.datastore.AppPreferencesDataSource
import kotlinx.coroutines.flow.Flow

class PreferencesLocalDataSource(
    private val preferencesDataSource: AppPreferencesDataSource,
) {
    val onboardingCompleted: Flow<Boolean> = preferencesDataSource.onboardingCompleted

    suspend fun setOnboardingCompleted(completed: Boolean) {
        preferencesDataSource.setOnboardingCompleted(completed)
    }
}
