package com.saglik.feature.summary.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
    com.saglik.core.ui.component.card.HealthSummaryMetricCard(
        title = "BMI",
        icon = Icons.Rounded.Speed,
        accentColor = accentColor,
        mainValue = summary.bmiText,
        secondaryText = summary.categoryText,
        trailingText = if (summary.hasData) "Today" else "Add data",
        modifier = modifier,
        isEmpty = !summary.hasData,
        onClick = onClick,
        contentSlot = {
            BmiRangeCylinderChart(
                value = summary.bmiValue,
                modifier = Modifier
                    .width(128.dp)
                    .height(58.dp)
                    .alpha(if (summary.hasData) 1f else 0.42f),
            )
        }
    )
}

private fun BmiCategory?.accentColor(): Color =
    when (this) {
        BmiCategory.LOW -> HealthColors.WeightBlue
        BmiCategory.HEALTHY -> HealthColors.BmiGreen
        BmiCategory.HIGH -> Color(0xFFE8B600)
        BmiCategory.VERY_HIGH -> Color(0xFFFF5F57)
        BmiCategory.UNKNOWN, null -> HealthColors.SecondaryText
    }
