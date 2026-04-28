package com.burak.healthapp.feature.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.navigation.ProfileDestination
import com.burak.healthapp.core.ui.navigation.TodayDestination
import com.burak.healthapp.core.ui.navigation.mainDestinations
import com.burak.healthapp.feature.root.RootUiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainShell(rootState: RootUiState) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TodayDestination.route
    val isMainRoute = mainDestinations.any { it.route == currentRoute }
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    val selectedDate = remember(selectedEpochDay) { LocalDate.ofEpochDay(selectedEpochDay) }
    var showDatePicker by remember { mutableStateOf(false) }
    val currentTitle = resolveTitle(currentRoute = currentRoute, selectedDate = selectedDate)
    val backgroundColor = MaterialTheme.colorScheme.background

    StepCounterPermissionEffect(rootState.stepTrackingEnabled)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toPickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedEpochDay = millis.toLocalDateUtc().toEpochDay()
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.common_select))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        topBar = {
            AppTopBar(
                title = currentTitle,
                currentRoute = currentRoute,
                isMainRoute = isMainRoute,
                avatarInitials = rootState.avatarInitials,
                onBack = { navController.popBackStack() },
                onOpenDatePicker = { showDatePicker = true },
                onOpenProfile = { navController.navigate(ProfileDestination.route) },
            )
        },
        bottomBar = {
            if (isMainRoute) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(TodayDestination.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        AppNavigation(
            navController = navController,
            selectedDate = selectedDate,
            avatarInitials = rootState.avatarInitials,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding),
        )
    }
}
