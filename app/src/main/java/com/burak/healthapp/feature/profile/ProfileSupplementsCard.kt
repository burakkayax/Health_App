package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.theme.HealthSpacing
import java.util.Locale

@Composable
internal fun ProfileSupplementsCard(
    templates: List<ProfileSupplementTemplateState>,
    onManageSupplements: () -> Unit,
) {
    HealthCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.profile_supplements_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            CardHeaderActionButton(
                label = stringResource(R.string.common_manage),
                icon = null,
                modifier = Modifier.testTag("profile_supplements_button"),
                onClick = onManageSupplements,
            )
        }
        Column(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            if (templates.isEmpty()) {
                Text(
                    text = stringResource(R.string.profile_empty_supplements),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                templates.forEach { template ->
                    Text(
                        text = stringResource(
                            R.string.profile_supplement_summary_format,
                            template.name,
                            formatTemplateAmount(template.targetAmount, template.unitLabel),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

private fun formatTemplateAmount(
    amount: Float,
    unitLabel: String,
): String {
    val value = if (amount % 1f == 0f) {
        amount.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", amount)
    }
    return "$value $unitLabel"
}
