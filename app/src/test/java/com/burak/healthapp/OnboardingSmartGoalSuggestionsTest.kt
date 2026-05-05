package com.burak.healthapp

import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.feature.onboarding.OnboardingActivityLevel
import com.burak.healthapp.feature.onboarding.OnboardingMainGoal
import com.burak.healthapp.feature.onboarding.OnboardingSex
import com.burak.healthapp.feature.onboarding.suggestCaffeineCutoffTime
import com.burak.healthapp.feature.onboarding.suggestCalories
import com.burak.healthapp.feature.onboarding.suggestCarbGrams
import com.burak.healthapp.feature.onboarding.suggestFatGrams
import com.burak.healthapp.feature.onboarding.suggestProteinGrams
import com.burak.healthapp.feature.onboarding.suggestWaterTargetMl
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalTime

class OnboardingSmartGoalSuggestionsTest {

    @Test
    fun suggestCaffeineCutoffTime_usesEightHoursBeforeBedtime() {
        val bedtime = LocalTime.of(23, 30)
        val cutoff = suggestCaffeineCutoffTime(bedtime)
        assertEquals(LocalTime.of(15, 30), cutoff)
    }

    @Test
    fun suggestCaffeineCutoffTime_handlesMidnightBedtime() {
        val bedtime = LocalTime.of(0, 30)
        val cutoff = suggestCaffeineCutoffTime(bedtime)
        // 00:30 - 8 hours = 16:30
        assertEquals(LocalTime.of(16, 30), cutoff)
    }

    @Test
    fun suggestWaterTargetMl_usesWeightAndActivityAndRoundsToNearest250() {
        // 62 * 35 = 2170
        // MODERATE = +250 = 2420
        // rounded to nearest 250 -> 2500
        val target = suggestWaterTargetMl(62f, OnboardingActivityLevel.MODERATE)
        assertEquals(2500, target)
    }

    @Test
    fun suggestWaterTargetMl_fallsBackToDefaultWhenWeightMissing() {
        val target1 = suggestWaterTargetMl(null, OnboardingActivityLevel.MODERATE)
        val target2 = suggestWaterTargetMl(0f, OnboardingActivityLevel.MODERATE)
        assertEquals(DefaultHealthGoals.WATER_TARGET_ML, target1)
        assertEquals(DefaultHealthGoals.WATER_TARGET_ML, target2)
    }

    @Test
    fun suggestCalories_usesMifflinStJeorForMale() {
        // BMR = 10*70 + 6.25*175 - 5*30 + 5 = 700 + 1093.75 - 150 + 5 = 1648.75
        // MODERATE (1.55) -> 2555.56
        // target (MAINTAIN) -> 2555.56
        // rounded to nearest 50 -> 2550
        val calories = suggestCalories(
            age = 30,
            sex = OnboardingSex.MALE,
            heightCm = 175f,
            currentWeightKg = 70f,
            activityLevel = OnboardingActivityLevel.MODERATE,
            mainGoal = OnboardingMainGoal.MAINTAIN
        )
        assertEquals(2550, calories)
    }

    @Test
    fun suggestCalories_usesMifflinStJeorForFemale() {
        // BMR = 10*60 + 6.25*165 - 5*25 - 161 = 600 + 1031.25 - 125 - 161 = 1345.25
        // LIGHT (1.375) -> 1849.71
        // target (MAINTAIN) -> 1849.71
        // rounded to nearest 50 -> 1850
        val calories = suggestCalories(
            age = 25,
            sex = OnboardingSex.FEMALE,
            heightCm = 165f,
            currentWeightKg = 60f,
            activityLevel = OnboardingActivityLevel.LIGHT,
            mainGoal = OnboardingMainGoal.MAINTAIN
        )
        assertEquals(1850, calories)
    }

    @Test
    fun suggestCalories_appliesMaintainGainLossAdjustment() {
        val baseCalories = suggestCalories(
            age = 30, sex = OnboardingSex.MALE, heightCm = 175f, currentWeightKg = 70f,
            activityLevel = OnboardingActivityLevel.MODERATE, mainGoal = OnboardingMainGoal.MAINTAIN
        )
        val gainCalories = suggestCalories(
            age = 30, sex = OnboardingSex.MALE, heightCm = 175f, currentWeightKg = 70f,
            activityLevel = OnboardingActivityLevel.MODERATE, mainGoal = OnboardingMainGoal.SLOW_GAIN
        )
        val lossCalories = suggestCalories(
            age = 30, sex = OnboardingSex.MALE, heightCm = 175f, currentWeightKg = 70f,
            activityLevel = OnboardingActivityLevel.MODERATE, mainGoal = OnboardingMainGoal.SLOW_LOSS
        )
        assertEquals(baseCalories + 250, gainCalories)
        assertEquals(baseCalories - 250, lossCalories)
    }

    @Test
    fun suggestMacros_usesProteinByWeightFatByPercentAndCarbsRemainder() {
        val weightKg = 70f
        val calories = 2500
        
        val protein = suggestProteinGrams(weightKg)
        // 70 * 1.6 = 112 -> nearest 5 = 110
        assertEquals(110, protein)
        
        val fat = suggestFatGrams(calories)
        // 2500 * 0.25 / 9 = 69.44 -> nearest 5 = 70
        assertEquals(70, fat)
        
        val carbs = suggestCarbGrams(calories, protein, fat)
        // 2500 - (110*4) - (70*9) = 2500 - 440 - 630 = 1430
        // 1430 / 4 = 357.5 -> nearest 5 = 360
        assertEquals(360, carbs)
    }

    @Test
    fun suggestNutrition_fallsBackToDefaultsWhenRequiredInputsMissing() {
        val calories = suggestCalories(
            age = null,
            sex = OnboardingSex.MALE,
            heightCm = 175f,
            currentWeightKg = 70f,
            activityLevel = OnboardingActivityLevel.MODERATE,
            mainGoal = OnboardingMainGoal.MAINTAIN
        )
        assertEquals(DefaultHealthGoals.DAILY_CALORIES, calories)

        val protein = suggestProteinGrams(null)
        assertEquals(DefaultHealthGoals.PROTEIN_GRAMS, protein)
    }
}
