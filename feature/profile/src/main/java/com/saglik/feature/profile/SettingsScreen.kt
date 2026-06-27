package com.saglik.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.designsystem.theme.HealthSpacing
import com.saglik.core.designsystem.theme.HealthTypography
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.core.ui.component.HealthFloatingBackButton
import com.saglik.core.ui.component.form.HealthPrimaryButton
import com.saglik.core.ui.component.form.HealthSecondaryButton
import com.saglik.core.ui.component.state.HealthInlineStatusMessage
import com.saglik.core.ui.screen.HealthGradientBackground

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    HealthGradientBackground(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = settingsContentPadding(),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.cardGap),
        ) {
            item {
                SettingsTopBar(onBackClick = onBackClick)
            }
            item {
                ProfileSummaryCard(summary = state.profileSummary)
            }
            item {
                PersonalHealthContextCard(context = state.personalHealthContext)
            }
            item {
                SettingsListSectionCard(
                    title = "Data & Privacy",
                    subtitle = "Export and delete tools will be added in a later update.",
                    accentColor = HealthColors.SystemBlue,
                    icon = Icons.Rounded.Info,
                    items = state.dataPrivacyItems,
                )
            }
            item {
                SettingsListSectionCard(
                    title = "Data Sources",
                    subtitle = "A simple source model for current and future records.",
                    accentColor = HealthColors.MoodTeal,
                    icon = Icons.Rounded.CheckCircle,
                    items = state.dataSourceItems,
                )
            }
            item {
                HealthConnectSection(items = state.healthConnectItems)
            }
            item {
                InsightsAiSection(
                    items = state.insightsAiItems,
                    safetyItems = state.safetyItems,
                )
            }
            item {
                SettingsListSectionCard(
                    title = "App Preferences",
                    subtitle = "Lightweight placeholders for future app controls.",
                    accentColor = HealthColors.SleepPurple,
                    icon = Icons.Rounded.Settings,
                    items = state.preferenceItems,
                )
            }
            item {
                SettingsListSectionCard(
                    title = "App Info",
                    subtitle = "Stable app and privacy details.",
                    accentColor = HealthColors.SecondaryText,
                    icon = Icons.Rounded.Search,
                    items = state.appInfoItems,
                )
            }
        }
    }
}

@Composable
private fun settingsContentPadding(): PaddingValues {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return PaddingValues(
        start = HealthSpacing.screenHorizontal,
        top = statusBarTop + 20.dp,
        end = HealthSpacing.screenHorizontal,
        bottom = navBarBottom + 28.dp,
    )
}

@Composable
private fun SettingsTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HealthFloatingBackButton(onClick = onBackClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = HealthColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Profile, privacy, and app controls",
                style = HealthTypography.bodyMedium,
                color = HealthColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    summary: ProfileSummaryUiState,
    modifier: Modifier = Modifier,
) {
    GlassHealthCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            ProfileAvatar(initials = summary.initials)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.displayName,
                    style = HealthTypography.titleLarge,
                    color = HealthColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = summary.supportingText,
                    style = HealthTypography.bodyMedium,
                    color = HealthColors.SecondaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        SettingsMetricList(metrics = summary.details)
        HealthSecondaryButton(
            text = summary.editActionText,
            onClick = {},
            enabled = false,
            modifier = Modifier.padding(top = 18.dp),
        )
    }
}

@Composable
private fun ProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(HealthColors.LightBlue.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = HealthTypography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = HealthColors.Ink,
            maxLines = 1,
        )
    }
}

@Composable
private fun PersonalHealthContextCard(
    context: PersonalHealthContextUiState,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(
        title = "Personal Health Context",
        accentColor = HealthColors.ActivityOrange,
        icon = Icons.Rounded.Favorite,
        modifier = modifier,
    ) {
        Text(
            text = context.description,
            style = HealthTypography.bodyMedium,
            color = HealthColors.SecondaryText,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsMetricList(metrics = context.metrics)
        HealthInlineStatusMessage(
            message = "More health context options will be added later.",
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun SettingsListSectionCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    items: List<SettingsItemUiState>,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(
        title = title,
        accentColor = accentColor,
        icon = icon,
        modifier = modifier,
    ) {
        Text(
            text = subtitle,
            style = HealthTypography.bodyMedium,
            color = HealthColors.SecondaryText,
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsItemList(items = items)
    }
}

@Composable
private fun HealthConnectSection(
    items: List<SettingsItemUiState>,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(
        title = "Health Connect",
        accentColor = HealthColors.SystemBlue,
        icon = Icons.Rounded.Favorite,
        modifier = modifier,
    ) {
        Text(
            text = "Connect Health Connect to import supported health data in a future update.",
            style = HealthTypography.bodyMedium,
            color = HealthColors.SecondaryText,
        )
        HealthInlineStatusMessage(
            message = "No Health Connect data is being read yet.",
            modifier = Modifier.padding(top = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        SettingsItemList(items = items)
        HealthPrimaryButton(
            text = "Setup coming soon",
            onClick = {},
            enabled = false,
            modifier = Modifier.padding(top = 18.dp),
        )
    }
}

@Composable
private fun InsightsAiSection(
    items: List<SettingsItemUiState>,
    safetyItems: List<String>,
    modifier: Modifier = Modifier,
) {
    SettingsSectionCard(
        title = "Insights & AI",
        accentColor = HealthColors.InsightIndigo,
        icon = Icons.Rounded.Lightbulb,
        modifier = modifier,
    ) {
        HealthInlineStatusMessage(message = "AI features are not active yet.")
        Spacer(modifier = Modifier.height(16.dp))
        SettingsItemList(items = items)
        Spacer(modifier = Modifier.height(16.dp))
        SafetyNoteList(items = safetyItems)
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassHealthCard(modifier = modifier) {
        HealthCardHeader(
            title = title,
            accentColor = accentColor,
            icon = icon,
            showChevron = false,
        )
        Spacer(modifier = Modifier.height(HealthSpacing.cardVerticalSpacing))
        content()
    }
}

@Composable
private fun SettingsMetricList(
    metrics: List<SettingsMetricUiState>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        metrics.forEach { metric ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = metric.label,
                    modifier = Modifier.weight(1f),
                    style = HealthTypography.bodyMedium,
                    color = HealthColors.SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = metric.value,
                    modifier = Modifier.weight(1.2f),
                    style = HealthTypography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = HealthColors.Ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SettingsItemList(
    items: List<SettingsItemUiState>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            SettingsItemRow(item = item)
            if (index != items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 14.dp),
                    color = HealthColors.GlassBorder.copy(alpha = 0.58f),
                    thickness = 1.dp,
                )
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItemUiState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = HealthTypography.titleMedium,
                color = if (item.enabled) HealthColors.Ink else HealthColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.description,
                style = HealthTypography.bodyMedium,
                color = HealthColors.SecondaryText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        item.status?.let {
            StatusPill(text = it)
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(HealthShapeTokens.pill)
            .background(HealthColors.LightBlue.copy(alpha = 0.48f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = HealthTypography.labelMedium,
            color = HealthColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SafetyNoteList(
    items: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = HealthColors.InsightIndigo,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(18.dp),
                )
                Text(
                    text = item,
                    style = HealthTypography.bodyMedium,
                    color = HealthColors.SecondaryText,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
