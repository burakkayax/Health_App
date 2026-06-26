package com.saglik.feature.sleep.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthTheme
import com.saglik.core.model.SleepQuality
import com.saglik.feature.sleep.AddSleepUiState
import com.saglik.feature.sleep.SleepTimeTarget

@Preview(showBackground = true, name = "Start Target")
@Composable
fun SleepTimePickerPreview_Start() {
    val state = AddSleepUiState(
        selectedPickerTarget = SleepTimeTarget.START,
        startHour = 23,
        startMinute = 30
    )
    HealthTheme {
        Surface {
            AddSleepCard(
                state = state,
                onStartTimeClick = {},
                onWakeTimeClick = {},
                onTimePickerConfirm = { _, _ -> },
                onTimePickerDismiss = {},
                onQualitySelected = {},
                onNoteChanged = {},
                onSaveClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Wake Target")
@Composable
fun SleepTimePickerPreview_Wake() {
    val state = AddSleepUiState(
        selectedPickerTarget = SleepTimeTarget.WAKE,
        wakeHour = 7,
        wakeMinute = 0
    )
    HealthTheme {
        Surface {
            AddSleepCard(
                state = state,
                onStartTimeClick = {},
                onWakeTimeClick = {},
                onTimePickerConfirm = { _, _ -> },
                onTimePickerDismiss = {},
                onQualitySelected = {},
                onNoteChanged = {},
                onSaveClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "05:00")
@Composable
fun SleepTimePickerPreview_05_00() {
    val state = AddSleepUiState(
        selectedPickerTarget = SleepTimeTarget.WAKE,
        wakeHour = 5,
        wakeMinute = 0
    )
    HealthTheme {
        Surface {
            AddSleepCard(
                state = state,
                onStartTimeClick = {},
                onWakeTimeClick = {},
                onTimePickerConfirm = { _, _ -> },
                onTimePickerDismiss = {},
                onQualitySelected = {},
                onNoteChanged = {},
                onSaveClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "23:50")
@Composable
fun SleepTimePickerPreview_23_50() {
    val state = AddSleepUiState(
        selectedPickerTarget = SleepTimeTarget.START,
        startHour = 23,
        startMinute = 50
    )
    HealthTheme {
        Surface {
            AddSleepCard(
                state = state,
                onStartTimeClick = {},
                onWakeTimeClick = {},
                onTimePickerConfirm = { _, _ -> },
                onTimePickerDismiss = {},
                onQualitySelected = {},
                onNoteChanged = {},
                onSaveClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Small Phone", device = "spec:width=320dp,height=480dp,dpi=160")
@Composable
fun SleepTimePickerPreview_SmallPhone() {
    val state = AddSleepUiState(
        startHour = 23,
        startMinute = 50,
        wakeHour = 7,
        wakeMinute = 15,
        selectedQuality = SleepQuality.GOOD
    )
    HealthTheme {
        Surface {
            AddSleepCard(
                state = state,
                onStartTimeClick = {},
                onWakeTimeClick = {},
                onTimePickerConfirm = { _, _ -> },
                onTimePickerDismiss = {},
                onQualitySelected = {},
                onNoteChanged = {},
                onSaveClick = {},
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
