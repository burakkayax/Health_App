package com.burak.healthapp

import android.app.Application
import com.burak.healthapp.core.notification.HealthNotifications
import com.burak.healthapp.core.reminder.WaterReminderScheduler
import com.burak.healthapp.domain.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class HealthApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var waterReminderScheduler: WaterReminderScheduler

    override fun onCreate() {
        super.onCreate()
        HealthNotifications.ensureChannels(applicationContext)
        applicationScope.launch {
            waterReminderScheduler.apply(settingsRepository.settings.first().waterReminderSettings)
        }
    }
}
