package com.saglik.app.di

import android.content.Context
import com.saglik.core.healthconnect.HealthConnectDataSource
import com.saglik.core.healthconnect.RealHealthConnectDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthConnectModule {

    @Provides
    @Singleton
    fun provideHealthConnectDataSource(
        @ApplicationContext context: Context,
    ): HealthConnectDataSource = RealHealthConnectDataSource(context)
}
