package com.burak.healthapp.core.reminder

import com.burak.healthapp.domain.model.WaterReminderSettings

fun interface WaterReminderSettingsApplier {
    fun apply(settings: WaterReminderSettings)
}
