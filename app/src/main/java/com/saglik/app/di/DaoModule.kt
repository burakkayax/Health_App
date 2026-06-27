package com.saglik.app.di

import com.saglik.core.database.AppDatabase
import com.saglik.core.database.dao.ExerciseDao
import com.saglik.core.database.dao.SleepDao
import com.saglik.core.database.dao.StepsDao
import com.saglik.core.database.dao.UserProfileDao
import com.saglik.core.database.dao.WeightDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    fun provideWeightDao(database: AppDatabase): WeightDao = database.weightDao()

    @Provides
    fun provideSleepDao(database: AppDatabase): SleepDao = database.sleepDao()

    @Provides
    fun provideStepsDao(database: AppDatabase): StepsDao = database.stepsDao()

    @Provides
    fun provideExerciseDao(database: AppDatabase): ExerciseDao = database.exerciseDao()
}
