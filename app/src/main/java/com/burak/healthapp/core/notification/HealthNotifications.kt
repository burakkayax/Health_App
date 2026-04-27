package com.burak.healthapp.core.notification

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
    const val STEP_NOTIFICATION_ID = NotificationConstants.STEP_NOTIFICATION_ID

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationConstants.STEP_CHANNEL_ID,
                context.getString(R.string.notification_channel_steps_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notification_channel_steps_description)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                NotificationConstants.WATER_CHANNEL_ID,
                context.getString(R.string.notification_channel_water_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_water_description)
            },
        )
    }

    fun stepTrackingNotification(context: Context): Notification {
        ensureChannels(context)
        return NotificationCompat.Builder(context, NotificationConstants.STEP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_steps)
            .setContentTitle(context.getString(R.string.notification_steps_title))
            .setContentText(context.getString(R.string.notification_steps_text))
            .setContentIntent(contentIntent(context))
            .addAction(
                0,
                context.getString(R.string.notification_steps_stop),
                PendingIntent.getService(
                    context,
                    NotificationConstants.STEP_NOTIFICATION_ID,
                    com.burak.healthapp.core.step.StepCounterService.stopIntent(context),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
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
        val notification = NotificationCompat.Builder(context, NotificationConstants.WATER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_water)
            .setContentTitle(context.getString(R.string.notification_water_title))
            .setContentText(context.getString(R.string.notification_water_text, currentMl, targetMl))
            .setContentIntent(contentIntent(context))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        NotificationManagerCompat.from(context).notify(NotificationConstants.WATER_NOTIFICATION_ID, notification)
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
