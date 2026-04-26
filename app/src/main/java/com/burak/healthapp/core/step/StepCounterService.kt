package com.burak.healthapp.core.step

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.burak.healthapp.HealthApplication
import com.burak.healthapp.core.notification.HealthNotifications
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class StepCounterService : Service(), SensorEventListener {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sensorManager: SensorManager? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasActivityRecognitionPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForegroundCompat()

        val manager = getSystemService(SensorManager::class.java)
        val stepCounter = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounter == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        sensorManager = manager
        manager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorValue = event?.values?.firstOrNull()?.toInt() ?: return
        val app = application as? HealthApplication ?: return
        serviceScope.launch {
            app.container.dashboardRepository.recordStepSensorValue(sensorValue)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun startForegroundCompat() {
        val notification = HealthNotifications.stepTrackingNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                HealthNotifications.STEP_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH,
            )
        } else {
            startForeground(HealthNotifications.STEP_NOTIFICATION_ID, notification)
        }
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION,
            ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        fun start(context: Context) {
            if (!context.hasActivityRecognitionPermission()) return
            ContextCompat.startForegroundService(
                context,
                Intent(context, StepCounterService::class.java),
            )
        }
    }
}

private fun Context.hasActivityRecognitionPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
}
