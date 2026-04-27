package com.burak.healthapp.core.step

class StepSensorWritePolicy(
    private val minIntervalMillis: Long = 60_000L,
    private val minStepDelta: Int = 50,
) {
    private var lastWrittenValue: Int? = null
    private var lastWriteMillis: Long? = null
    private var pendingValue: Int? = null

    fun shouldWrite(sensorValue: Int, nowMillis: Long): Boolean {
        val previousValue = lastWrittenValue
        val previousMillis = lastWriteMillis
        pendingValue = sensorValue
        return previousValue == null ||
            previousMillis == null ||
            nowMillis - previousMillis >= minIntervalMillis ||
            sensorValue - previousValue >= minStepDelta
    }

    fun markWritten(sensorValue: Int, nowMillis: Long) {
        lastWrittenValue = sensorValue
        lastWriteMillis = nowMillis
        pendingValue = null
    }

    fun pendingFlushValue(): Int? = pendingValue
}
