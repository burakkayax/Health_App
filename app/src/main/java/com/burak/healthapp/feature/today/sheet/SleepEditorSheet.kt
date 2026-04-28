package com.burak.healthapp.feature.today.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.validation.HealthInputError
import com.burak.healthapp.domain.validation.SleepInputValidator
import com.burak.healthapp.domain.validation.ValidationResult
import java.time.LocalTime
@Composable
internal fun SleepEditorSheet(
    timeRangeLabel: String,
    onSave: (LocalTime, LocalTime) -> Unit,
) {
    var start by rememberSaveable { mutableStateOf(timeRangeLabel.substringBefore(" - ").ifBlank { "23:30" }) }
    var end by rememberSaveable { mutableStateOf(timeRangeLabel.substringAfter(" - ", "07:00")) }
    var timeError by rememberSaveable { mutableStateOf<HealthInputError?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.today_sheet_sleep_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.today_sheet_sleep_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        NumberFieldRow(
            leftLabel = stringResource(R.string.today_label_sleep_start),
            leftValue = start,
            rightLabel = stringResource(R.string.today_label_sleep_end),
            rightValue = end,
            onLeftChange = {
                start = it
                timeError = null
            },
            onRightChange = {
                end = it
                timeError = null
            },
            numeric = false,
        )
        if (timeError != null) {
            Text(
                text = timeError!!.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        RoundedPillButton(
            label = stringResource(R.string.common_save),
            modifier = Modifier.fillMaxWidth(),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            onClick = {
                when (val result = SleepInputValidator.validate(start, end)) {
                    is ValidationResult.Valid -> onSave(result.value.first, result.value.second)
                    is ValidationResult.Invalid -> timeError = result.errors.firstOrNull()
                }
            },
        )
        Spacer(modifier = Modifier.height(HealthSpacing.xs))
    }
}
