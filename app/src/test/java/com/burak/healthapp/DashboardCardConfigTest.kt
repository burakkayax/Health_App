package com.burak.healthapp

import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.defaultDashboardCardConfig
import com.burak.healthapp.domain.model.sanitizeDashboardCardConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
    fun defaultConfig_includesSmokingAndSupplements() {
        val config = defaultDashboardCardConfig()
        val types = config.map(DashboardCardConfig::type)
        assertTrue(types.contains(DashboardCardType.SMOKING))
        assertTrue(types.contains(DashboardCardType.SUPPLEMENTS))
    }

    @Test
    fun defaultConfig_allCardsVisibleByDefault() {
        val config = defaultDashboardCardConfig()
        assertTrue(config.all(DashboardCardConfig::isVisible))
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

    @Test
    fun sanitizeConfig_addsMissingSmokingSupplementsAndSteps() {
        val incompleteConfig = listOf(
            DashboardCardConfig(DashboardCardType.NUTRITION, isVisible = true, sortOrder = 0),
            DashboardCardConfig(DashboardCardType.WEIGHT, isVisible = true, sortOrder = 1),
            DashboardCardConfig(DashboardCardType.EXERCISE, isVisible = true, sortOrder = 2),
            DashboardCardConfig(DashboardCardType.HYDRATION, isVisible = true, sortOrder = 3),
            DashboardCardConfig(DashboardCardType.SLEEP, isVisible = true, sortOrder = 4),
        )

        val result = sanitizeDashboardCardConfig(incompleteConfig)
        val types = result.map(DashboardCardConfig::type)

        assertEquals(8, result.size)
        assertTrue(types.contains(DashboardCardType.SMOKING))
        assertTrue(types.contains(DashboardCardType.SUPPLEMENTS))
        assertTrue(types.contains(DashboardCardType.STEPS))
        assertEquals(DashboardCardType.entries.size, result.size)
    }

    @Test
    fun reorderLogic_moveItemFromIndexToIndex() {
        val cards = defaultDashboardCardConfig().toMutableList()

        // Move STEPS (index 3) to index 1
        val item = cards.removeAt(3)
        cards.add(1, item)

        assertEquals(DashboardCardType.NUTRITION, cards[0].type)
        assertEquals(DashboardCardType.STEPS, cards[1].type)
        assertEquals(DashboardCardType.WEIGHT, cards[2].type)
        assertEquals(DashboardCardType.EXERCISE, cards[3].type)
    }

    @Test
    fun reorderLogic_hiddenItemCanBeReordered() {
        val cards = defaultDashboardCardConfig().map { config ->
            if (config.type == DashboardCardType.SMOKING) config.copy(isVisible = false) else config
        }.toMutableList()

        val smokingIndex = cards.indexOfFirst { it.type == DashboardCardType.SMOKING }
        val item = cards.removeAt(smokingIndex)
        cards.add(0, item)

        assertEquals(DashboardCardType.SMOKING, cards[0].type)
        assertFalse(cards[0].isVisible)
    }

    @Test
    fun reorderLogic_firstItemCannotMoveUp() {
        val cards = defaultDashboardCardConfig()
        val targetIndex = (0 - 1).coerceIn(0, cards.lastIndex)
        assertEquals(0, targetIndex)
    }

    @Test
    fun reorderLogic_lastItemCannotMoveDown() {
        val cards = defaultDashboardCardConfig()
        val targetIndex = (cards.lastIndex + 1).coerceIn(0, cards.lastIndex)
        assertEquals(cards.lastIndex, targetIndex)
    }
}
