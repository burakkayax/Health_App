package com.burak.healthapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class HealthDestination(
    val route: String,
    val title: String,
)

sealed class MainHealthDestination(
    route: String,
    title: String,
    val icon: ImageVector,
) : HealthDestination(route, title)

data object TodayDestination : MainHealthDestination(
    route = "today",
    title = "Bugün",
    icon = Icons.Outlined.Home,
)

data object TrendsDestination : MainHealthDestination(
    route = "trends",
    title = "Eğilimler",
    icon = Icons.AutoMirrored.Outlined.ShowChart,
)

data object MealHistoryDestination : HealthDestination(
    route = "meal_history",
    title = "Öğünler",
)

data object ProfileDestination : HealthDestination(
    route = "profile",
    title = "Profil",
)

data object ProfileGoalsDestination : HealthDestination(
    route = "profile_goals",
    title = "Hedefleri Düzenle",
)

data object WeightDetailDestination : HealthDestination(
    route = "weight_detail",
    title = "Kilo Detayı",
)

data object SleepDetailDestination : HealthDestination(
    route = "sleep_detail",
    title = "Uyku Detayı",
)

data object StepDetailDestination : HealthDestination(
    route = "step_detail",
    title = "Adım Detayı",
)

val mainDestinations = listOf(
    TodayDestination,
    TrendsDestination,
)
