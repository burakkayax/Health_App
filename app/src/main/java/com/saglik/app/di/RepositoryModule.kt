package com.saglik.app.di

import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.UserProfileDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.data.local.PreferencesLocalDataSource
import com.saglik.data.repository.DefaultAppPreferencesRepository
import com.saglik.data.repository.DefaultSleepRepository
import com.saglik.data.repository.DefaultUserProfileRepository
import com.saglik.data.repository.DefaultWeightRepository
import com.saglik.domain.repository.AppPreferencesRepository
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WeightRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(
        localDataSource: PreferencesLocalDataSource,
    ): AppPreferencesRepository = DefaultAppPreferencesRepository(localDataSource)

    @Provides
    @Singleton
    fun provideUserProfileRepository(
        userProfileDao: UserProfileDao,
    ): UserProfileRepository = DefaultUserProfileRepository(userProfileDao)

    @Provides
    @Singleton
    fun provideWeightRepository(
        weightDao: WeightDao,
    ): WeightRepository = DefaultWeightRepository(weightDao)

    @Provides
    @Singleton
    fun provideSleepRepository(
        sleepDao: SleepDao,
    ): SleepRepository = DefaultSleepRepository(sleepDao)
}
