package com.burak.healthapp.feature.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.burak.healthapp.core.notification.HealthNotifications
import com.burak.healthapp.core.step.StepCounterService

@Composable
internal fun StepCounterPermissionEffect(stepTrackingEnabled: Boolean) {
    val context = LocalContext.current

    LaunchedEffect(stepTrackingEnabled) {
        when {
            !stepTrackingEnabled -> StepCounterService.stop(context)
            !context.hasStepCounterSensor() -> StepCounterService.stop(context)
            context.hasActivityRecognitionPermission() -> StepCounterService.start(context)
            else -> Unit
        }
    }
}

@Composable
internal fun WaterReminderNotificationPermissionEffect(waterReminderEnabled: Boolean) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(waterReminderEnabled) {
        if (
            waterReminderEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !HealthNotifications.canPostNotifications(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

internal fun Context.hasActivityRecognitionPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACTIVITY_RECOGNITION,
    ) == PackageManager.PERMISSION_GRANTED

internal fun Context.hasStepCounterSensor(): Boolean {
    val sensorManager = getSystemService(SensorManager::class.java)
    return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}
