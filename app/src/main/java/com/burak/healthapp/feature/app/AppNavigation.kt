package com.burak.healthapp.feature.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.burak.healthapp.core.ui.navigation.CaffeineDetailDestination
import com.burak.healthapp.core.ui.navigation.HydrationDetailDestination
import com.burak.healthapp.core.ui.navigation.MealHistoryDestination
import com.burak.healthapp.core.ui.navigation.ProfileDestination
import com.burak.healthapp.core.ui.navigation.ProfileGoalsDestination
import com.burak.healthapp.core.ui.navigation.SleepDetailDestination
import com.burak.healthapp.core.ui.navigation.StepDetailDestination
import com.burak.healthapp.core.ui.navigation.TodayDestination
import com.burak.healthapp.core.ui.navigation.TrendsDestination
import com.burak.healthapp.core.ui.navigation.WeightDetailDestination
import com.burak.healthapp.feature.detail.caffeine.CaffeineDetailRoute
import com.burak.healthapp.feature.detail.hydration.HydrationDetailRoute
import com.burak.healthapp.feature.detail.mealhistory.MealHistoryRoute
import com.burak.healthapp.feature.detail.sleep.SleepDetailRoute
import com.burak.healthapp.feature.detail.step.StepDetailRoute
import com.burak.healthapp.feature.detail.weight.WeightDetailRoute
import com.burak.healthapp.feature.profile.ProfileRoute
import com.burak.healthapp.feature.profile.goals.ProfileGoalsRoute
import com.burak.healthapp.feature.today.TodayRoute
import com.burak.healthapp.feature.trends.TrendsRoute
import java.time.LocalDate

@Composable
internal fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier,
    selectedDate: LocalDate,
    avatarInitials: String,
) {
    NavHost(
        navController = navController,
        startDestination = TodayDestination.route,
        modifier = modifier,
    ) {
        composable(TodayDestination.route) {
            TodayRoute(
                selectedDate = selectedDate,
                onOpenMealHistory = { navController.navigate(MealHistoryDestination.route) },
                onOpenWeightDetail = { navController.navigate(WeightDetailDestination.route) },
                onOpenSleepDetail = { navController.navigate(SleepDetailDestination.route) },
                onOpenStepDetail = { navController.navigate(StepDetailDestination.route) },
                onOpenHydrationDetail = { navController.navigate(HydrationDetailDestination.route) },
                onOpenCaffeineDetail = { navController.navigate(CaffeineDetailDestination.route) },
            )
        }
        composable(TrendsDestination.route) {
            TrendsRoute(avatarInitials = avatarInitials)
        }
        composable(MealHistoryDestination.route) {
            MealHistoryRoute(selectedDate = selectedDate)
        }
        composable(ProfileDestination.route) {
            ProfileRoute(
                onOpenGoals = { navController.navigate(ProfileGoalsDestination.route) },
            )
        }
        composable(ProfileGoalsDestination.route) {
            ProfileGoalsRoute(
                onSaved = { navController.popBackStack() },
            )
        }
        composable(WeightDetailDestination.route) {
            WeightDetailRoute()
        }
        composable(SleepDetailDestination.route) {
            SleepDetailRoute(selectedDate = selectedDate)
        }
        composable(StepDetailDestination.route) {
            StepDetailRoute(selectedDate = selectedDate)
        }
        composable(HydrationDetailDestination.route) {
            HydrationDetailRoute(selectedDate = selectedDate)
        }
        composable(CaffeineDetailDestination.route) {
            CaffeineDetailRoute(selectedDate = selectedDate)
        }
    }
}
