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
import android.util.Log
import androidx.core.content.ContextCompat
import com.burak.healthapp.BuildConfig
import com.burak.healthapp.core.notification.HealthNotifications
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService :
    Service(),
    SensorEventListener {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sensorManager: SensorManager? = null
    private var listenerRegistered = false
    private val writePolicy = StepSensorWritePolicy()

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var dashboardRepository: DashboardRepository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            serviceScope.launch {
                runCatching {
                    settingsRepository.updateStepTrackingEnabled(false)
                }.onFailure { error ->
                    debugLog("Failed to persist step tracking stop action", error)
                }
                withContext(Dispatchers.Main.immediate) {
                    stopStepTracking(startId)
                }
            }
            return START_NOT_STICKY
        }

        if (!hasActivityRecognitionPermission()) {
            stopStepTracking(startId)
            return START_NOT_STICKY
        }

        val manager = getSystemService(SensorManager::class.java)
        val stepCounter = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (stepCounter == null) {
            stopStepTracking(startId)
            return START_NOT_STICKY
        }

        serviceScope.launch {
            val enabled = runCatching {
                settingsRepository.settings.first().stepTrackingEnabled
            }.onFailure { error ->
                debugLog("Failed to read step tracking preference", error)
            }.getOrDefault(false)

            val decision = decideStepServiceStart(
                isStopAction = false,
                stepTrackingEnabled = enabled,
                hasPermission = true,
                hasSensor = true,
            )
            withContext(Dispatchers.Main.immediate) {
                when (decision) {
                    StepServiceStartDecision.START -> {
                        startForegroundCompat()
                        registerStepListener(manager, stepCounter)
                    }

                    else -> stopStepTracking(startId)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        flushPendingSensorValue()
        unregisterStepListener()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorValue = event?.values?.firstOrNull()?.toInt() ?: return
        val nowMillis = System.currentTimeMillis()
        if (!writePolicy.shouldWrite(sensorValue, nowMillis)) return
        serviceScope.launch {
            runCatching {
                dashboardRepository.recordStepSensorValue(sensorValue)
                writePolicy.markWritten(sensorValue, nowMillis)
            }.onFailure { error ->
                debugLog("Failed to record step sensor value", error)
            }
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

    private fun registerStepListener(
        manager: SensorManager,
        stepCounter: Sensor,
    ) {
        if (listenerRegistered) return
        sensorManager = manager
        listenerRegistered = manager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
        if (!listenerRegistered) {
            debugLog("Step sensor listener registration was rejected")
            stopStepTracking()
        }
    }

    private fun unregisterStepListener() {
        if (listenerRegistered) {
            sensorManager?.unregisterListener(this)
        }
        listenerRegistered = false
        sensorManager = null
    }

    private fun stopStepTracking(startId: Int? = null) {
        unregisterStepListener()
        if (startId != null) {
            stopSelf(startId)
        } else {
            stopSelf()
        }
    }

    private fun hasActivityRecognitionPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        fun start(context: Context) {
            if (!context.hasActivityRecognitionPermission()) return
            ContextCompat.startForegroundService(
                context,
                Intent(context, StepCounterService::class.java),
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, StepCounterService::class.java))
        }

        fun stopIntent(context: Context): Intent = Intent(context, StepCounterService::class.java).setAction(ACTION_STOP)

        private const val ACTION_STOP = "com.burak.healthapp.action.STOP_STEP_TRACKING"
        private const val PENDING_FLUSH_TIMEOUT_MILLIS = 500L
        private const val TAG = "StepCounterService"
    }

    private fun flushPendingSensorValue() {
        if (writePolicy.pendingFlushValue() == null) return
        val result = runBlocking(Dispatchers.IO) {
            withTimeoutOrNull(PENDING_FLUSH_TIMEOUT_MILLIS) {
                flushPendingStepSensorValue(
                    writePolicy = writePolicy,
                    nowMillis = System.currentTimeMillis(),
                    recordStepSensorValue = dashboardRepository::recordStepSensorValue,
                )
            }
        }
        when (result) {
            StepSensorFlushResult.FAILED -> debugLog("Failed to flush pending step sensor value")
            null -> debugLog("Timed out while flushing pending step sensor value")
            else -> Unit
        }
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
}

private fun Context.hasActivityRecognitionPermission(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACTIVITY_RECOGNITION,
    ) == PackageManager.PERMISSION_GRANTED
