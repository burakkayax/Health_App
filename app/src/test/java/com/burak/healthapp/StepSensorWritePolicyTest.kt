package com.burak.healthapp

import com.burak.healthapp.core.step.StepSensorFlushResult
import com.burak.healthapp.core.step.StepSensorWritePolicy
import com.burak.healthapp.core.step.flushPendingStepSensorValue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepSensorWritePolicyTest {
    @Test
    fun firstSensorValue_shouldWrite() {
        val policy = StepSensorWritePolicy()

        assertTrue(policy.shouldWrite(sensorValue = 100, nowMillis = 0))
    }

    @Test
    fun smallDeltaBeforeInterval_isThrottledAndPendingForFlush() {
        val policy = StepSensorWritePolicy()

        assertTrue(policy.shouldWrite(sensorValue = 100, nowMillis = 0))
        policy.markWritten(sensorValue = 100, nowMillis = 0)

        assertFalse(policy.shouldWrite(sensorValue = 120, nowMillis = 10_000))
        assertEquals(120, policy.pendingFlushValue())
    }

    @Test
    fun largeDeltaBeforeInterval_shouldWrite() {
        val policy = StepSensorWritePolicy()

        assertTrue(policy.shouldWrite(sensorValue = 100, nowMillis = 0))
        policy.markWritten(sensorValue = 100, nowMillis = 0)

        assertTrue(policy.shouldWrite(sensorValue = 150, nowMillis = 10_000))
    }

    @Test
    fun intervalElapsed_shouldWrite() {
        val policy = StepSensorWritePolicy()

        assertTrue(policy.shouldWrite(sensorValue = 100, nowMillis = 0))
        policy.markWritten(sensorValue = 100, nowMillis = 0)

        assertTrue(policy.shouldWrite(sensorValue = 101, nowMillis = 60_000))
    }

    @Test
    fun sensorResetBeforeInterval_isThrottledUntilFlushOrInterval() {
        val policy = StepSensorWritePolicy()

        assertTrue(policy.shouldWrite(sensorValue = 1_000, nowMillis = 0))
        policy.markWritten(sensorValue = 1_000, nowMillis = 0)

        assertFalse(policy.shouldWrite(sensorValue = 10, nowMillis = 10_000))
        assertEquals(10, policy.pendingFlushValue())
        assertTrue(policy.shouldWrite(sensorValue = 10, nowMillis = 60_000))
    }

    @Test
    fun flushPendingValue_noPendingValueIsNoOp() = runTest {
        val policy = StepSensorWritePolicy()

        val result = flushPendingStepSensorValue(
            writePolicy = policy,
            nowMillis = 10_000,
            recordStepSensorValue = { error("Should not record") },
        )

        assertEquals(StepSensorFlushResult.NO_PENDING_VALUE, result)
    }

    @Test
    fun flushPendingValue_recordsPendingValueAndMarksWritten() = runTest {
        val policy = StepSensorWritePolicy()
        val recorded = mutableListOf<Int>()
        assertTrue(policy.shouldWrite(sensorValue = 100, nowMillis = 0))
        policy.markWritten(sensorValue = 100, nowMillis = 0)
        assertFalse(policy.shouldWrite(sensorValue = 120, nowMillis = 10_000))

        val result = flushPendingStepSensorValue(
            writePolicy = policy,
            nowMillis = 10_000,
            recordStepSensorValue = { sensorValue -> recorded += sensorValue },
        )

        assertEquals(StepSensorFlushResult.WRITTEN, result)
        assertEquals(listOf(120), recorded)
        assertEquals(null, policy.pendingFlushValue())
    }

    @Test
    fun flushPendingValue_repositoryErrorDoesNotClearPendingValue() = runTest {
        val policy = StepSensorWritePolicy()
        assertTrue(policy.shouldWrite(sensorValue = 100, nowMillis = 0))
        policy.markWritten(sensorValue = 100, nowMillis = 0)
        assertFalse(policy.shouldWrite(sensorValue = 120, nowMillis = 10_000))

        val result = flushPendingStepSensorValue(
            writePolicy = policy,
            nowMillis = 10_000,
            recordStepSensorValue = { error("boom") },
        )

        assertEquals(StepSensorFlushResult.FAILED, result)
        assertEquals(120, policy.pendingFlushValue())
    }
}
