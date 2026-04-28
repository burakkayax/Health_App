package com.burak.healthapp

import com.burak.healthapp.core.step.StepSensorWritePolicy
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
}
