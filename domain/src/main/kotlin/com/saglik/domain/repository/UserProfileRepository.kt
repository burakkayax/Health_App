package com.saglik.domain.repository

import com.saglik.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile?>

    suspend fun saveProfile(profile: UserProfile)
}
