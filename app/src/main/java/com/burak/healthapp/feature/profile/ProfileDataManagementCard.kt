package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
internal fun ProfileDataManagementCard(
    exportState: ProfileExportUiState,
    onExportData: () -> Unit,
    onImportData: () -> Unit,
    onDeleteAllHealthData: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_data_management_section"),
    ) {
        Text(
            text = stringResource(R.string.profile_data_management_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.profile_data_management_helper),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.export_sensitive_data_notice),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
        exportState.message?.let { message ->
            Text(
                modifier = Modifier
                    .padding(top = HealthSpacing.xs)
                    .testTag("profile_export_message"),
                text = message.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (exportState.isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
        RoundedPillButton(
            label = if (exportState.isExporting) {
                stringResource(R.string.common_saving)
            } else {
                stringResource(R.string.profile_export_data)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm)
                .testTag("profile_export_data_button"),
            containerColor = HealthPrimary,
            contentColor = Color.White,
            enabled = !exportState.isExporting,
            onClick = onExportData,
        )
        RoundedPillButton(
            label = if (exportState.isImporting) {
                stringResource(R.string.common_loading)
            } else {
                stringResource(R.string.profile_import_data)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.xs)
                .testTag("profile_import_data_button"),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            enabled = !exportState.isImporting,
            onClick = onImportData,
        )
        RoundedPillButton(
            label = if (exportState.isDeleting) {
                stringResource(R.string.common_loading)
            } else {
                stringResource(R.string.profile_delete_all_health_data)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.xs)
                .testTag("profile_delete_all_health_data_button"),
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
            contentColor = MaterialTheme.colorScheme.error,
            enabled = !exportState.isDeleting,
            onClick = onDeleteAllHealthData,
        )
    }
}
