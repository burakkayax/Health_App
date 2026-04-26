package com.burak.healthapp.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.burak.healthapp.MainActivity
import com.burak.healthapp.R

object HealthNotifications {
    const val STEP_NOTIFICATION_ID = 1001
    private const val WATER_NOTIFICATION_ID = 1002
    private const val STEP_CHANNEL_ID = "step_tracking"
    private const val WATER_CHANNEL_ID = "water_reminders"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                STEP_CHANNEL_ID,
                "Adım sayar",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Adım sayımının arka planda çalışması için kullanılır."
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                WATER_CHANNEL_ID,
                "Su hatırlatıcıları",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "Su içme hedefini hatırlatır."
            },
        )
    }

    fun stepTrackingNotification(context: Context): Notification {
        ensureChannels(context)
        return NotificationCompat.Builder(context, STEP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Adım sayılıyor")
            .setContentText("Günlük adım hedefin otomatik takip ediliyor.")
            .setContentIntent(contentIntent(context))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    fun showWaterReminder(
        context: Context,
        currentMl: Int,
        targetMl: Int,
    ) {
        if (!canPostNotifications(context)) return
        ensureChannels(context)
        val notification = NotificationCompat.Builder(context, WATER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Su zamanı")
            .setContentText("Bugün $currentMl / $targetMl ml su kaydettin.")
            .setContentIntent(contentIntent(context))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(WATER_NOTIFICATION_ID, notification)
    }

    fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
