package com.burak.healthapp.core.performance

import android.os.SystemClock
import android.util.Log
import com.burak.healthapp.BuildConfig
import java.util.Locale

object PerformanceLogger {
    private const val TAG = "HealthPerf"

    fun mark(name: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "$name @ ${SystemClock.elapsedRealtime()}ms")
        }
    }

    fun <T> measure(
        name: String,
        block: () -> T,
    ): T {
        if (!BuildConfig.DEBUG) return block()

        val startedAt = SystemClock.elapsedRealtimeNanos()
        return try {
            block()
        } finally {
            val elapsedMs = (SystemClock.elapsedRealtimeNanos() - startedAt) / 1_000_000f
            Log.d(TAG, "$name took ${String.format(Locale.US, "%.2f", elapsedMs)}ms")
        }
    }
}
