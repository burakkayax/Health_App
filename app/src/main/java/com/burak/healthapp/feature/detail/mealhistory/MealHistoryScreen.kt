package com.burak.healthapp.feature.detail.mealhistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.feature.detail.mealhistory.MealHistorySectionState
import com.burak.healthapp.feature.detail.mealhistory.MealHistoryUiState
import com.burak.healthapp.core.ui.theme.HealthSpacing
import java.time.LocalDate

@Composable
fun MealHistoryRoute(selectedDate: LocalDate) {
    val viewModel: MealHistoryViewModel = viewModel(factory = MealHistoryViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    MealHistoryContent(
        state = uiState,
        onDeleteMeal = viewModel::deleteMeal,
    )
}

@Composable
fun MealHistoryContent(
    state: MealHistoryUiState,
    onDeleteMeal: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("meal_history_list"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        if (state.sections.isEmpty()) {
            item {
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Seçili tarih için öğün kaydı yok",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = "Beslenme kartındaki + Ekle aksiyonuyla öğün girmeye başladığında bu ekran otomatik dolacak.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.sections) { section ->
                MealHistorySection(
                    section = section,
                    onDeleteMeal = onDeleteMeal,
                )
            }
        }
    }
}

@Composable
private fun MealHistorySection(
    section: MealHistorySectionState,
    onDeleteMeal: (Long) -> Unit,
) {
    HealthCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        section.entries.forEachIndexed { index, entry ->
            if (index > 0) {
                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "${entry.calories} kcal • P ${entry.proteinGrams} • K ${entry.carbsGrams} • Y ${entry.fatGrams}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    modifier = Modifier.testTag("meal_history_delete_${entry.id}"),
                    onClick = { onDeleteMeal(entry.id) },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = "Öğünü sil",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
