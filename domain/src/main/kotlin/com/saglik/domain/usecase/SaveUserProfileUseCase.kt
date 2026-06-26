package com.saglik.domain.usecase

import com.saglik.core.model.UserProfile
import com.saglik.domain.repository.UserProfileRepository

class SaveUserProfileUseCase(
    private val repository: UserProfileRepository,
) {
    suspend operator fun invoke(profile: UserProfile) {
        repository.saveProfile(profile)
    }
}
