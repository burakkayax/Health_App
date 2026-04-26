package com.burak.healthapp.reminders

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.burak.healthapp.domain.model.WaterReminderSettings
import java.util.concurrent.TimeUnit

class WaterReminderScheduler(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun apply(settings: WaterReminderSettings) {
        val workManager = WorkManager.getInstance(appContext)
        if (!settings.enabled) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }

        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            settings.intervalMinutes.coerceAtLeast(MIN_INTERVAL_MINUTES).toLong(),
            TimeUnit.MINUTES,
        ).build()

        workManager.enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "water_reminder"
        private const val MIN_INTERVAL_MINUTES = 15
    }
}
