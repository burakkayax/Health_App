package com.saglik.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.designsystem.theme.HealthTheme
import com.saglik.core.ui.chart.BmiRangeCylinderChart

// ==========================================
// Cards Previews
// ==========================================

@Preview(showBackground = true, name = "Health Summary Metric Card")
@Composable
fun HealthSummaryMetricCardPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.card.HealthSummaryMetricCard(
                title = "Weight",
                icon = Icons.Rounded.AutoGraph,
                accentColor = HealthColors.WeightBlue,
                mainValue = "72.4 kg",
                secondaryText = "-0.2 kg",
                trailingText = "Today",
                contentSlot = {
                    com.saglik.core.ui.component.chart.HealthMiniLineChart(
                        dataPoints = listOf(70f, 69f, 68f, 68.4f),
                        lineColor = HealthColors.WeightBlue,
                        modifier = Modifier.padding(top = 16.dp).fillMaxWidth().height(58.dp)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Detail Hero Card")
@Composable
fun HealthDetailHeroCardPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.card.HealthDetailHeroCard(
                title = "Weight Trend (All Time)",
                mainValue = "72.4 kg",
                secondaryText = "Today, 08:30"
            ) {
                Text("Content Goes Here", color = HealthColors.Ink)
            }
        }
    }
}

@Preview(showBackground = true, name = "Health Add Entry Card")
@Composable
fun HealthAddEntryCardPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.card.HealthAddEntryCard(
                title = "Add Weight"
            ) {
                Text("Form Content", color = HealthColors.Ink)
            }
        }
    }
}

@Preview(showBackground = true, name = "Health History Card")
@Composable
fun HealthHistoryCardPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.card.HealthHistoryCard(
                title = "History"
            ) {
                Text("List of History Items", color = HealthColors.Ink)
            }
        }
    }
}

@Preview(showBackground = true, name = "Health Info Card")
@Composable
fun HealthInfoCardPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.card.HealthInfoCard(
                message = "This is an important message for the user."
            )
        }
    }
}

// ==========================================
// Forms Previews
// ==========================================

@Preview(showBackground = true, name = "Health Primary Button")
@Composable
fun HealthPrimaryButtonPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.form.HealthPrimaryButton(
                text = "Add Data",
                onClick = {},
                containerColor = HealthColors.WeightBlue
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Secondary Button")
@Composable
fun HealthSecondaryButtonPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.form.HealthSecondaryButton(
                text = "Cancel",
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Number Input")
@Composable
fun HealthNumberInputPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.form.HealthNumberInput(
                value = "72.5",
                onValueChange = {},
                label = "Weight",
                suffix = "kg"
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Text Input")
@Composable
fun HealthTextInputPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.form.HealthTextInput(
                value = "Feeling good",
                onValueChange = {},
                label = "Notes"
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Time Row")
@Composable
fun HealthTimeRowPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.form.HealthTimeRow(
                label = "Bedtime",
                timeText = "10:30 PM",
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Validation Message")
@Composable
fun HealthValidationMessagePreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.form.HealthValidationMessage(
                message = "Invalid input provided"
            )
        }
    }
}

// ==========================================
// State Previews
// ==========================================

@Preview(showBackground = true, name = "Health Empty State")
@Composable
fun HealthEmptyStatePreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.state.HealthEmptyState(
                message = "No data available."
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Loading State")
@Composable
fun HealthLoadingStatePreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.state.HealthLoadingState()
        }
    }
}

@Preview(showBackground = true, name = "Health Error State")
@Composable
fun HealthErrorStatePreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.state.HealthErrorState(
                message = "Failed to load data"
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Inline Status Message")
@Composable
fun HealthInlineStatusMessagePreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.state.HealthInlineStatusMessage(
                message = "Changes saved"
            )
        }
    }
}

// ==========================================
// Chart Previews
// ==========================================

@Preview(showBackground = true, name = "BMI Indicator")
@Composable
fun BMIIndicatorPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            BmiRangeCylinderChart(value = 22.5f)
        }
    }
}

@Preview(showBackground = true, name = "Weight Chart")
@Composable
fun WeightChartPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.chart.HealthMiniLineChart(
                dataPoints = listOf(70f, 69f, 68f, 68.4f),
                lineColor = HealthColors.WeightBlue
            )
        }
    }
}

@Preview(showBackground = true, name = "Bar Chart")
@Composable
fun BarChartPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.chart.HealthBarChart(
                dataPoints = listOf(70f, 69f, 68f, 68.4f),
                barColor = HealthColors.SleepPurple
            )
        }
    }
}

// ==========================================
// Picker Previews
// ==========================================

@Preview(showBackground = true, name = "Health Wheel Picker Column")
@Composable
fun HealthWheelPickerColumnPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.picker.HealthWheelPickerColumn(
                range = 0..23,
                value = 8,
                onValueChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Time Picker Content")
@Composable
fun HealthTimePickerContentPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.picker.HealthTimePickerContent(
                hourContent = {
                    com.saglik.core.ui.component.picker.HealthWheelPickerColumn(
                        range = 0..23,
                        value = 8,
                        onValueChange = {}
                    )
                },
                minuteContent = {
                    com.saglik.core.ui.component.picker.HealthWheelPickerColumn(
                        range = 0..59,
                        value = 30,
                        onValueChange = {}
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Health Picker Action Row")
@Composable
fun HealthPickerActionRowPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            com.saglik.core.ui.component.picker.HealthPickerActionRow(
                title = "Sleep Start",
                onCancel = {},
                onDone = {}
            )
        }
    }
}
