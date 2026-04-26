package com.burak.healthapp

import com.burak.healthapp.feature.profile.goals.StepGoalInputError
import com.burak.healthapp.feature.profile.goals.StepGoalValidationResult
import com.burak.healthapp.feature.profile.goals.validateStepTargetInput
import org.junit.Assert.assertEquals
import org.junit.Test

class StepGoalValidationTest {
    @Test
    fun validateStepTargetInput_rejectsBlankInput() {
        assertEquals(
            StepGoalValidationResult.Invalid(StepGoalInputError.REQUIRED),
            validateStepTargetInput("   "),
        )
    }

    @Test
    fun validateStepTargetInput_rejectsNonNumberInput() {
        assertEquals(
            StepGoalValidationResult.Invalid(StepGoalInputError.MUST_BE_NUMBER),
            validateStepTargetInput("abc"),
        )
    }

    @Test
    fun validateStepTargetInput_rejectsDecimalInput() {
        assertEquals(
            StepGoalValidationResult.Invalid(StepGoalInputError.MUST_BE_NUMBER),
            validateStepTargetInput("8000.5"),
        )
    }

    @Test
    fun validateStepTargetInput_rejectsZeroAndNegativeInput() {
        assertEquals(
            StepGoalValidationResult.Invalid(StepGoalInputError.POSITIVE),
            validateStepTargetInput("0"),
        )
        assertEquals(
            StepGoalValidationResult.Invalid(StepGoalInputError.POSITIVE),
            validateStepTargetInput("-10"),
        )
    }

    @Test
    fun validateStepTargetInput_rejectsTooHighInput() {
        assertEquals(
            StepGoalValidationResult.Invalid(StepGoalInputError.TOO_HIGH),
            validateStepTargetInput("100001"),
        )
    }

    @Test
    fun validateStepTargetInput_acceptsUpperBoundAndRegularInput() {
        assertEquals(
            StepGoalValidationResult.Valid(100_000),
            validateStepTargetInput("100000"),
        )
        assertEquals(
            StepGoalValidationResult.Valid(8_000),
            validateStepTargetInput(" 8000 "),
        )
    }
}
