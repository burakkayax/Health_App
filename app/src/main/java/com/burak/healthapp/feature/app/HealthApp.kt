package com.burak.healthapp.feature.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.navigation.MealHistoryDestination
import com.burak.healthapp.core.ui.navigation.ProfileDestination
import com.burak.healthapp.core.ui.navigation.ProfileGoalsDestination
import com.burak.healthapp.core.ui.navigation.SleepDetailDestination
import com.burak.healthapp.core.ui.navigation.StepDetailDestination
import com.burak.healthapp.core.ui.navigation.TodayDestination
import com.burak.healthapp.core.ui.navigation.TrendsDestination
import com.burak.healthapp.core.ui.navigation.WeightDetailDestination
import com.burak.healthapp.core.ui.navigation.mainDestinations
import com.burak.healthapp.core.notification.HealthNotifications
import com.burak.healthapp.core.ui.components.AvatarBadge
import com.burak.healthapp.core.ui.components.HealthBottomBar
import com.burak.healthapp.feature.detail.mealhistory.MealHistoryRoute
import com.burak.healthapp.feature.onboarding.OnboardingRoute
import com.burak.healthapp.feature.profile.goals.ProfileGoalsRoute
import com.burak.healthapp.feature.profile.ProfileRoute
import com.burak.healthapp.feature.root.RootUiState
import com.burak.healthapp.feature.root.RootViewModel
import com.burak.healthapp.feature.detail.sleep.SleepDetailRoute
import com.burak.healthapp.feature.detail.step.StepDetailRoute
import com.burak.healthapp.core.step.StepCounterService
import com.burak.healthapp.core.ui.theme.HealthTheme
import com.burak.healthapp.feature.today.TodayRoute
import com.burak.healthapp.feature.trends.TrendsRoute
import com.burak.healthapp.feature.detail.weight.WeightDetailRoute
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun HealthApp() {
    val rootViewModel: RootViewModel = viewModel(factory = RootViewModel.Factory)
    val rootState by rootViewModel.uiState.collectAsStateWithLifecycle()

    HealthTheme(themeMode = rootState.themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            when {
                !rootState.isLoaded -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = stringResource(R.string.common_loading))
                    }
                }

                !rootState.onboardingCompleted -> {
                    OnboardingRoute()
                }

                else -> {
                    MainShell(rootState = rootState)
                }
            }
        }
    }
}
