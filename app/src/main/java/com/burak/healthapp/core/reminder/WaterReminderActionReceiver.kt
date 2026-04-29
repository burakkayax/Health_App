package com.burak.healthapp.core.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.burak.healthapp.core.notification.NotificationConstants
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class WaterReminderActionReceiver : BroadcastReceiver() {
    @Inject
    lateinit var dashboardRepository: DashboardRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                when (intent.action) {
                    ReminderConstants.ACTION_ADD_WATER_250 -> {
                        dashboardRepository.addHydration(
                            amountMl = ReminderConstants.QUICK_ADD_WATER_ML,
                            date = LocalDate.now(),
                        )
                    }
                    ReminderConstants.ACTION_SNOOZE_WATER_TODAY -> {
                        settingsRepository.updateWaterReminderSnoozedDate(LocalDate.now())
                    }
                }
                NotificationManagerCompat.from(context)
                    .cancel(NotificationConstants.WATER_NOTIFICATION_ID)
            }
            pendingResult.finish()
        }
    }
}
