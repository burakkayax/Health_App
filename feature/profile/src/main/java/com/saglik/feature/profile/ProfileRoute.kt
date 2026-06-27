package com.saglik.feature.profile

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.saglik.core.healthconnect.HealthConnectIntents
import com.saglik.core.healthconnect.HealthConnectPermissionRequestContract
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

object ProfileRoute {
    const val route = "profile"
}

fun NavGraphBuilder.profileScreen(
    onBackClick: () -> Unit = {},
) {
    composable(ProfileRoute.route) {
        val viewModel: SettingsViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = HealthConnectPermissionRequestContract.create(),
        ) { grantedPermissions ->
            viewModel.onHealthConnectPermissionResult(grantedPermissions)
        }

        SettingsScreen(
            state = state,
            onBackClick = onBackClick,
            onGrantHealthConnectPermissionsClick = { permissions ->
                viewModel.onGrantHealthConnectPermissionsClick()
                permissionLauncher.launch(permissions)
            },
            onOpenHealthConnectSettingsClick = {
                if (!context.startActivitySafely(HealthConnectIntents.settingsIntent())) {
                    viewModel.onHealthConnectExternalActionError()
                }
            },
            onInstallOrUpdateHealthConnectClick = {
                if (
                    !context.startActivitySafely(
                        intent = HealthConnectIntents.installOrUpdateIntent(),
                        fallbackIntent = HealthConnectIntents.installOrUpdateWebIntent(),
                    )
                ) {
                    viewModel.onHealthConnectExternalActionError()
                }
            },
            onRefreshHealthConnectStatusClick = viewModel::refreshHealthConnectStatus,
        )
    }
}

private fun Context.startActivitySafely(
    intent: Intent,
    fallbackIntent: Intent? = null,
): Boolean {
    if (tryStartActivity(intent)) {
        return true
    }

    return fallbackIntent?.let(::tryStartActivity) == true
}

private fun Context.tryStartActivity(intent: Intent): Boolean {
    return try {
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: SecurityException) {
        false
    } catch (_: RuntimeException) {
        false
    }
}
