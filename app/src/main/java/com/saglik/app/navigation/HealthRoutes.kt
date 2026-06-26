package com.saglik.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Search
import com.saglik.core.ui.component.HealthBottomNavItem
import com.saglik.feature.sleep.SleepRoute
import com.saglik.feature.summary.SummaryRoute
import com.saglik.feature.weight.WeightRoute

object HealthRoutes {
    val topLevelDestinations = listOf(
        HealthBottomNavItem(SummaryRoute.route, "Summary", Icons.Rounded.Favorite),
        HealthBottomNavItem(HealthTabRoutes.trends, "Trends", Icons.Rounded.AutoGraph),
        HealthBottomNavItem(HealthTabRoutes.insights, "Insights", Icons.Rounded.Lightbulb),
        HealthBottomNavItem(HealthTabRoutes.search, "Search", Icons.Rounded.Search),
    )

    private val chromeRoutes = setOf(
        SummaryRoute.route,
        WeightRoute.route,
        SleepRoute.route,
        HealthTabRoutes.trends,
        HealthTabRoutes.insights,
        HealthTabRoutes.search
    )

    fun isMainChromeRoute(route: String?): Boolean {
        return route in chromeRoutes
    }

    fun selectedBottomRouteFor(route: String?): String {
        return when (route) {
            HealthTabRoutes.trends -> HealthTabRoutes.trends
            HealthTabRoutes.insights -> HealthTabRoutes.insights
            HealthTabRoutes.search -> HealthTabRoutes.search
            else -> SummaryRoute.route
        }
    }
}
