package com.burak.healthapp.core.di

import android.content.Context
import com.burak.healthapp.core.reminder.WaterReminderScheduler
import com.burak.healthapp.core.reminder.WaterReminderSettingsApplier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReminderModule {
    @Provides
    @Singleton
    fun provideWaterReminderScheduler(
        @ApplicationContext context: Context,
    ): WaterReminderScheduler = WaterReminderScheduler(context)

    @Provides
    fun provideWaterReminderSettingsApplier(
        scheduler: WaterReminderScheduler,
    ): WaterReminderSettingsApplier = scheduler
}
