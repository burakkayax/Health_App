package com.saglik.feature.sleep

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthTheme
import com.saglik.core.model.ChartPoint
import com.saglik.core.model.PeriodType
import com.saglik.core.model.SleepQuality

@Preview(showBackground = true, name = "With Data", device = "id:pixel_5")
@Composable
fun SleepDetailPreview_WithData() {
    val state = SleepDetailUiState(
        selectedPeriod = PeriodType.WEEKLY,
        latestDurationText = "7 hr 14 min",
        averageText = "7 hr 0 min",
        shortestText = "5 hr 30 min",
        longestText = "8 hr 15 min",
        chartPoints = listOf(
            ChartPoint("Mon", 6.5f),
            ChartPoint("Tue", 7.0f),
            ChartPoint("Wed", 5.5f),
            ChartPoint("Thu", 8.25f),
            ChartPoint("Fri", 7.2f),
            ChartPoint("Sat", 0f),
            ChartPoint("Sun", 0f),
        ),
        historyItems = listOf(
            SleepHistoryItemUi("1", "Today", "23:50 - 07:14", "7h 14m", "Good"),
            SleepHistoryItemUi("2", "Yesterday", "00:30 - 07:00", "6h 30m", "Fair"),
        ),
        isLoading = false,
        errorMessage = null,
        addSleep = AddSleepUiState()
    )

    HealthTheme {
        Surface {
            SleepDetailScreen(
                state = state,
                onEvent = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Data")
@Composable
fun SleepDetailPreview_Empty() {
    HealthTheme {
        Surface {
            SleepDetailScreen(
                state = SleepDetailUiState.loading(),
                onEvent = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Validation Error")
@Composable
fun SleepDetailPreview_ValidationError() {
    val state = SleepDetailUiState.loading().copy(
        addSleep = AddSleepUiState(
            errorMessage = "Sleep duration cannot be zero or negative."
        )
    )

    HealthTheme {
        Surface {
            SleepDetailScreen(
                state = state,
                onEvent = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Overnight Sleep")
@Composable
fun SleepDetailPreview_OvernightSleep() {
    val state = SleepDetailUiState.loading().copy(
        addSleep = AddSleepUiState(
            startHour = 23,
            startMinute = 50,
            wakeHour = 7,
            wakeMinute = 14,
            selectedQuality = SleepQuality.GOOD,
            note = "Slept very well"
        )
    )

    HealthTheme {
        Surface {
            SleepDetailScreen(
                state = state,
                onEvent = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Small Phone", device = "spec:width=320dp,height=480dp,dpi=160")
@Composable
fun SleepDetailPreview_SmallPhone() {
    SleepDetailPreview_WithData()
}

@PreviewFontScale
@Composable
fun SleepDetailPreview_LargeFont() {
    SleepDetailPreview_WithData()
}
