package com.burak.healthapp.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class DashboardCardType {
    NUTRITION,
    WEIGHT,
    HYDRATION,
    SLEEP,
    EXERCISE,
    CAFFEINE,
    SMOKING,
    SUPPLEMENTS,
    STEPS,
}

@Serializable
data class DashboardCardConfig(
    val type: DashboardCardType,
    val isVisible: Boolean,
    val sortOrder: Int,
)

fun defaultDashboardCardConfig(): List<DashboardCardConfig> = listOf(
    DashboardCardType.NUTRITION,
    DashboardCardType.WEIGHT,
    DashboardCardType.EXERCISE,
    DashboardCardType.STEPS,
    DashboardCardType.CAFFEINE,
    DashboardCardType.HYDRATION,
    DashboardCardType.SLEEP,
    DashboardCardType.SMOKING,
    DashboardCardType.SUPPLEMENTS,
).mapIndexed { index, type ->
    DashboardCardConfig(
        type = type,
        isVisible = type in setOf(
            DashboardCardType.HYDRATION,
            DashboardCardType.SLEEP,
            DashboardCardType.NUTRITION,
            DashboardCardType.STEPS,
            DashboardCardType.WEIGHT,
        ),
        sortOrder = index,
    )
}

fun sanitizeDashboardCardConfig(config: List<DashboardCardConfig>): List<DashboardCardConfig> {
    val defaultsByType = defaultDashboardCardConfig().associateBy(DashboardCardConfig::type)
    val normalized = config
        .sortedWith(compareBy<DashboardCardConfig> { it.sortOrder }.thenBy { it.type.ordinal })
        .distinctBy(DashboardCardConfig::type)
        .filter { it.type in defaultsByType }

    val missing = defaultDashboardCardConfig().filter { default ->
        normalized.none { it.type == default.type }
    }

    return (normalized + missing).mapIndexed { index, item ->
        item.copy(sortOrder = index)
    }
}
