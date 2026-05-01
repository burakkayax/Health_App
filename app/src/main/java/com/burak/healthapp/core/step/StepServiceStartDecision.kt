package com.burak.healthapp.core.step

internal enum class StepServiceStartDecision {
    STOP_ACTION,
    DISABLED,
    MISSING_PERMISSION,
    MISSING_SENSOR,
    START,
}

internal fun decideStepServiceStart(
    isStopAction: Boolean,
    stepTrackingEnabled: Boolean,
    hasPermission: Boolean,
    hasSensor: Boolean,
): StepServiceStartDecision = when {
    isStopAction -> StepServiceStartDecision.STOP_ACTION
    !stepTrackingEnabled -> StepServiceStartDecision.DISABLED
    !hasPermission -> StepServiceStartDecision.MISSING_PERMISSION
    !hasSensor -> StepServiceStartDecision.MISSING_SENSOR
    else -> StepServiceStartDecision.START
}
