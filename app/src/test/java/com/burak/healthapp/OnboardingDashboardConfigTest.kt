package com.burak.healthapp

import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.defaultDashboardCardConfig
import com.burak.healthapp.feature.onboarding.buildDashboardConfigFromTrackingAreas
import com.burak.healthapp.feature.onboarding.defaultOnboardingTrackingAreas
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingDashboardConfigTest {

    @Test
    fun buildDashboardConfigFromTrackingAreas_onlySelectedCardsVisible() {
        val selected = setOf(DashboardCardType.HYDRATION, DashboardCardType.SLEEP)
        val config = buildDashboardConfigFromTrackingAreas(selected)

        val hydrationCard = config.find { it.type == DashboardCardType.HYDRATION }
        val sleepCard = config.find { it.type == DashboardCardType.SLEEP }
        val nutritionCard = config.find { it.type == DashboardCardType.NUTRITION }

        assertTrue(hydrationCard?.isVisible == true)
        assertTrue(sleepCard?.isVisible == true)
        assertFalse(nutritionCard?.isVisible == true)
    }

    @Test
    fun buildDashboardConfigFromTrackingAreas_keepsDefaultSortOrder() {
        val selected = setOf(DashboardCardType.HYDRATION, DashboardCardType.SLEEP)
        val config = buildDashboardConfigFromTrackingAreas(selected)
        val defaultConfig = defaultDashboardCardConfig()

        config.forEachIndexed { index, card ->
            assertEquals(defaultConfig[index].type, card.type)
            assertEquals(defaultConfig[index].sortOrder, card.sortOrder)
        }
    }

    @Test
    fun defaultOnboardingTrackingAreas_containsHydrationSleepNutritionStepsWeight() {
        val defaults = defaultOnboardingTrackingAreas()
        assertTrue(defaults.contains(DashboardCardType.HYDRATION))
        assertTrue(defaults.contains(DashboardCardType.SLEEP))
        assertTrue(defaults.contains(DashboardCardType.NUTRITION))
        assertTrue(defaults.contains(DashboardCardType.STEPS))
        assertTrue(defaults.contains(DashboardCardType.WEIGHT))
    }
}
