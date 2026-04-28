package com.burak.healthapp.feature.profile

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R

@Composable
internal fun DeleteHealthDataConfirmationDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.delete_health_data_title))
        },
        text = {
            Text(text = stringResource(R.string.delete_health_data_message))
        },
        confirmButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(R.string.profile_delete_all_health_data),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isDeleting,
                onClick = onDismiss,
            ) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
    )
}
