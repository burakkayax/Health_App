package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
internal fun StepTrackingPreferenceCard(
    enabled: Boolean,
    hasStepSensor: Boolean,
    message: UiText?,
    onToggle: (Boolean) -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_step_tracking_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.profile_step_tracking_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = when {
                        !hasStepSensor -> stringResource(R.string.profile_step_tracking_no_sensor)
                        enabled -> stringResource(R.string.profile_step_tracking_status_on)
                        else -> stringResource(R.string.profile_step_tracking_status_off)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = stringResource(R.string.profile_step_tracking_helper),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                modifier = Modifier.testTag("profile_step_tracking_toggle"),
                checked = enabled && hasStepSensor,
                enabled = hasStepSensor,
                onCheckedChange = onToggle,
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
    }
}
