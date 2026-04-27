package com.burak.healthapp.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.burak.healthapp.HealthApplication
import com.burak.healthapp.core.notification.NotificationConstants
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WaterReminderActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as? HealthApplication

        if (app == null) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                when (intent.action) {
                    ReminderConstants.ACTION_ADD_WATER_250 -> {
                        app.container.dashboardRepository.addHydration(
                            amountMl = ReminderConstants.QUICK_ADD_WATER_ML,
                            date = LocalDate.now(),
                        )
                    }
                    ReminderConstants.ACTION_SNOOZE_WATER_TODAY -> {
                        app.container.settingsRepository.updateWaterReminderSnoozedDate(LocalDate.now())
                    }
                }
                NotificationManagerCompat.from(context)
                    .cancel(NotificationConstants.WATER_NOTIFICATION_ID)
            }
            pendingResult.finish()
        }
    }
}
