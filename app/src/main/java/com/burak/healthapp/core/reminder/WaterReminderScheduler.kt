package com.burak.healthapp.core.reminder

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.burak.healthapp.domain.model.WaterReminderSettings
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class WaterReminderScheduler(
    context: Context,
) : WaterReminderSettingsApplier {
    private val appContext = context.applicationContext

    override fun apply(settings: WaterReminderSettings) {
        val workManager = WorkManager.getInstance(appContext)
        if (!settings.enabled) {
            workManager.cancelUniqueWork(ReminderConstants.WATER_REMINDER_WORK_NAME)
            return
        }

        val request = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            settings.intervalMinutes.coerceAtLeast(ReminderConstants.MIN_INTERVAL_MINUTES).toLong(),
            TimeUnit.MINUTES,
        ).setInitialDelay(
            calculateNextWaterReminderDelay(LocalDateTime.now(), settings).toMillis(),
            TimeUnit.MILLISECONDS,
        ).build()

        workManager.enqueueUniquePeriodicWork(
            ReminderConstants.WATER_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }
}
