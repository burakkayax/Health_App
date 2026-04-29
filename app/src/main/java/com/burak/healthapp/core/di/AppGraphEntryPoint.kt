package com.burak.healthapp.core.di

import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppGraphEntryPoint {
    fun dashboardRepository(): DashboardRepository
    fun settingsRepository(): SettingsRepository
}
