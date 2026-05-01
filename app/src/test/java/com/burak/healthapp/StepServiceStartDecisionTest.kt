package com.burak.healthapp

import com.burak.healthapp.core.step.StepServiceStartDecision
import com.burak.healthapp.core.step.decideStepServiceStart
import org.junit.Assert.assertEquals
import org.junit.Test

class StepServiceStartDecisionTest {
    @Test
    fun stopAction_takesPriority() {
        val decision = decideStepServiceStart(
            isStopAction = true,
            stepTrackingEnabled = true,
            hasPermission = true,
            hasSensor = true,
        )

        assertEquals(StepServiceStartDecision.STOP_ACTION, decision)
    }

    @Test
    fun disabledPreference_stopsBeforeSensorRegistration() {
        val decision = decideStepServiceStart(
            isStopAction = false,
            stepTrackingEnabled = false,
            hasPermission = true,
            hasSensor = true,
        )

        assertEquals(StepServiceStartDecision.DISABLED, decision)
    }

    @Test
    fun missingPermission_stopsService() {
        val decision = decideStepServiceStart(
            isStopAction = false,
            stepTrackingEnabled = true,
            hasPermission = false,
            hasSensor = true,
        )

        assertEquals(StepServiceStartDecision.MISSING_PERMISSION, decision)
    }

    @Test
    fun missingSensor_stopsService() {
        val decision = decideStepServiceStart(
            isStopAction = false,
            stepTrackingEnabled = true,
            hasPermission = true,
            hasSensor = false,
        )

        assertEquals(StepServiceStartDecision.MISSING_SENSOR, decision)
    }

    @Test
    fun enabledPermissionAndSensor_startService() {
        val decision = decideStepServiceStart(
            isStopAction = false,
            stepTrackingEnabled = true,
            hasPermission = true,
            hasSensor = true,
        )

        assertEquals(StepServiceStartDecision.START, decision)
    }
}
