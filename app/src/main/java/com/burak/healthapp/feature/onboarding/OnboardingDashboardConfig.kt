package com.burak.healthapp.feature.onboarding

import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.defaultDashboardCardConfig

fun buildDashboardConfigFromTrackingAreas(selected: Set<DashboardCardType>): List<DashboardCardConfig> = defaultDashboardCardConfig().map { config ->
    config.copy(isVisible = selected.contains(config.type))
}
