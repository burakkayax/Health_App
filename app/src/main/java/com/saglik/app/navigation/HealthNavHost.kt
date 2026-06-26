package com.saglik.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.saglik.core.ui.component.HealthBottomNavItem
import com.saglik.core.ui.screen.HealthAppScaffold
import com.saglik.feature.addentry.addEntryScreen
import com.saglik.feature.onboarding.OnboardingRoute
import com.saglik.feature.onboarding.onboardingScreen
import com.saglik.feature.profile.ProfileRoute
import com.saglik.feature.profile.profileScreen
import com.saglik.feature.sleep.SleepDetailScreen
import com.saglik.feature.sleep.SleepDetailViewModel
import com.saglik.feature.sleep.SleepRoute
import com.saglik.feature.summary.SummaryRoute
import com.saglik.feature.summary.SummaryScreen
import com.saglik.feature.summary.SummaryViewModel
import com.saglik.feature.weight.WeightDetailScreen
import com.saglik.feature.weight.WeightDetailViewModel
import com.saglik.feature.weight.WeightRoute

@Composable
fun HealthNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = SplashRoute.route,
) {
    val bottomItems = remember { HealthRoutes.topLevelDestinations }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        splashScreen(
            onNavigateToOnboarding = {
                navController.navigate(OnboardingRoute.route) {
                    popUpTo(SplashRoute.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onNavigateToSummary = {
                navController.navigate(SummaryRoute.route) {
                    popUpTo(SplashRoute.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
        )
        onboardingScreen(
            onOnboardingComplete = {
                navController.navigate(SummaryRoute.route) {
                    popUpTo(OnboardingRoute.route) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
        )
        composable(SummaryRoute.route) {
            val viewModel: SummaryViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MainChromeRoute(
                title = "Summary",
                selectedRoute = HealthRoutes.selectedBottomRouteFor(SummaryRoute.route),
                bottomItems = bottomItems,
                onBottomTabSelected = navController::navigateHealthTab,
                onProfileClick = navController::navigateToProfile,
            ) { listState, contentPadding ->
                SummaryScreen(
                    state = state,
                    listState = listState,
                    contentPadding = contentPadding,
                    onWeightClick = {
                        navController.navigate(WeightRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    onBmiClick = {
                        navController.navigate(WeightRoute.route) {
                            launchSingleTop = true
                        }
                    },
                    onSleepClick = {
                        navController.navigate(SleepRoute.route) {
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
        composable(WeightRoute.route) {
            val viewModel: WeightDetailViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MainChromeRoute(
                title = "Weight",
                selectedRoute = HealthRoutes.selectedBottomRouteFor(WeightRoute.route),
                bottomItems = bottomItems,
                onBottomTabSelected = navController::navigateHealthTab,
                onProfileClick = navController::navigateToProfile,
            ) { listState, contentPadding ->
                WeightDetailScreen(
                    state = state,
                    onWeightInputChanged = viewModel::onWeightInputChanged,
                    onAddWeightClick = viewModel::addWeight,
                    listState = listState,
                    contentPadding = contentPadding,
                )
            }
        }
        composable(SleepRoute.route) {
            val viewModel: SleepDetailViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MainChromeRoute(
                title = "Sleep",
                selectedRoute = HealthRoutes.selectedBottomRouteFor(SleepRoute.route),
                bottomItems = bottomItems,
                onBottomTabSelected = navController::navigateHealthTab,
                onProfileClick = navController::navigateToProfile,
            ) { listState, contentPadding ->
                SleepDetailScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    listState = listState,
                    contentPadding = contentPadding,
                )
            }
        }
        addEntryScreen()
        composable(HealthTabRoutes.trends) {
            MainChromeRoute(
                title = "Trends",
                selectedRoute = HealthRoutes.selectedBottomRouteFor(HealthTabRoutes.trends),
                bottomItems = bottomItems,
                onBottomTabSelected = navController::navigateHealthTab,
                onProfileClick = navController::navigateToProfile,
            ) { listState, contentPadding ->
                HealthTabPlaceholderScreen(
                    title = "Trends",
                    listState = listState,
                    contentPadding = contentPadding,
                )
            }
        }
        composable(HealthTabRoutes.insights) {
            MainChromeRoute(
                title = "Insights",
                selectedRoute = HealthRoutes.selectedBottomRouteFor(HealthTabRoutes.insights),
                bottomItems = bottomItems,
                onBottomTabSelected = navController::navigateHealthTab,
                onProfileClick = navController::navigateToProfile,
            ) { listState, contentPadding ->
                HealthTabPlaceholderScreen(
                    title = "Insights",
                    listState = listState,
                    contentPadding = contentPadding,
                )
            }
        }
        composable(HealthTabRoutes.search) {
            MainChromeRoute(
                title = "Search",
                selectedRoute = HealthRoutes.selectedBottomRouteFor(HealthTabRoutes.search),
                bottomItems = bottomItems,
                onBottomTabSelected = navController::navigateHealthTab,
                onProfileClick = navController::navigateToProfile,
            ) { listState, contentPadding ->
                HealthTabPlaceholderScreen(
                    title = "Search",
                    listState = listState,
                    contentPadding = contentPadding,
                )
            }
        }
        profileScreen()
    }
}

@Composable
private fun MainChromeRoute(
    title: String,
    selectedRoute: String,
    bottomItems: List<HealthBottomNavItem>,
    onBottomTabSelected: (String) -> Unit,
    onProfileClick: () -> Unit,
    content: @Composable (LazyListState, PaddingValues) -> Unit,
) {
    val listState = rememberLazyListState()
    HealthAppScaffold(
        title = title,
        selectedRoute = selectedRoute,
        bottomItems = bottomItems,
        onBottomTabSelected = onBottomTabSelected,
        listState = listState,
        onProfileClick = onProfileClick,
    ) { contentPadding ->
        content(listState, contentPadding)
    }
}

private fun NavHostController.navigateHealthTab(route: String) {
    if (currentDestination?.route == route) return

    navigate(route) {
        launchSingleTop = true
        restoreState = true
        popUpTo(SummaryRoute.route) {
            saveState = true
        }
    }
}

private fun NavHostController.navigateToProfile() {
    navigate(ProfileRoute.route) {
        launchSingleTop = true
    }
}
