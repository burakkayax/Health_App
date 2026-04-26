package com.burak.healthapp.core.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.burak.healthapp.R

sealed class HealthDestination(
    val route: String,
    @param:StringRes val titleRes: Int,
)

sealed class MainHealthDestination(
    route: String,
    @StringRes titleRes: Int,
    val icon: ImageVector,
) : HealthDestination(route, titleRes)

data object TodayDestination : MainHealthDestination(
    route = "today",
    titleRes = R.string.route_today,
    icon = Icons.Outlined.Home,
)

data object TrendsDestination : MainHealthDestination(
    route = "trends",
    titleRes = R.string.route_trends,
    icon = Icons.AutoMirrored.Outlined.ShowChart,
)

data object MealHistoryDestination : HealthDestination(
    route = "meal_history",
    titleRes = R.string.route_meals,
)

data object ProfileDestination : HealthDestination(
    route = "profile",
    titleRes = R.string.route_profile,
)

data object ProfileGoalsDestination : HealthDestination(
    route = "profile_goals",
    titleRes = R.string.route_profile_goals,
)

data object WeightDetailDestination : HealthDestination(
    route = "weight_detail",
    titleRes = R.string.route_weight_detail,
)

data object SleepDetailDestination : HealthDestination(
    route = "sleep_detail",
    titleRes = R.string.route_sleep_detail,
)

data object StepDetailDestination : HealthDestination(
    route = "step_detail",
    titleRes = R.string.route_step_detail,
)

data object HydrationDetailDestination : HealthDestination(
    route = "hydration_detail",
    titleRes = R.string.route_hydration_detail,
)

val mainDestinations = listOf(
    TodayDestination,
    TrendsDestination,
)
