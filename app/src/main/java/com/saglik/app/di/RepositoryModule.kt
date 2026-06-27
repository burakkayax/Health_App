package com.saglik.app.di

import com.saglik.core.database.dao.ExerciseDao
import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.StepsDao
import com.saglik.core.database.dao.UserProfileDao
import com.saglik.core.database.dao.WaterDao
import com.saglik.core.database.dao.WeightDao
import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.data.local.PreferencesLocalDataSource
import com.saglik.data.repository.DefaultAppPreferencesRepository
import com.saglik.data.repository.DefaultExerciseRepository
import com.saglik.data.repository.DefaultHealthConnectRepository
import com.saglik.data.repository.DefaultHealthConnectSyncRepository
import com.saglik.data.repository.DefaultSleepRepository
import com.saglik.data.repository.DefaultStepsRepository
import com.saglik.data.repository.DefaultUserProfileRepository
import com.saglik.data.repository.DefaultWaterRepository
import com.saglik.data.repository.DefaultWeightRepository
import com.saglik.domain.repository.AppPreferencesRepository
import com.saglik.domain.repository.ExerciseRepository
import com.saglik.domain.repository.HealthConnectRepository
import com.saglik.domain.repository.HealthConnectSyncRepository
import com.saglik.domain.repository.SleepRepository
import com.saglik.domain.repository.StepsRepository
import com.saglik.domain.repository.UserProfileRepository
import com.saglik.domain.repository.WaterRepository
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

    @Provides
    @Singleton
    fun provideStepsRepository(
        stepsDao: StepsDao,
    ): StepsRepository = DefaultStepsRepository(stepsDao)

    @Provides
    @Singleton
    fun provideExerciseRepository(
        exerciseDao: ExerciseDao,
    ): ExerciseRepository = DefaultExerciseRepository(exerciseDao)

    @Provides
    @Singleton
    fun provideWaterRepository(
        waterDao: WaterDao,
    ): WaterRepository = DefaultWaterRepository(waterDao)

    @Provides
    @Singleton
    fun provideHealthConnectRepository(
        dataSource: HealthConnectDataSource,
    ): HealthConnectRepository = DefaultHealthConnectRepository(dataSource)

    @Provides
    @Singleton
    fun provideHealthConnectSyncRepository(
        dataSource: HealthConnectDataSource,
        weightDao: WeightDao,
        sleepDao: SleepDao,
        stepsDao: StepsDao,
        exerciseDao: ExerciseDao,
    ): HealthConnectSyncRepository = DefaultHealthConnectSyncRepository(
        dataSource = dataSource,
        weightDao = weightDao,
        sleepDao = sleepDao,
        stepsDao = stepsDao,
        exerciseDao = exerciseDao,
    )
}
