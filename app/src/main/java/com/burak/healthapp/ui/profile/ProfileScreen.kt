package com.burak.healthapp.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.ui.components.AvatarBadge
import com.burak.healthapp.ui.components.HealthCard
import com.burak.healthapp.ui.components.HealthPillTextField
import com.burak.healthapp.ui.components.CardHeaderActionButton
import com.burak.healthapp.ui.components.RoundedPillButton
import com.burak.healthapp.ui.components.SegmentedControl
import com.burak.healthapp.ui.model.EditableSupplementTemplateState
import com.burak.healthapp.ui.model.ProfileUiState
import com.burak.healthapp.ui.model.SupplementEditorUiState
import com.burak.healthapp.ui.theme.HealthPrimary
import com.burak.healthapp.ui.theme.HealthSpacing
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
    onOpenGoals: () -> Unit,
) {
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        state = uiState,
        onOpenGoals = onOpenGoals,
        onManageSupplements = viewModel::openSupplementEditor,
        onThemeModeChange = viewModel::updateThemeMode,
    )

    if (uiState.supplementEditor.isVisible) {
        ModalBottomSheet(onDismissRequest = viewModel::dismissSupplementEditor) {
            SupplementTemplateEditorSheet(
                state = uiState.supplementEditor,
                onNameChange = viewModel::updateSupplementName,
                onTargetAmountChange = viewModel::updateSupplementTargetAmount,
                onUnitLabelChange = viewModel::updateSupplementUnitLabel,
                onRemoveDraft = viewModel::removeSupplementDraft,
                onAddDraft = viewModel::addSupplementDraft,
                onSave = viewModel::saveSupplementTemplates,
            )
        }
    }
}

@Composable
fun ProfileContent(
    state: ProfileUiState,
    onOpenGoals: () -> Unit,
    onManageSupplements: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
                ) {
                    AvatarBadge(initials = state.avatarInitials)
                    Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                        Text(
                            text = state.userName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Hedefler, tema ve takviyeler bu ekrandan yönetilir.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Hedefler",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    CardHeaderActionButton(
                        label = "Düzenle",
                        modifier = Modifier.testTag("profile_goals_button"),
                        onClick = onOpenGoals,
                    )
                }
                Column(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
                ) {
                    state.goalSummaries.forEach { summary ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = summary.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = summary.value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
        item {
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_theme_section"),
            ) {
                Text(
                    text = "Tema Ayarları",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = "Uygulama temasını Açık, Koyu veya Sistem olarak ayarlayabilirsin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SegmentedControl(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = HealthSpacing.sm),
                    options = listOf("Açık", "Koyu", "Sistem"),
                    selectedIndex = when (state.themeMode) {
                        ThemeMode.LIGHT -> 0
                        ThemeMode.DARK -> 1
                        ThemeMode.SYSTEM -> 2
                    },
                    onSelectionChange = { index ->
                        onThemeModeChange(
                            when (index) {
                                0 -> ThemeMode.LIGHT
                                1 -> ThemeMode.DARK
                                else -> ThemeMode.SYSTEM
                            },
                        )
                    },
                )
            }
        }
        item {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Takviyeler",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    CardHeaderActionButton(
                        label = "Yönet",
                        modifier = Modifier.testTag("profile_supplements_button"),
                        onClick = onManageSupplements,
                    )
                }
                Column(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
                ) {
                    if (state.supplementTemplates.isEmpty()) {
                        Text(
                            text = "Henüz takviye eklenmedi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.supplementTemplates.forEach { template ->
                            Text(
                                text = "${template.name} • ${formatTemplateAmount(template.targetAmount, template.unitLabel)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplementTemplateEditorSheet(
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
                    text = "Takviyeleri Yönet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            item {
                Text(
                    text = "Ad, günlük hedef doz ve birim bilgisini buradan düzenleyebilirsin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(state.drafts, key = EditableSupplementTemplateState::draftId) { draft ->
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    HealthPillTextField(
                        value = draft.name,
                        onValueChange = { newValue -> onNameChange(draft.draftId, newValue) },
                        label = "Takviye Adı",
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
                            label = "Hedef Doz",
                            isError = draft.targetAmountError != null,
                        )
                        HealthPillTextField(
                            modifier = Modifier.weight(1f),
                            value = draft.unitLabel,
                            onValueChange = { newValue -> onUnitLabelChange(draft.draftId, newValue) },
                            label = "Birim",
                            isError = draft.unitLabelError != null,
                        )
                        IconButton(
                            modifier = Modifier.testTag("supplement_remove_${draft.draftId}"),
                            onClick = { onRemoveDraft(draft.draftId) },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Takviyeyi kaldır",
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
                        text = helperMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.saveErrorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                RoundedPillButton(
                    label = "Yeni Takviye",
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("supplement_template_add_button"),
                    containerColor = HealthPrimary.copy(alpha = 0.12f),
                    contentColor = HealthPrimary,
                    onClick = onAddDraft,
                )
                RoundedPillButton(
                    label = if (state.isSaving) "Kaydediliyor..." else "Takviyeleri Kaydet",
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

private fun EditableSupplementTemplateState.combinedErrorMessage(): String? {
    return listOfNotNull(
        nameError,
        targetAmountError,
        unitLabelError,
    ).distinct().takeIf { it.isNotEmpty() }?.joinToString(" ")
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
