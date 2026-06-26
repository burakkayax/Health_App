package com.saglik.data.repository

import com.saglik.core.database.dao.UserProfileDao
import com.saglik.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.map

class DefaultUserProfileRepository(
    private val userProfileDao: UserProfileDao,
) : UserProfileRepository {
    override fun observeProfile() = userProfileDao.observeProfile().map { it?.toDomain() }

    override suspend fun saveProfile(profile: com.saglik.core.model.UserProfile) {
        userProfileDao.upsertProfile(profile.toEntity())
    }
}
