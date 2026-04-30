package com.burak.healthapp

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.burak.healthapp.core.ui.components.HealthBottomBar
import com.burak.healthapp.core.ui.components.HealthNavigationRail
import com.burak.healthapp.core.ui.navigation.TodayDestination
import com.burak.healthapp.core.ui.navigation.mainDestinations
import com.burak.healthapp.core.ui.navigation.railDestinations
import com.burak.healthapp.core.ui.theme.HealthTheme
import org.junit.Rule
import org.junit.Test

class AdaptiveNavigationComponentsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun compactNavigation_usesBottomBarComponent() {
        composeRule.setContent {
            HealthTheme {
                HealthBottomBar(
                    destinations = mainDestinations,
                    currentRoute = TodayDestination.route,
                    onNavigate = {},
                )
            }
        }

        composeRule.onNodeWithTag("health_bottom_navigation").assertIsDisplayed()
    }

    @Test
    fun expandedNavigation_usesNavigationRailComponent() {
        composeRule.setContent {
            HealthTheme {
                HealthNavigationRail(
                    destinations = railDestinations,
                    currentRoute = TodayDestination.route,
                    onNavigate = {},
                )
            }
        }

        composeRule.onNodeWithTag("health_navigation_rail").assertIsDisplayed()
    }
}
