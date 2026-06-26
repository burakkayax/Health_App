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
import com.saglik.feature.sleep.AddSleepUiState
import com.saglik.feature.sleep.SleepTimeTarget
import java.util.Calendar


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
        val isStart = state.selectedPickerTarget == SleepTimeTarget.START
        val initialHour = if (isStart) state.startHour else state.wakeHour
        val initialMinute = if (isStart) state.startMinute else state.wakeMinute
        
        val localHourState = androidx.compose.runtime.remember(state.selectedPickerTarget, initialHour) { androidx.compose.runtime.mutableIntStateOf(initialHour) }
        val localMinuteState = androidx.compose.runtime.remember(state.selectedPickerTarget, initialMinute) { androidx.compose.runtime.mutableIntStateOf(initialMinute) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = onTimePickerDismiss
        ) {
            com.saglik.core.ui.component.picker.HealthBottomSheetSurface {
                com.saglik.core.ui.component.picker.HealthPickerActionRow(
                    title = if (isStart) "Sleep start" else "Wake time",
                    onCancel = onTimePickerDismiss,
                    onDone = { onTimePickerConfirm(localHourState.intValue, localMinuteState.intValue) }
                )
                com.saglik.core.ui.component.picker.HealthTimePickerContent(
                    hourContent = {
                        com.saglik.core.ui.component.picker.HealthWheelPickerColumn(
                            range = 0..23,
                            value = localHourState.intValue,
                            onValueChange = { localHourState.intValue = it }
                        )
                    },
                    minuteContent = {
                        com.saglik.core.ui.component.picker.HealthWheelPickerColumn(
                            range = 0..59,
                            value = localMinuteState.intValue,
                            onValueChange = { localMinuteState.intValue = it }
                        )
                    }
                )
            }
        }
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
