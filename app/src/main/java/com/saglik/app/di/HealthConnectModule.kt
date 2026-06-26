package com.saglik.app.di

import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.healthconnect.NoOpHealthConnectDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthConnectModule {

    @Provides
    @Singleton
    fun provideHealthConnectDataSource(): HealthConnectDataSource = NoOpHealthConnectDataSource()
}
