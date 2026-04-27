package com.burak.healthapp.core.reminder

import com.burak.healthapp.domain.config.DefaultHealthGoals

object ReminderConstants {
    const val WATER_REMINDER_WORK_NAME = "water_reminder"
    const val MIN_INTERVAL_MINUTES = DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES
    const val QUICK_ADD_WATER_ML = 250
    const val ACTION_ADD_WATER_250 = "com.burak.healthapp.action.ADD_WATER_250"
    const val ACTION_SNOOZE_WATER_TODAY = "com.burak.healthapp.action.SNOOZE_WATER_TODAY"
    const val ADD_WATER_REQUEST_CODE = 2101
    const val SNOOZE_TODAY_REQUEST_CODE = 2102
}
