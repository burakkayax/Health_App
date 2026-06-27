package com.saglik.feature.profile

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import com.saglik.core.designsystem.theme.HealthTheme

@Preview(showBackground = true, name = "Profile Data", device = "id:pixel_5")
@Composable
fun SettingsProfilePreview_WithData() {
    HealthTheme {
        Surface {
            SettingsScreen(
                state = settingsPreviewStateWithData(),
                onBackClick = {},
                onGrantHealthConnectPermissionsClick = {},
                onOpenHealthConnectSettingsClick = {},
                onInstallOrUpdateHealthConnectClick = {},
                onRefreshHealthConnectStatusClick = {},
                onSyncHealthConnectWeightAndSleepClick = {},
                listState = rememberLazyListState(),
            )
        }
    }
}

@Preview(showBackground = true, name = "Missing Profile")
@Composable
fun SettingsProfilePreview_MissingData() {
    HealthTheme {
        Surface {
            SettingsScreen(
                state = SettingsUiState(
                    profileSummary = ProfileSummaryUiState.empty(),
                    personalHealthContext = PersonalHealthContextUiState.empty(),
                ),
                onBackClick = {},
                onGrantHealthConnectPermissionsClick = {},
                onOpenHealthConnectSettingsClick = {},
                onInstallOrUpdateHealthConnectClick = {},
                onRefreshHealthConnectStatusClick = {},
                onSyncHealthConnectWeightAndSleepClick = {},
                listState = rememberLazyListState(),
            )
        }
    }
}

@Preview(showBackground = true, name = "Small Phone", device = "spec:width=320dp,height=480dp,dpi=160")
@Composable
fun SettingsProfilePreview_SmallPhone() {
    SettingsProfilePreview_WithData()
}

@PreviewFontScale
@Composable
fun SettingsProfilePreview_LargeFont() {
    SettingsProfilePreview_WithData()
}

private fun settingsPreviewStateWithData(): SettingsUiState =
    SettingsUiState(
        profileSummary = ProfileSummaryUiState(
            displayName = "Your health profile",
            supportingText = "Basics saved from onboarding.",
            initials = "P",
            details = listOf(
                SettingsMetricUiState("Age", "32 years"),
                SettingsMetricUiState("Height", "178 cm"),
                SettingsMetricUiState("Goal", "Maintain weight"),
                SettingsMetricUiState("Sex", "Not specified"),
            ),
            editActionText = "Edit profile (coming soon)",
            hasProfile = true,
        ),
        personalHealthContext = PersonalHealthContextUiState(
            description = "Used to personalize future trends and insights.",
            metrics = listOf(
                SettingsMetricUiState("Height", "178 cm"),
                SettingsMetricUiState("Weight goal", "Maintain weight"),
                SettingsMetricUiState("Tracking goal", "Maintain weight"),
                SettingsMetricUiState("Preferred units", "Metric"),
                SettingsMetricUiState("Health context", "More health context options will be added later"),
            ),
        ),
    )
