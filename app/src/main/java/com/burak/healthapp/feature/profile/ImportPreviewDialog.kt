package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.export.HealthDataImportPreview

@Composable
internal fun ImportPreviewDialog(
    preview: HealthDataImportPreview,
    isImporting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.import_preview_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                Text(
                    text = stringResource(R.string.import_preview_settings_notice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ImportPreviewRow(stringResource(R.string.import_preview_meals), preview.mealCount)
                ImportPreviewRow(stringResource(R.string.import_preview_hydration), preview.hydrationCount)
                ImportPreviewRow(stringResource(R.string.import_preview_sleep), preview.sleepCount)
                ImportPreviewRow(stringResource(R.string.import_preview_exercise), preview.exerciseCount)
                ImportPreviewRow(stringResource(R.string.import_preview_smoking), preview.smokingCount)
                ImportPreviewRow(stringResource(R.string.import_preview_steps), preview.stepCount)
                ImportPreviewRow(stringResource(R.string.import_preview_caffeine), preview.caffeineCount)
                ImportPreviewRow(stringResource(R.string.import_preview_body_measurements), preview.bodyMeasurementCount)
                ImportPreviewRow(stringResource(R.string.import_preview_supplement_templates), preview.supplementTemplateCount)
                ImportPreviewRow(stringResource(R.string.import_preview_supplement_doses), preview.supplementDoseCount)
                ImportPreviewRow(stringResource(R.string.import_preview_custom_foods), preview.customFoodsCount)
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isImporting,
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(
                        if (isImporting) {
                            R.string.profile_import_importing
                        } else {
                            R.string.profile_import_confirm
                        },
                    ),
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isImporting,
                onClick = onDismiss,
            ) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
    )
}

@Composable
private fun ImportPreviewRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
        )
    }
}
