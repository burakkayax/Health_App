package com.burak.healthapp.feature.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.burak.healthapp.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.core.ui.components.HealthPillTextField
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.CardFooterLinkRow
import com.burak.healthapp.core.ui.components.CardHeaderActionButton
import com.burak.healthapp.core.ui.components.CircularProgressRing
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.SectionTitle
import com.burak.healthapp.feature.today.meal.MealDraftFoodState
import com.burak.healthapp.feature.today.meal.MealEditorUiState
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.SupplementItemState
import com.burak.healthapp.feature.today.TodayUiState
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthFat
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthProtein
import com.burak.healthapp.core.ui.theme.HealthSuccess
import com.burak.healthapp.core.ui.theme.HealthSleep
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthWater
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.burak.healthapp.feature.today.formatFloat
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
                        text = "${state.nutrition.currentCalories}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "/ ${state.nutrition.targetCalories} kcal",
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
                            text = "${macro.current}g",
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
