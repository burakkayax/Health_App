package com.burak.healthapp.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing

@Composable
internal fun SupplementTemplateEditorSheet(
    state: SupplementEditorUiState,
    onNameChange: (Long, String) -> Unit,
    onTargetAmountChange: (Long, String) -> Unit,
    onUnitLabelChange: (Long, String) -> Unit,
    onRemoveDraft: (Long) -> Unit,
    onAddDraft: () -> Unit,
    onSave: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .imePadding()
            .navigationBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("supplement_template_sheet"),
            contentPadding = PaddingValues(
                start = HealthSpacing.md,
                end = HealthSpacing.md,
                top = HealthSpacing.xs,
                bottom = 168.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            item {
                Text(
                    text = stringResource(R.string.profile_supplements_sheet_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            item {
                Text(
                    text = stringResource(R.string.profile_supplements_sheet_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.drafts, key = EditableSupplementTemplateState::draftId) { draft ->
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    HealthPillTextField(
                        value = draft.name,
                        onValueChange = { newValue -> onNameChange(draft.draftId, newValue) },
                        label = stringResource(R.string.profile_supplement_name),
                        isError = draft.nameError != null,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = HealthSpacing.xs),
                        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        HealthPillTextField(
                            modifier = Modifier.weight(1f),
                            value = draft.targetAmount,
                            onValueChange = { newValue -> onTargetAmountChange(draft.draftId, newValue) },
                            label = stringResource(R.string.profile_supplement_target),
                            isError = draft.targetAmountError != null,
                        )
                        HealthPillTextField(
                            modifier = Modifier.weight(1f),
                            value = draft.unitLabel,
                            onValueChange = { newValue -> onUnitLabelChange(draft.draftId, newValue) },
                            label = stringResource(R.string.profile_supplement_unit),
                            isError = draft.unitLabelError != null,
                        )
                        IconButton(
                            modifier = Modifier.testTag("supplement_remove_${draft.draftId}"),
                            onClick = { onRemoveDraft(draft.draftId) },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.content_description_remove_supplement),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    draft.combinedErrorMessage()?.let { message ->
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HealthSpacing.md, vertical = HealthSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                val helperMessage = state.saveErrorMessage ?: state.validationMessage
                if (helperMessage != null) {
                    Text(
                        modifier = Modifier.testTag("supplement_editor_message"),
                        text = helperMessage.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.saveErrorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                RoundedPillButton(
                    label = stringResource(R.string.profile_new_supplement),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supplement_template_add_button"),
                    containerColor = HealthPrimary.copy(alpha = 0.12f),
                    contentColor = HealthPrimary,
                    onClick = onAddDraft,
                )
                RoundedPillButton(
                    label = if (state.isSaving) {
                        stringResource(R.string.common_saving)
                    } else {
                        stringResource(R.string.profile_save_supplements)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supplement_template_save_button"),
                    containerColor = HealthPrimary,
                    contentColor = Color.White,
                    enabled = state.canSave,
                    onClick = onSave,
                )
            }
        }
    }
}

@Composable
private fun EditableSupplementTemplateState.combinedErrorMessage(): String? {
    val messages = listOfNotNull(
        nameError,
        targetAmountError,
        unitLabelError,
    ).distinct()
    if (messages.isEmpty()) return null
    var resolvedMessage = ""
    messages.forEachIndexed { index, message ->
        if (index > 0) resolvedMessage += " "
        resolvedMessage += message.asString()
    }
    return resolvedMessage
}
