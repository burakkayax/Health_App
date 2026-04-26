package com.burak.healthapp

import android.app.Application
import com.burak.healthapp.core.di.AppContainer
import com.burak.healthapp.core.notification.HealthNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HealthApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
        HealthNotifications.ensureChannels(applicationContext)
        applicationScope.launch {
            container.waterReminderScheduler.apply(
                container.settingsRepository.settings.first().waterReminderSettings,
            )
        }
    }
}
