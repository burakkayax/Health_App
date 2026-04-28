package com.burak.healthapp

import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.defaultDashboardCardConfig
import com.burak.healthapp.domain.model.sanitizeDashboardCardConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DashboardCardConfigTest {
    @Test
    fun defaultConfig_containsAllCardsInExpectedOrder() {
        val config = defaultDashboardCardConfig()

        assertEquals(
            listOf(
                DashboardCardType.NUTRITION,
                DashboardCardType.WEIGHT,
                DashboardCardType.EXERCISE,
                DashboardCardType.STEPS,
                DashboardCardType.HYDRATION,
                DashboardCardType.SLEEP,
                DashboardCardType.SMOKING,
                DashboardCardType.SUPPLEMENTS,
            ),
            config.map(DashboardCardConfig::type),
        )
        assertEquals((0..7).toList(), config.map(DashboardCardConfig::sortOrder))
    }

    @Test
    fun sanitizeConfig_deduplicatesNormalizesOrderAndAddsMissingDefaults() {
        val config = sanitizeDashboardCardConfig(
            listOf(
                DashboardCardConfig(DashboardCardType.STEPS, isVisible = false, sortOrder = 4),
                DashboardCardConfig(DashboardCardType.STEPS, isVisible = true, sortOrder = 5),
                DashboardCardConfig(DashboardCardType.NUTRITION, isVisible = true, sortOrder = 0),
            ),
        )

        assertEquals(DashboardCardType.NUTRITION, config[0].type)
        assertEquals(DashboardCardType.STEPS, config[1].type)
        assertFalse(config[1].isVisible)
        assertEquals(DashboardCardType.entries.toSet(), config.map(DashboardCardConfig::type).toSet())
        assertEquals((0..7).toList(), config.map(DashboardCardConfig::sortOrder))
    }
}
