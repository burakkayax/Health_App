package com.burak.healthapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DashboardCardConfigTest {

    @Test
    fun defaultDashboardCardConfig_hasUniqueTypes() {
        val config = defaultDashboardCardConfig()
        val types = config.map { it.type }
        assertEquals(DashboardCardType.entries.size, types.size)
        assertEquals(types.toSet().size, types.size)
    }

    @Test
    fun defaultDashboardCardConfig_hasStableContinuousSortOrder() {
        val config = defaultDashboardCardConfig()
        config.forEachIndexed { index, item ->
            assertEquals(index, item.sortOrder)
        }
    }

    @Test
    fun resetDashboardCardsToDefault_restoresCanonicalDefaults() {
        val corruptConfig = listOf(
            DashboardCardConfig(DashboardCardType.STEPS, true, 5),
            DashboardCardConfig(DashboardCardType.STEPS, true, 2), // Duplicate
            DashboardCardConfig(DashboardCardType.NUTRITION, false, -1),
        )

        val sanitized = sanitizeDashboardCardConfig(corruptConfig)
        
        // Should have all types
        assertEquals(DashboardCardType.entries.size, sanitized.size)
        
        // Should be continuous
        sanitized.forEachIndexed { index, item ->
            assertEquals(index, item.sortOrder)
        }
        
        // First should be Nutrition (sorted by sortOrder -1)
        assertEquals(DashboardCardType.NUTRITION, sanitized[0].type)
        assertEquals(false, sanitized[0].isVisible)

        // Second should be Steps (sorted by sortOrder 2)
        assertEquals(DashboardCardType.STEPS, sanitized[1].type)
        assertEquals(true, sanitized[1].isVisible)
    }
}
