package com.saglik.feature.profile

data class SettingsUiState(
    val isLoading: Boolean = false,
    val profileSummary: ProfileSummaryUiState = ProfileSummaryUiState.empty(),
    val personalHealthContext: PersonalHealthContextUiState = PersonalHealthContextUiState.empty(),
    val dataPrivacyItems: List<SettingsItemUiState> = SettingsDefaults.dataPrivacyItems,
    val dataSourceItems: List<SettingsItemUiState> = SettingsDefaults.dataSourceItems,
    val healthConnectItems: List<SettingsItemUiState> = SettingsDefaults.healthConnectItems,
    val insightsAiItems: List<SettingsItemUiState> = SettingsDefaults.insightsAiItems,
    val safetyItems: List<String> = SettingsDefaults.safetyItems,
    val preferenceItems: List<SettingsItemUiState> = SettingsDefaults.preferenceItems,
    val appInfoItems: List<SettingsItemUiState> = SettingsDefaults.appInfoItems,
) {
    companion object {
        fun loading(): SettingsUiState =
            SettingsUiState(
                isLoading = true,
                profileSummary = ProfileSummaryUiState(
                    displayName = "Loading profile",
                    supportingText = "Reading your local profile details.",
                    initials = "P",
                    details = listOf(
                        SettingsMetricUiState("Age", "Loading"),
                        SettingsMetricUiState("Height", "Loading"),
                        SettingsMetricUiState("Goal", "Loading"),
                    ),
                    editActionText = "Edit profile (coming soon)",
                    hasProfile = false,
                ),
                personalHealthContext = PersonalHealthContextUiState(
                    description = "Used to personalize future trends and insights.",
                    metrics = listOf(
                        SettingsMetricUiState("Height", "Loading"),
                        SettingsMetricUiState("Weight goal", "Loading"),
                        SettingsMetricUiState("Preferred units", "Metric"),
                    ),
                ),
            )
    }
}

data class ProfileSummaryUiState(
    val displayName: String,
    val supportingText: String,
    val initials: String,
    val details: List<SettingsMetricUiState>,
    val editActionText: String,
    val hasProfile: Boolean,
) {
    companion object {
        fun empty(): ProfileSummaryUiState =
            ProfileSummaryUiState(
                displayName = "No profile yet",
                supportingText = "Complete onboarding to personalize tracking.",
                initials = "P",
                details = listOf(
                    SettingsMetricUiState("Age", "Not set"),
                    SettingsMetricUiState("Height", "Not set"),
                    SettingsMetricUiState("Goal", "Not set"),
                ),
                editActionText = "Edit profile (coming soon)",
                hasProfile = false,
            )
    }
}

data class PersonalHealthContextUiState(
    val description: String,
    val metrics: List<SettingsMetricUiState>,
) {
    companion object {
        fun empty(): PersonalHealthContextUiState =
            PersonalHealthContextUiState(
                description = "Used to personalize future trends and insights.",
                metrics = listOf(
                    SettingsMetricUiState("Height", "Not set"),
                    SettingsMetricUiState("Weight goal", "Not set"),
                    SettingsMetricUiState("Preferred units", "Metric"),
                    SettingsMetricUiState("Health context", "More options will be added later"),
                ),
            )
    }
}

data class SettingsMetricUiState(
    val label: String,
    val value: String,
)

data class SettingsItemUiState(
    val title: String,
    val description: String,
    val status: String? = null,
    val enabled: Boolean = true,
)

object SettingsDefaults {
    val dataPrivacyItems = listOf(
        SettingsItemUiState(
            title = "Privacy Summary",
            description = "Your data is currently stored on this device. External integrations will require separate permission.",
        ),
        SettingsItemUiState(
            title = "Export Data",
            description = "Export tools will be added in a later update.",
            status = "Coming soon",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Delete All Data",
            description = "A full data deletion tool will be added later.",
            status = "Coming soon",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Delete Category Data",
            description = "Category-level delete controls will be added later.",
            status = "Coming soon",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Local Data Explanation",
            description = "You stay in control of what data is connected.",
        ),
    )

    val dataSourceItems = listOf(
        SettingsItemUiState(
            title = "Manual",
            description = "Data you enter directly in the app.",
        ),
        SettingsItemUiState(
            title = "Health Connect",
            description = "Data from Health Connect after you grant permission.",
            status = "Future",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Imported",
            description = "Data imported from files or supported external sources.",
            status = "Future",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Estimated",
            description = "Values calculated or estimated by the app.",
        ),
    )

    val healthConnectItems = listOf(
        SettingsItemUiState(
            title = "Status",
            description = "No Health Connect data is being read yet.",
            status = "Not active",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Permissions",
            description = "You will choose which permissions to grant.",
            status = "Later",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Supported data later",
            description = "Weight, sleep, steps, and exercise may be connected in future updates.",
            status = "Planned",
            enabled = false,
        ),
    )

    val insightsAiItems = listOf(
        SettingsItemUiState(
            title = "Insights",
            description = "Future insights will help you understand patterns in your data.",
            status = "Coming soon",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "AI interpretation",
            description = "AI features are not active yet.",
            status = "Not active",
            enabled = false,
        ),
    )

    val safetyItems = listOf(
        "The app does not diagnose conditions.",
        "The app does not provide medication dose or treatment advice.",
        "Health insights are for tracking and awareness, not medical diagnosis.",
    )

    val preferenceItems = listOf(
        SettingsItemUiState(
            title = "Units",
            description = "Metric and imperial controls will be added later.",
            status = "Coming soon",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Theme",
            description = "Theme controls will be added later.",
            status = "Coming soon",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Notifications",
            description = "Notification settings will be added later.",
            status = "Coming soon",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Language",
            description = "Language settings will be added later.",
            status = "Coming soon",
            enabled = false,
        ),
    )

    val appInfoItems = listOf(
        SettingsItemUiState(
            title = "App name",
            description = "Health App",
        ),
        SettingsItemUiState(
            title = "Version",
            description = "Version details will appear when build metadata is exposed to this screen.",
            status = "Later",
            enabled = false,
        ),
        SettingsItemUiState(
            title = "Privacy note",
            description = "This foundation does not add cloud sync, Health Connect reading, export, delete, or AI processing.",
        ),
        SettingsItemUiState(
            title = "Repository",
            description = "Open-source details can be linked in a later update.",
            status = "Placeholder",
            enabled = false,
        ),
    )
}
