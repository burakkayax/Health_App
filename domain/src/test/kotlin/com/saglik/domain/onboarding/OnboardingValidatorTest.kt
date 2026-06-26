package com.saglik.domain.onboarding

import com.saglik.core.model.HealthGoal
import com.saglik.core.model.Sex
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingValidatorTest {
    @Test
    fun validAgePasses() {
        assertNull(OnboardingValidator.validateAge(35))
    }

    @Test
    fun ageBelowThirteenFails() {
        assertFalse(
            OnboardingValidator.validateForCompletion(validInput(age = 12)).isValid,
        )
    }

    @Test
    fun heightOutsideRangeFails() {
        assertFalse(
            OnboardingValidator.validateForCompletion(validInput(heightCm = 99f)).isValid,
        )
        assertFalse(
            OnboardingValidator.validateForCompletion(validInput(heightCm = 251f)).isValid,
        )
    }

    @Test
    fun weightOutsideRangeFails() {
        assertFalse(
            OnboardingValidator.validateForCompletion(validInput(startingWeightKg = 29f)).isValid,
        )
        assertFalse(
            OnboardingValidator.validateForCompletion(validInput(startingWeightKg = 301f)).isValid,
        )
    }

    @Test
    fun missingSexOrGoalPreventsCompletion() {
        assertFalse(
            OnboardingValidator.validateForCompletion(validInput(sex = null)).isValid,
        )
        assertFalse(
            OnboardingValidator.validateForCompletion(validInput(goal = null)).isValid,
        )
    }

    @Test
    fun completeValidInputPasses() {
        assertTrue(OnboardingValidator.validateForCompletion(validInput()).isValid)
    }

    private fun validInput(
        sex: Sex? = Sex.FEMALE,
        age: Int? = 35,
        heightCm: Float? = 168f,
        startingWeightKg: Float? = 68f,
        goal: HealthGoal? = HealthGoal.GENERAL_HEALTH,
    ) = CompleteOnboardingInput(
        sex = sex,
        age = age,
        heightCm = heightCm,
        startingWeightKg = startingWeightKg,
        goal = goal,
    )
}
