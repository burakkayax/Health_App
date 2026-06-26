package com.saglik.feature.weight

import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.chart.BmiRangeCylinderChart
import com.saglik.domain.bmi.BmiCategory

@Composable
internal fun BmiSectionCard(
    bmi: WeightBmiUiState,
    modifier: Modifier = Modifier,
) {
    val accentColor = bmi.category.accentColor()
    GlassHealthCard(modifier = modifier) {
        HealthCardHeader(
            title = "BMI",
            trailingText = if (bmi.hasData) "Today" else "Not available",
            accentColor = accentColor,
            icon = Icons.Rounded.Speed,
            showChevron = false,
        )
        Column(
            modifier = Modifier.padding(top = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = bmi.valueText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = HealthColors.Ink,
            )
            Text(
                text = bmi.detailText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (bmi.hasData) accentColor else HealthColors.SecondaryText,
                fontWeight = if (bmi.hasData) FontWeight.SemiBold else FontWeight.Normal,
            )
            BmiRangeCylinderChart(
                value = bmi.bmiValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .alpha(if (bmi.hasData) 1f else 0.42f),
            )
        }
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
