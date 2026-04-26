package com.burak.healthapp.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.burak.healthapp.HealthApplication
import com.burak.healthapp.domain.calculation.calculateHydrationTotal
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.notifications.HealthNotifications
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first

class WaterReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? HealthApplication ?: return Result.success()
        val settings = app.container.settingsRepository.settings.first()
        val reminder = settings.waterReminderSettings

        if (!reminder.enabled || !LocalTime.now().isInside(reminder)) {
            return Result.success()
        }

        val snapshot = app.container.dashboardRepository.observeToday(LocalDate.now()).first()
        val currentMl = calculateHydrationTotal(snapshot.hydrationEntries)
        val targetMl = settings.goalSettings.waterTargetMl

        if (currentMl >= targetMl) {
            return Result.success()
        }

        HealthNotifications.showWaterReminder(
            context = applicationContext,
            currentMl = currentMl,
            targetMl = targetMl,
        )
        return Result.success()
    }
}

private fun LocalTime.isInside(settings: WaterReminderSettings): Boolean {
    return if (settings.startTime <= settings.endTime) {
        this >= settings.startTime && this <= settings.endTime
    } else {
        this >= settings.startTime || this <= settings.endTime
    }
}
