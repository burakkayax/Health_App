package com.burak.healthapp.feature.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.AvatarBadge
import com.burak.healthapp.core.ui.navigation.TodayDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppTopBar(
    title: String,
    currentRoute: String,
    isMainRoute: Boolean,
    avatarInitials: String,
    onBack: () -> Unit,
    onOpenDatePicker: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground

    CenterAlignedTopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (!isMainRoute) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                    )
                }
            }
        },
        actions = {
            if (currentRoute == TodayDestination.route) {
                IconButton(
                    modifier = Modifier.testTag("today_date_button"),
                    onClick = onOpenDatePicker,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarMonth,
                        contentDescription = stringResource(R.string.content_description_date_select),
                    )
                }
            }
            if (isMainRoute) {
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .testTag("profile_avatar_button")
                        .clickable(onClick = onOpenProfile),
                ) {
                    AvatarBadge(initials = avatarInitials)
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
}
