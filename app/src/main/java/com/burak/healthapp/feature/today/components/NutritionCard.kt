package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardFooterLinkRow
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.CircularProgressRing
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthFat
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthProtein
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.feature.today.TodayUiState
@Composable
internal fun NutritionCard(
    state: TodayUiState,
    onAddMeal: () -> Unit,
    onOpenMealHistory: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nutrition_card"),
        footer = {
            CardFooterLinkRow(
                label = stringResource(R.string.today_title_meals),
                modifier = Modifier.testTag("nutrition_history_link"),
                onClick = onOpenMealHistory,
            )
        },
    ) {
        SectionTitle(
            title = stringResource(R.string.today_title_nutrition),
            trailing = {
                CardHeaderActionButton(
                    label = stringResource(R.string.common_add),
                    modifier = Modifier.testTag("nutrition_add_button"),
                    onClick = onAddMeal,
                )
            },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm),
            horizontalArrangement = Arrangement.Center,
        ) {
            CircularProgressRing(
                progress = state.nutrition.progress,
                color = HealthPrimary,
                modifier = Modifier.width(220.dp).height(220.dp),
                strokeWidth = 12.dp,
                trackColor = HealthPrimary.copy(alpha = 0.15f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = HealthSpacing.md),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = formatWholeNumber(state.nutrition.currentCalories),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(
                            R.string.nutrition_target_kcal_format,
                            formatWholeNumber(state.nutrition.targetCalories),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = HealthSpacing.sm, bottom = HealthSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            state.nutrition.macros.forEach { macro ->
                val color = when {
                    macro.label == "Karb" -> HealthCarbs
                    macro.label.startsWith("Ya") -> HealthFat
                    else -> HealthProtein
                }
                val trackColor = when {
                    macro.label == "Karb" -> HealthCarbs.copy(alpha = 0.15f)
                    macro.label.startsWith("Ya") -> HealthFat.copy(alpha = 0.15f)
                    else -> HealthProtein.copy(alpha = 0.15f)
                }
                CircularProgressRing(
                    progress = macro.progress,
                    color = color,
                    modifier = Modifier.width(if (macro.isEmphasized) 100.dp else 92.dp).height(if (macro.isEmphasized) 100.dp else 92.dp),
                    strokeWidth = if (macro.isEmphasized) 11.dp else 10.dp,
                    trackColor = trackColor,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = HealthSpacing.xs),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(
                                R.string.format_grams_count,
                                formatWholeNumber(macro.current),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = macro.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
            }
        }
    }
}
