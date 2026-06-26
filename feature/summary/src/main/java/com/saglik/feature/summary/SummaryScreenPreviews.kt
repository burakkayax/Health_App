package com.saglik.feature.summary

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthTheme

@Preview(showBackground = true, name = "Data", device = "id:pixel_5")
@Composable
fun SummaryPreview_WithData() {
    val state = SummaryUiState(
        selectedTabRoute = SummaryRoute.route,
        weight = WeightSummary(value = "68.4 kg", delta = "-0.2 kg from last week", trend = listOf(70f, 69f, 68.6f, 68.4f)),
        bmi = BmiSummaryUiState(
            bmiText = "22.5",
            categoryText = "Healthy",
            bmiValue = 22.5f,
            category = com.saglik.domain.bmi.BmiCategory.HEALTHY,
            hasData = true,
            isLoading = false,
            missingReasonText = null
        ),
        sleep = SleepSummaryUiState(
            duration = "7 hr 12 min",
            quality = "Good sleep",
            weeklyHours = listOf(6f, 7.5f, 7f, 6.5f, 8f, 7.2f),
            hasData = true,
            isLoading = false
        ),
        activity = ActivitySummary(move = "450 cal", exercise = "30 min", stand = "8 hr"),
        mood = MoodSummary(title = "Great", tags = "Energetic")
    )

    HealthTheme {
        Surface {
            SummaryScreen(
                state = state,
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Data")
@Composable
fun SummaryPreview_EmptyData() {
    val state = SummaryUiState(
        selectedTabRoute = SummaryRoute.route,
        weight = WeightSummary(value = "Not available", delta = "Add weight to start", trend = emptyList()),
        bmi = BmiSummaryUiState(
            bmiText = "--",
            categoryText = "No data",
            bmiValue = null,
            category = null,
            hasData = false,
            isLoading = false,
            missingReasonText = "Missing height"
        ),
        sleep = SleepSummaryUiState(
            duration = "--",
            quality = "No sleep data",
            weeklyHours = emptyList(),
            hasData = false,
            isLoading = false
        ),
        activity = ActivitySummary(move = "--", exercise = "--", stand = "--"),
        mood = MoodSummary(title = "No mood", tags = "Add your mood")
    )

    HealthTheme {
        Surface {
            SummaryScreen(
                state = state,
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
fun SummaryPreview_Loading() {
    HealthTheme {
        Surface {
            SummaryScreen(
                state = SummaryUiState.loading(),
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Small Phone", device = "spec:width=320dp,height=480dp,dpi=160")
@Composable
fun SummaryPreview_SmallPhone() {
    SummaryPreview_WithData()
}

@PreviewFontScale
@Composable
fun SummaryPreview_LargeFont() {
    SummaryPreview_WithData()
}
