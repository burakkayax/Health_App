package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.feature.today.TodayUiState
import java.util.Locale
@Composable
internal fun WeightCard(
    state: TodayUiState,
    onAddWeight: () -> Unit,
    onOpenDetails: () -> Unit,
) {
    val locale = remember { Locale.forLanguageTag("tr") }

    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_card")
            .clickable(onClick = onOpenDetails),
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_weight),
            trailing = {
                CardHeaderActionButton(
                    label = stringResource(R.string.common_add),
                    modifier = Modifier.testTag("weight_add_button"),
                    onClick = onAddWeight,
                )
            },
        )
        CompactRingMetricLayout(
            progress = state.weight.progress,
            color = HealthPrimary,
            headline = state.weight.headline,
            supportingLabel = state.weight.supportingLabel,
            helperLabel = state.weight.helperLabel,
        ) {
            Text(
                text = state.weight.currentWeightKg?.let { String.format(locale, "%.1f", it) } ?: "--",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}
