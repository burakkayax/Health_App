package com.burak.healthapp.core.step

internal enum class StepSensorFlushResult {
    NO_PENDING_VALUE,
    WRITTEN,
    FAILED,
}

internal suspend fun flushPendingStepSensorValue(
    writePolicy: StepSensorWritePolicy,
    nowMillis: Long,
    recordStepSensorValue: suspend (Int) -> Unit,
): StepSensorFlushResult {
    val sensorValue = writePolicy.pendingFlushValue() ?: return StepSensorFlushResult.NO_PENDING_VALUE
    return runCatching {
        recordStepSensorValue(sensorValue)
        writePolicy.markWritten(sensorValue, nowMillis)
    }.fold(
        onSuccess = { StepSensorFlushResult.WRITTEN },
        onFailure = { StepSensorFlushResult.FAILED },
    )
}
