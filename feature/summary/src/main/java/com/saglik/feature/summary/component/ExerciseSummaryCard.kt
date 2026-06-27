package com.saglik.feature.summary.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.saglik.core.designsystem.theme.HealthColors
import com.saglik.core.ui.component.GlassHealthCard
import com.saglik.core.ui.component.HealthCardHeader
import com.saglik.feature.summary.ExerciseSummaryUiState

@Composable
fun ExerciseSummaryCard(
    summary: ExerciseSummaryUiState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    GlassHealthCard(modifier = modifier, onClick = onClick) {
        HealthCardHeader(
            title = "Exercise",
            trailingText = summary.trailingText(),
            accentColor = HealthColors.ActivityOrange,
            icon = Icons.Rounded.FitnessCenter,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = summary.primaryText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = HealthColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = summary.secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = HealthColors.SecondaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = summary.latestText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (summary.hasData) HealthColors.ActivityOrange else HealthColors.TertiaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun ExerciseSummaryUiState.trailingText(): String =
    when {
        isLoading -> "Loading"
        hasData -> "Latest"
        else -> "Add data"
    }
