package com.burak.healthapp.feature.app

import androidx.compose.runtime.Composable
import com.burak.healthapp.core.ui.components.HealthBottomBar
import com.burak.healthapp.core.ui.navigation.MainHealthDestination
import com.burak.healthapp.core.ui.navigation.mainDestinations

@Composable
internal fun AppBottomBar(
    currentRoute: String,
    onNavigate: (MainHealthDestination) -> Unit,
) {
    HealthBottomBar(
        destinations = mainDestinations,
        currentRoute = currentRoute,
        onNavigate = onNavigate,
    )
}
