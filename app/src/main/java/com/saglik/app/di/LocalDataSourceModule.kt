package com.saglik.app.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.saglik.core.datastore.AppPreferencesDataSource
import com.saglik.data.local.PreferencesLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalDataSourceModule {

    @Provides
    @Singleton
    fun provideAppPreferencesDataSource(
        dataStore: DataStore<Preferences>,
    ): AppPreferencesDataSource = AppPreferencesDataSource(dataStore)

    @Provides
    @Singleton
    fun providePreferencesLocalDataSource(
        preferencesDataSource: AppPreferencesDataSource,
    ): PreferencesLocalDataSource = PreferencesLocalDataSource(preferencesDataSource)
}
