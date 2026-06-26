package com.saglik.core.ui.component

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
import com.saglik.core.ui.chart.MiniLineChart

@Preview(showBackground = true, name = "HealthGlassCard")
@Composable
fun HealthGlassCardPreview() {
    HealthTheme {
        Surface {
            GlassHealthCard(modifier = Modifier.padding(16.dp)) {
                HealthCardHeader(
                    title = "Glass Card",
                    trailingText = "Today",
                    accentColor = HealthColors.WeightBlue,
                    icon = Icons.Rounded.AutoGraph
                )
                Text(
                    text = "This is a premium glass card",
                    modifier = Modifier.padding(top = 16.dp),
                    color = HealthColors.Ink
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "HealthPrimaryButton")
@Composable
fun HealthPrimaryButtonPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            HealthPrimaryPillButton(
                text = "Add Data",
                onClick = {},
                containerColor = HealthColors.WeightBlue
            )
        }
    }
}

@Preview(showBackground = true, name = "HealthSelectionChip")
@Composable
fun HealthSelectionChipPreview() {
    HealthTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            EditChip(onClick = {})
        }
    }
}

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
            MiniLineChart(
                values = listOf(70f, 69f, 68f, 68.4f),
                color = HealthColors.WeightBlue
            )
        }
    }
}
