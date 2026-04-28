package com.burak.healthapp.feature.today

import com.burak.healthapp.domain.model.DashboardCardConfig

internal fun reorderDashboardCards(
    cards: List<DashboardCardConfig>,
    fromIndex: Int,
    toIndex: Int,
): List<DashboardCardConfig> {
    if (cards.isEmpty()) return cards
    val safeFromIndex = fromIndex.coerceIn(cards.indices)
    val safeToIndex = toIndex.coerceIn(cards.indices)
    if (safeFromIndex == safeToIndex) return cards

    return cards.toMutableList().apply {
        val item = removeAt(safeFromIndex)
        add(safeToIndex, item)
    }
}
