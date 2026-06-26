package com.saglik.feature.weight

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthTheme

@Preview(showBackground = true, name = "Multiple Entries", device = "id:pixel_5")
@Composable
fun WeightDetailPreview_WithMultipleEntries() {
    val state = WeightDetailUiState(
        latestWeightText = "61.0 kg",
        latestEntryText = "Today, 08:00",
        trend = listOf(70f, 68f, 61f),
        highestWeightText = "70.0 kg",
        lowestWeightText = "61.0 kg",
        bmi = WeightBmiUiState(
            valueText = "21.0",
            detailText = "Healthy",
            bmiValue = 21.0f,
            category = com.saglik.domain.bmi.BmiCategory.HEALTHY,
            hasData = true
        ),
        addWeightValue = "",
        isSaving = false,
        canSave = false,
        errorMessage = null,
        history = listOf(
            WeightHistoryUiState("1", "Today, 08:00", "61.0 kg"),
            WeightHistoryUiState("2", "Yesterday, 08:00", "68.0 kg"),
            WeightHistoryUiState("3", "2 days ago", "70.0 kg")
        )
    )

    HealthTheme {
        Surface {
            WeightDetailScreen(
                state = state,
                onWeightInputChanged = {},
                onAddWeightClick = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Single Entry")
@Composable
fun WeightDetailPreview_SingleEntry() {
    val state = WeightDetailUiState(
        latestWeightText = "70.0 kg",
        latestEntryText = "Today",
        trend = listOf(70f),
        highestWeightText = "70.0 kg",
        lowestWeightText = "70.0 kg",
        bmi = WeightBmiUiState(
            valueText = "24.5",
            detailText = "Healthy",
            bmiValue = 24.5f,
            category = com.saglik.domain.bmi.BmiCategory.HEALTHY,
            hasData = true
        ),
        addWeightValue = "",
        isSaving = false,
        canSave = false,
        errorMessage = null,
        history = listOf(
            WeightHistoryUiState("1", "Today", "70.0 kg")
        )
    )

    HealthTheme {
        Surface {
            WeightDetailScreen(
                state = state,
                onWeightInputChanged = {},
                onAddWeightClick = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Data")
@Composable
fun WeightDetailPreview_Empty() {
    HealthTheme {
        Surface {
            WeightDetailScreen(
                state = WeightDetailUiState.loading(),
                onWeightInputChanged = {},
                onAddWeightClick = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Validation Error")
@Composable
fun WeightDetailPreview_ValidationError() {
    val state = WeightDetailUiState.loading().copy(
        addWeightValue = "9999",
        errorMessage = "Please enter a valid weight"
    )

    HealthTheme {
        Surface {
            WeightDetailScreen(
                state = state,
                onWeightInputChanged = {},
                onAddWeightClick = {},
                listState = rememberLazyListState(),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Small Phone", device = "spec:width=320dp,height=480dp,dpi=160")
@Composable
fun WeightDetailPreview_SmallPhone() {
    WeightDetailPreview_WithMultipleEntries()
}

@PreviewFontScale
@Composable
fun WeightDetailPreview_LargeFont() {
    WeightDetailPreview_WithMultipleEntries()
}
