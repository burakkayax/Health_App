package com.saglik.domain.usecase

import com.saglik.core.model.UserProfile
import com.saglik.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow

class ObserveUserProfileUseCase(
    private val repository: UserProfileRepository,
) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeProfile()
}
