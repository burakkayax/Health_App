package com.burak.healthapp.feature.today.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderDestructiveButton
@Composable
internal fun DeleteIconButton(
    testTag: String,
    contentDescription: String,
    onClick: () -> Unit,
) {
    CardHeaderDestructiveButton(
        label = stringResource(R.string.common_delete),
        contentDescription = contentDescription,
        modifier = Modifier.testTag(testTag),
        icon = Icons.Outlined.DeleteOutline,
        onClick = onClick,
    )
}
