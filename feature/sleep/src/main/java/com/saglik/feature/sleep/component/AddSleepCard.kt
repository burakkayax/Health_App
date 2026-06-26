package com.saglik.feature.sleep.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthShapeTokens
import com.saglik.core.common.time.SleepTimeTextFormatter
import com.saglik.core.model.SleepQuality
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.core.ui.component.HealthPrimaryPillButton
import com.saglik.feature.sleep.AddSleepUiState
import com.saglik.feature.sleep.SleepTimeTarget
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepCard(
    state: AddSleepUiState,
    onStartTimeClick: () -> Unit,
    onWakeTimeClick: () -> Unit,
    onTimePickerConfirm: (Int, Int) -> Unit,
    onTimePickerDismiss: () -> Unit,
    onQualitySelected: (SleepQuality?) -> Unit,
    onNoteChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    com.saglik.core.ui.component.card.HealthAddEntryCard(
        title = "Add Sleep",
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SleepTimeInput(
                hour = state.startHour,
                minute = state.startMinute,
                onClick = onStartTimeClick,
                label = "Sleep start",
                isError = state.errorMessage != null,
            )
            SleepTimeInput(
                hour = state.wakeHour,
                minute = state.wakeMinute,
                onClick = onWakeTimeClick,
                label = "Wake time",
                isError = state.errorMessage != null,
            )
            SleepQualitySelector(
                selectedQuality = state.selectedQuality,
                onQualitySelected = onQualitySelected,
            )
            SleepNoteInput(
                value = state.note,
                onValueChange = onNoteChanged,
            )
        }
        if (state.errorMessage != null) {
            com.saglik.core.ui.component.form.HealthValidationMessage(
                message = state.errorMessage,
                modifier = Modifier.padding(top = 14.dp)
            )
        }
        com.saglik.core.ui.component.form.HealthPrimaryButton(
            text = if (state.isSaving) "Saving" else "Add Sleep",
            onClick = onSaveClick,
            enabled = !state.isSaving,
            containerColor = HealthColors.SleepPurple,
            modifier = Modifier.padding(top = 18.dp),
        )
    }

    if (state.selectedPickerTarget != null) {
        val initialHour = if (state.selectedPickerTarget == SleepTimeTarget.START) state.startHour else state.wakeHour
        val initialMinute = if (state.selectedPickerTarget == SleepTimeTarget.START) state.startMinute else state.wakeMinute
        
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )

        AlertDialog(
            onDismissRequest = onTimePickerDismiss,
            title = { Text(text = if (state.selectedPickerTarget == SleepTimeTarget.START) "Select Sleep Start" else "Select Wake Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = { onTimePickerConfirm(timePickerState.hour, timePickerState.minute) }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = onTimePickerDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SleepNoteInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Note",
            style = MaterialTheme.typography.labelMedium,
            color = HealthColors.SecondaryText,
            fontWeight = FontWeight.SemiBold,
        )
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clip(HealthShapeTokens.card)
                .border(1.dp, HealthColors.GlassBorder, HealthShapeTokens.card)
                .background(HealthColors.GlassSurface.copy(alpha = 0.5f))
                .padding(horizontal = 18.dp, vertical = 16.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = HealthColors.Ink),
                cursorBrush = SolidColor(HealthColors.SleepPurple),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isBlank()) {
                            Text(
                                text = "Optional",
                                style = MaterialTheme.typography.bodyLarge,
                                color = HealthColors.TertiaryText.copy(alpha = 0.72f),
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
    }
}
