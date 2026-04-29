package com.burak.healthapp.core.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.burak.healthapp.core.di.AppGraphEntryPoint
import com.burak.healthapp.core.notification.HealthNotifications
import com.burak.healthapp.domain.calculation.calculateHydrationTotal
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime

class WaterReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            AppGraphEntryPoint::class.java,
        )
        val settings = entryPoint.settingsRepository().settings.first()
        val reminder = settings.waterReminderSettings

        if (!reminder.enabled || !isInsideWaterReminderWindow(LocalTime.now(), reminder)) {
            return Result.success()
        }

        val today = LocalDate.now()
        val snapshot = entryPoint.dashboardRepository().observeToday(today).first()
        val currentMl = calculateHydrationTotal(snapshot.hydrationEntries)
        val targetMl = settings.goalSettings.waterTargetMl

        if (!shouldShowWaterReminder(today, settings.waterReminderSnoozedDate, currentMl, targetMl)) {
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
