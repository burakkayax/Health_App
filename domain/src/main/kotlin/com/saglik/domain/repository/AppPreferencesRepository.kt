package com.saglik.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    fun observeOnboardingCompleted(): Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)
}
