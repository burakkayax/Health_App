package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.model.ThemeMode

@Composable
internal fun ProfileThemeCard(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_theme_section"),
    ) {
        Text(
            text = stringResource(R.string.profile_theme_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.profile_theme_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        SegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            options = listOf(
                stringResource(R.string.profile_theme_light),
                stringResource(R.string.profile_theme_dark),
                stringResource(R.string.profile_theme_system),
            ),
            selectedIndex = when (themeMode) {
                ThemeMode.LIGHT -> 0
                ThemeMode.DARK -> 1
                ThemeMode.SYSTEM -> 2
            },
            onSelectionChange = { index ->
                onThemeModeChange(
                    when (index) {
                        0 -> ThemeMode.LIGHT
                        1 -> ThemeMode.DARK
                        else -> ThemeMode.SYSTEM
                    },
                )
            },
        )
    }
}
