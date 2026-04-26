package com.burak.healthapp

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
import com.burak.healthapp.navigation.MealHistoryDestination
import com.burak.healthapp.navigation.ProfileDestination
import com.burak.healthapp.navigation.ProfileGoalsDestination
import com.burak.healthapp.navigation.SleepDetailDestination
import com.burak.healthapp.navigation.StepDetailDestination
import com.burak.healthapp.navigation.TodayDestination
import com.burak.healthapp.navigation.TrendsDestination
import com.burak.healthapp.navigation.WeightDetailDestination
import com.burak.healthapp.navigation.mainDestinations
import com.burak.healthapp.notifications.HealthNotifications
import com.burak.healthapp.ui.components.AvatarBadge
import com.burak.healthapp.ui.components.HealthBottomBar
import com.burak.healthapp.ui.mealhistory.MealHistoryRoute
import com.burak.healthapp.ui.onboarding.OnboardingRoute
import com.burak.healthapp.ui.profile.ProfileGoalsRoute
import com.burak.healthapp.ui.profile.ProfileRoute
import com.burak.healthapp.ui.root.RootUiState
import com.burak.healthapp.ui.root.RootViewModel
import com.burak.healthapp.ui.sleepdetail.SleepDetailRoute
import com.burak.healthapp.ui.stepdetail.StepDetailRoute
import com.burak.healthapp.step.StepCounterService
import com.burak.healthapp.ui.theme.HealthTheme
import com.burak.healthapp.ui.today.TodayRoute
import com.burak.healthapp.ui.trends.TrendsRoute
import com.burak.healthapp.ui.weightdetail.WeightDetailRoute
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
                        Text(text = "Yükleniyor")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(rootState: RootUiState) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TodayDestination.route
    val isMainRoute = mainDestinations.any { it.route == currentRoute }
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    val selectedDate = remember(selectedEpochDay) { LocalDate.ofEpochDay(selectedEpochDay) }
    var showDatePicker by remember { mutableStateOf(false) }
    val currentTitle = remember(currentRoute, selectedDate) {
        resolveTitle(currentRoute = currentRoute, selectedDate = selectedDate)
    }
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val activityPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            StepCounterService.start(context)
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        when {
            !context.hasStepCounterSensor() -> Unit
            context.hasActivityRecognitionPermission() -> StepCounterService.start(context)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    LaunchedEffect(rootState.waterReminderEnabled) {
        if (
            rootState.waterReminderEnabled &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !HealthNotifications.canPostNotifications(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
                    Text("Seç")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("İptal")
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
            CenterAlignedTopAppBar(
                title = { Text(text = currentTitle) },
                navigationIcon = {
                    if (!isMainRoute) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Geri",
                            )
                        }
                    }
                },
                actions = {
                    if (currentRoute == TodayDestination.route) {
                        IconButton(
                            modifier = Modifier.testTag("today_date_button"),
                            onClick = { showDatePicker = true },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CalendarMonth,
                                contentDescription = "Tarih seç",
                            )
                        }
                    }
                    if (isMainRoute) {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .testTag("profile_avatar_button")
                                .clickable { navController.navigate(ProfileDestination.route) },
                        ) {
                            AvatarBadge(initials = rootState.avatarInitials)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = backgroundColor,
                    titleContentColor = contentColor,
                    navigationIconContentColor = contentColor,
                    actionIconContentColor = contentColor,
                ),
            )
        },
        bottomBar = {
            if (isMainRoute) {
                HealthBottomBar(
                    destinations = mainDestinations,
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
        NavHost(
            navController = navController,
            startDestination = TodayDestination.route,
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding),
        ) {
            composable(TodayDestination.route) {
                TodayRoute(
                    selectedDate = selectedDate,
                    onOpenMealHistory = { navController.navigate(MealHistoryDestination.route) },
                    onOpenWeightDetail = { navController.navigate(WeightDetailDestination.route) },
                    onOpenSleepDetail = { navController.navigate(SleepDetailDestination.route) },
                    onOpenStepDetail = { navController.navigate(StepDetailDestination.route) },
                )
            }
            composable(TrendsDestination.route) {
                TrendsRoute(avatarInitials = rootState.avatarInitials)
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
        }
    }
}

private fun resolveTitle(currentRoute: String, selectedDate: LocalDate): String {
    val today = LocalDate.now()
    return when (currentRoute) {
        TrendsDestination.route -> TrendsDestination.title
        MealHistoryDestination.route -> {
            if (selectedDate == today) {
                "Bugünün Öğünleri"
            } else {
                "${selectedDate.format(screenDateFormatter())} Öğünleri"
            }
        }
        ProfileDestination.route -> ProfileDestination.title
        ProfileGoalsDestination.route -> ProfileGoalsDestination.title
        WeightDetailDestination.route -> WeightDetailDestination.title
        SleepDetailDestination.route -> SleepDetailDestination.title
        StepDetailDestination.route -> StepDetailDestination.title
        else -> {
            if (selectedDate == today) {
                TodayDestination.title
            } else {
                selectedDate.format(screenDateFormatter())
            }
        }
    }
}

private fun screenDateFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr"))
}

private fun LocalDate.toPickerMillis(): Long {
    return atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}

private fun Long.toLocalDateUtc(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun Context.hasActivityRecognitionPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
}

private fun Context.hasStepCounterSensor(): Boolean {
    val sensorManager = getSystemService(SensorManager::class.java)
    return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
}
