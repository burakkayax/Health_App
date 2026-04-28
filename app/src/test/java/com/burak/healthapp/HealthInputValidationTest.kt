package com.burak.healthapp

import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.validation.ExerciseInputValidator
import com.burak.healthapp.domain.validation.GoalSettingsValidator
import com.burak.healthapp.domain.validation.HealthInputError
import com.burak.healthapp.domain.validation.HydrationInputValidator
import com.burak.healthapp.domain.validation.MealInputValidator
import com.burak.healthapp.domain.validation.SleepInputValidator
import com.burak.healthapp.domain.validation.SmokingInputValidator
import com.burak.healthapp.domain.validation.SupplementDoseValidator
import com.burak.healthapp.domain.validation.ValidationResult
import com.burak.healthapp.domain.validation.WeightInputValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthInputValidationTest {
    @Test
    fun mealValidator_rejectsBlankNameAndNegativeMacros() {
        val result = MealInputValidator.validate(
            name = "",
            calories = "-1",
            protein = "-2",
            carbs = "10",
            fat = "5",
        )

        assertInvalidContains(result, HealthInputError.REQUIRED)
        assertInvalidContains(result, HealthInputError.MUST_NOT_BE_NEGATIVE)
    }

    @Test
    fun mealValidator_acceptsValidInput() {
        val result = MealInputValidator.validate(
            name = "Yulaf",
            calories = "300",
            protein = "20",
            carbs = "40",
            fat = "8",
        )

        assertTrue(result is ValidationResult.Valid)
        assertEquals(300, (result as ValidationResult.Valid).value.calories)
    }

    @Test
    fun mealValidator_rejectsNonIntegerAndTooHighValues() {
        val result = MealInputValidator.validate(
            name = "Bowl",
            calories = "5001",
            protein = "10.5",
            carbs = "1200",
            fat = "8",
        )

        assertInvalidContains(result, HealthInputError.TOO_HIGH)
        assertInvalidContains(result, HealthInputError.MUST_BE_INTEGER)
    }

    @Test
    fun hydrationValidator_rejectsInvalidValues() {
        assertInvalidContains(HydrationInputValidator.validate(""), HealthInputError.REQUIRED)
        assertInvalidContains(HydrationInputValidator.validate("abc"), HealthInputError.MUST_BE_INTEGER)
        assertInvalidContains(HydrationInputValidator.validate("0"), HealthInputError.MUST_BE_POSITIVE)
        assertInvalidContains(HydrationInputValidator.validate("5001"), HealthInputError.TOO_HIGH)
    }

    @Test
    fun hydrationValidator_acceptsValidInput() {
        assertEquals(ValidationResult.Valid(750), HydrationInputValidator.validate("750"))
    }

    @Test
    fun sleepValidator_supportsOvernightAndRejectsInvalidTime() {
        assertTrue(SleepInputValidator.validate("23:30", "07:00") is ValidationResult.Valid)
        assertInvalidContains(SleepInputValidator.validate("bad", "07:00"), HealthInputError.INVALID_TIME)
        assertInvalidContains(SleepInputValidator.validate("10:00", "03:00"), HealthInputError.DURATION_TOO_LONG)
    }

    @Test
    fun weightValidator_rejectsOutOfRangeValues() {
        assertInvalidContains(WeightInputValidator.validateWeight("0"), HealthInputError.MUST_BE_POSITIVE)
        assertInvalidContains(WeightInputValidator.validateWeight("10"), HealthInputError.TOO_LOW)
        assertInvalidContains(WeightInputValidator.validateWeight("401"), HealthInputError.TOO_HIGH)
    }

    @Test
    fun smokingValidator_allowsZeroButRejectsNegative() {
        assertEquals(ValidationResult.Valid(0), SmokingInputValidator.validateCount("0"))
        assertInvalidContains(SmokingInputValidator.validateCount("-1"), HealthInputError.MUST_NOT_BE_NEGATIVE)
    }

    @Test
    fun exerciseValidator_rejectsZeroAndTooLongDuration() {
        assertInvalidContains(ExerciseInputValidator.validateDuration("0"), HealthInputError.MUST_BE_POSITIVE)
        assertInvalidContains(ExerciseInputValidator.validateDuration("301"), HealthInputError.TOO_HIGH)
        assertEquals(ValidationResult.Valid(45), ExerciseInputValidator.validateDuration("45"))
    }

    @Test
    fun supplementDoseValidator_rejectsInvalidAmount() {
        assertInvalidContains(SupplementDoseValidator.validateAmount(""), HealthInputError.REQUIRED)
        assertInvalidContains(SupplementDoseValidator.validateAmount("abc"), HealthInputError.MUST_BE_NUMBER)
        assertInvalidContains(SupplementDoseValidator.validateAmount("0"), HealthInputError.MUST_BE_POSITIVE)
        assertEquals(ValidationResult.Valid(25f), SupplementDoseValidator.validateAmount("25"))
    }

    @Test
    fun goalSettingsValidator_reusesStepAndBusinessRules() {
        assertTrue(GoalSettingsValidator.validate(GoalSettings()) is ValidationResult.Valid)
        assertInvalidContains(
            GoalSettingsValidator.validate(GoalSettings(dailyStepTarget = 100_001)),
            HealthInputError.TOO_HIGH,
        )
        assertInvalidContains(
            GoalSettingsValidator.validate(GoalSettings(waterTargetMl = 0)),
            HealthInputError.MUST_BE_POSITIVE,
        )
    }

    @Test
    fun goalSettingsValidator_rejectsInvalidBusinessRanges() {
        val result = GoalSettingsValidator.validate(
            GoalSettings(
                dailyCaloriesTarget = 0,
                proteinTargetGrams = -1,
                exerciseTargetDaysPerWeek = 8,
                exerciseTargetDurationMinutes = 0,
                smokeDailyLimit = -1,
                baselineWeightKg = 0f,
            ),
        )

        assertInvalidContains(result, HealthInputError.MUST_BE_POSITIVE)
        assertInvalidContains(result, HealthInputError.MUST_NOT_BE_NEGATIVE)
        assertInvalidContains(result, HealthInputError.TOO_HIGH)
    }

    private fun assertInvalidContains(
        result: ValidationResult<*>,
        error: HealthInputError,
    ) {
        assertTrue(result is ValidationResult.Invalid)
        assertTrue((result as ValidationResult.Invalid).errors.contains(error))
    }
}
