package com.burak.healthapp.core.reminder

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.burak.healthapp.BuildConfig
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
    override suspend fun doWork(): Result = runCatching {
        doReminderWork()
    }.getOrElse { error ->
        debugLog("Water reminder worker failed", error)
        Result.success()
    }

    private suspend fun doReminderWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            AppGraphEntryPoint::class.java,
        )
        val settings = entryPoint.settingsRepository().settings.first()
        val reminder = settings.waterReminderSettings

        if (
            !shouldReadHydrationSnapshotForReminder(
                settings = reminder,
                now = LocalTime.now(),
                canPostNotifications = HealthNotifications.canPostNotifications(applicationContext),
            )
        ) {
            return Result.success()
        }

        val today = LocalDate.now()
        val snapshot = entryPoint.dashboardRepository().observeToday(today).first()
        val currentMl = calculateHydrationTotal(snapshot.hydrationEntries)
        val targetMl = settings.goalSettings.waterTargetMl

        if (!shouldShowWaterReminder(today, settings.waterReminderSnoozedDate, currentMl, targetMl)) {
            return Result.success()
        }

        runCatching {
            HealthNotifications.showWaterReminder(
                context = applicationContext,
                currentMl = currentMl,
                targetMl = targetMl,
            )
        }.onFailure { error ->
            debugLog("Failed to show water reminder notification", error)
        }
        return Result.success()
    }

    private fun debugLog(
        message: String,
        error: Throwable? = null,
    ) {
        if (!BuildConfig.DEBUG) return
        if (error == null) {
            Log.d(TAG, message)
        } else {
            Log.d(TAG, message, error)
        }
    }

    private companion object {
        private const val TAG = "WaterReminderWorker"
    }
}
