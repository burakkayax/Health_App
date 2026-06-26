package com.saglik.feature.summary.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.chart.BmiRangeCylinderChart
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.domain.bmi.BmiCategory
import com.saglik.feature.summary.BmiSummaryUiState

@Composable
fun BmiSummaryCard(
    summary: BmiSummaryUiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val accentColor = summary.category.accentColor()
    GlassHealthCard(modifier = modifier, onClick = onClick) {
        HealthCardHeader(
            title = "BMI",
            trailingText = if (summary.hasData) "Today" else "Add data",
            accentColor = accentColor,
            icon = Icons.Rounded.Speed,
        )
        Row(
            modifier = Modifier.padding(top = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SummaryValueText(text = summary.bmiText)
                SummarySecondaryText(
                    text = summary.categoryText,
                    color = accentColor,
                )
            }
        }
        BmiRangeCylinderChart(
            value = summary.bmiValue,
            modifier = Modifier
                .padding(top = 18.dp)
                .fillMaxWidth()
                .height(52.dp)
                .alpha(if (summary.hasData) 1f else 0.42f),
        )
    }
}

private fun BmiCategory?.accentColor(): Color =
    when (this) {
        BmiCategory.LOW -> HealthColors.WeightBlue
        BmiCategory.HEALTHY -> HealthColors.BmiGreen
        BmiCategory.HIGH -> Color(0xFFE8B600)
        BmiCategory.VERY_HIGH -> Color(0xFFFF5F57)
        BmiCategory.UNKNOWN, null -> HealthColors.SecondaryText
    }
