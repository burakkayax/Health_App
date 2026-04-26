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
internal fun StepCounterPermissionEffect() {
    val context = LocalContext.current
    val activityPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            StepCounterService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        when {
            !context.hasStepCounterSensor() -> Unit
            context.hasActivityRecognitionPermission() -> StepCounterService.start(context)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
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

internal fun Context.hasActivityRecognitionPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
}

internal fun Context.hasStepCounterSensor(): Boolean {
    val sensorManager = getSystemService(SensorManager::class.java)
    return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}
