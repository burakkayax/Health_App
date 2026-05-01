package com.burak.healthapp

import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.defaultDashboardCardConfig
import com.burak.healthapp.domain.model.sanitizeDashboardCardConfig
import com.burak.healthapp.feature.today.TodayDashboardItem
import com.burak.healthapp.feature.today.buildDashboardItems
import com.burak.healthapp.feature.today.reorderDashboardCards
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
                DashboardCardType.CAFFEINE,
                DashboardCardType.HYDRATION,
                DashboardCardType.SLEEP,
                DashboardCardType.SMOKING,
                DashboardCardType.SUPPLEMENTS,
            ),
            config.map(DashboardCardConfig::type),
        )
        assertEquals((0..8).toList(), config.map(DashboardCardConfig::sortOrder))
    }

    @Test
    fun defaultConfig_includesSmokingAndSupplements() {
        val config = defaultDashboardCardConfig()
        val types = config.map(DashboardCardConfig::type)
        assertTrue(types.contains(DashboardCardType.SMOKING))
        assertTrue(types.contains(DashboardCardType.SUPPLEMENTS))
    }

    @Test
    fun defaultConfig_caffeineIsHiddenByDefault() {
        val config = defaultDashboardCardConfig()
        assertFalse(config.first { it.type == DashboardCardType.CAFFEINE }.isVisible)
        assertTrue(config.filterNot { it.type == DashboardCardType.CAFFEINE }.all(DashboardCardConfig::isVisible))
    }

    @Test
    fun buildDashboardItems_preservesVisibleCardOrderAndCustomizeItem() {
        val config = defaultDashboardCardConfig().map { card ->
            when (card.type) {
                DashboardCardType.HYDRATION -> card.copy(sortOrder = 0)
                DashboardCardType.NUTRITION -> card.copy(sortOrder = 1)
                else -> card.copy(sortOrder = card.sortOrder + 2)
            }
        }

        val items = buildDashboardItems(config)
        val cardTypes = items.filterIsInstance<TodayDashboardItem.Card>().map { it.config.type }

        assertEquals(DashboardCardType.HYDRATION, cardTypes.first())
        assertEquals(DashboardCardType.NUTRITION, cardTypes[1])
        assertFalse(cardTypes.contains(DashboardCardType.CAFFEINE))
        assertTrue(items.last() is TodayDashboardItem.Customize)
    }

    @Test
    fun buildDashboardItems_returnsEmptyItemWhenEveryCardIsHidden() {
        val config = defaultDashboardCardConfig().map { card -> card.copy(isVisible = false) }

        val items = buildDashboardItems(config)

        assertEquals(listOf(TodayDashboardItem.Empty), items)
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
        assertEquals((0..8).toList(), config.map(DashboardCardConfig::sortOrder))
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

        assertEquals(9, result.size)
        assertTrue(types.contains(DashboardCardType.SMOKING))
        assertTrue(types.contains(DashboardCardType.SUPPLEMENTS))
        assertTrue(types.contains(DashboardCardType.STEPS))
        assertTrue(types.contains(DashboardCardType.CAFFEINE))
        assertEquals(DashboardCardType.entries.size, result.size)
    }

    @Test
    fun reorderLogic_movesFirstItemToLaterIndex() {
        val cards = reorderDashboardCards(defaultDashboardCardConfig(), fromIndex = 0, toIndex = 3)

        assertEquals(DashboardCardType.WEIGHT, cards[0].type)
        assertEquals(DashboardCardType.EXERCISE, cards[1].type)
        assertEquals(DashboardCardType.STEPS, cards[2].type)
        assertEquals(DashboardCardType.NUTRITION, cards[3].type)
    }

    @Test
    fun reorderLogic_movesLaterItemToFirstIndex() {
        val cards = reorderDashboardCards(defaultDashboardCardConfig(), fromIndex = 3, toIndex = 0)

        assertEquals(DashboardCardType.STEPS, cards[0].type)
        assertEquals(DashboardCardType.NUTRITION, cards[1].type)
        assertEquals(DashboardCardType.WEIGHT, cards[2].type)
        assertEquals(DashboardCardType.EXERCISE, cards[3].type)
    }

    @Test
    fun reorderLogic_sameIndexIsNoOp() {
        val cards = defaultDashboardCardConfig()

        assertTrue(reorderDashboardCards(cards, fromIndex = 2, toIndex = 2) === cards)
    }

    @Test
    fun reorderLogic_hiddenItemCanBeReordered() {
        val cards = defaultDashboardCardConfig().map { config ->
            if (config.type == DashboardCardType.SMOKING) config.copy(isVisible = false) else config
        }

        val smokingIndex = cards.indexOfFirst { it.type == DashboardCardType.SMOKING }
        val reordered = reorderDashboardCards(cards, fromIndex = smokingIndex, toIndex = 0)

        assertEquals(DashboardCardType.SMOKING, reordered[0].type)
        assertFalse(reordered[0].isVisible)
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
