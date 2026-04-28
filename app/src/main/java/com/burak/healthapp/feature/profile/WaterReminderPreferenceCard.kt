package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.WaterReminderSettings
import java.time.LocalTime

@Composable
internal fun WaterReminderPreferenceCard(
    settings: WaterReminderSettings,
    canPostNotifications: Boolean,
    message: UiText?,
    onToggle: (Boolean) -> Unit,
    onSave: (WaterReminderSettings) -> Unit,
) {
    var startTime by remember(settings) { mutableStateOf(settings.startTime.toString()) }
    var endTime by remember(settings) { mutableStateOf(settings.endTime.toString()) }
    var interval by remember(settings) { mutableStateOf(settings.intervalMinutes.toString()) }
    var formError by remember(settings) { mutableStateOf<UiText?>(null) }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_water_reminder_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_water_reminder_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = if (settings.enabled) {
                        stringResource(R.string.profile_water_reminder_status_on)
                    } else {
                        stringResource(R.string.profile_water_reminder_status_off)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                modifier = Modifier.testTag("profile_water_reminder_toggle"),
                checked = settings.enabled,
                onCheckedChange = onToggle,
            )
        }
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.profile_water_reminder_helper),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (settings.enabled && !canPostNotifications) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = stringResource(R.string.profile_water_reminder_permission_off),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        message?.let {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        ) {
            HealthPillTextField(
                modifier = Modifier.weight(1f),
                value = startTime,
                onValueChange = { startTime = it },
                label = stringResource(R.string.profile_goal_start),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
            HealthPillTextField(
                modifier = Modifier.weight(1f),
                value = endTime,
                onValueChange = { endTime = it },
                label = stringResource(R.string.profile_goal_end),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }
        HealthPillTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.xs),
            value = interval,
            onValueChange = { interval = it },
            label = stringResource(R.string.profile_goal_frequency),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = formError != null,
        )
        formError?.let {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.xs),
                text = it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        RoundedPillButton(
            label = stringResource(R.string.common_save),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm)
                .testTag("profile_water_reminder_save_button"),
            containerColor = HealthPrimary.copy(alpha = 0.12f),
            contentColor = HealthPrimary,
            onClick = {
                val parsedStart = startTime.toLocalTimeOrNull()
                val parsedEnd = endTime.toLocalTimeOrNull()
                val parsedInterval = interval.toIntOrNull()
                if (parsedStart == null || parsedEnd == null || parsedInterval == null || parsedInterval <= 0) {
                    formError = UiText.StringResource(R.string.profile_water_reminder_invalid_settings)
                    return@RoundedPillButton
                }
                formError = null
                onSave(
                    settings.copy(
                        startTime = parsedStart,
                        endTime = parsedEnd,
                        intervalMinutes = parsedInterval
                            .coerceAtLeast(DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES),
                    ),
                )
            },
        )
    }
}

private fun String.toLocalTimeOrNull(): LocalTime? = runCatching { LocalTime.parse(this.trim()) }.getOrNull()
