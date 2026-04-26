package com.burak.healthapp.ui.stepdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.data.repository.DashboardRepository
import com.burak.healthapp.data.repository.SettingsRepository
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.ui.components.HealthCard
import com.burak.healthapp.ui.components.InsightCard
import com.burak.healthapp.ui.components.SegmentedControl
import com.burak.healthapp.ui.model.StepBarState
import com.burak.healthapp.ui.model.StepDetailUiState
import com.burak.healthapp.ui.root.healthApplication
import com.burak.healthapp.ui.theme.HealthPrimary
import com.burak.healthapp.ui.theme.HealthSpacing
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class StepDetailViewModel(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = combine(selectedDate, selectedPeriod) { date, period -> date to period }
        .flatMapLatest { (date, period) ->
            val displayDays = if (period == TrendsPeriod.WEEKLY) {
                (6L downTo 0L).map(date::minusDays)
            } else {
                (29L downTo 0L).map(date::minusDays)
            }
            combine(
                settingsRepository.settings,
                dashboardRepository.observeStepsBetween(displayDays.first(), date),
            ) { settings, entries ->
                entries.toStepDetailUiState(
                    days = displayDays,
                    period = period,
                    targetSteps = settings.goalSettings.dailyStepTarget,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyStepDetailUiState(),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                StepDetailViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                    dashboardRepository = healthApplication().container.dashboardRepository,
                )
            }
        }
    }
}

@Composable
fun StepDetailRoute(
    selectedDate: LocalDate,
) {
    val viewModel: StepDetailViewModel = viewModel(factory = StepDetailViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    StepDetailContent(
        state = uiState,
        onSelectPeriod = viewModel::selectPeriod,
    )
}

@Composable
fun StepDetailContent(
    state: StepDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("step_detail_screen"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            SegmentedControl(
                modifier = Modifier.fillMaxWidth(),
                options = listOf("Haftalık", "Aylık"),
                selectedIndex = if (state.selectedPeriod == TrendsPeriod.WEEKLY) 0 else 1,
                onSelectionChange = { index ->
                    onSelectPeriod(if (index == 0) TrendsPeriod.WEEKLY else TrendsPeriod.MONTHLY)
                },
            )
        }
        item {
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("step_detail_chart_card"),
            ) {
                Text(
                    text = "Adım İstatistikleri",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = "Günlük hedef ${state.targetLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!state.hasData) {
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        text = "Bu dönem için henüz adım verisi yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    StepBarChart(
                        modifier = Modifier
                            .padding(top = HealthSpacing.sm)
                            .testTag("step_detail_chart"),
                        bars = state.bars,
                    )
                }
            }
        }
        item {
            InsightCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Toplam Adım",
                value = state.totalStepsLabel,
                subtitle = if (state.selectedPeriod == TrendsPeriod.WEEKLY) "Son 7 gün" else "Son 30 gün",
            )
        }
        item {
            InsightCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Ortalama Adım",
                value = state.averageStepsLabel,
                subtitle = "Kayıt olan günlere göre",
            )
        }
    }
}

@Composable
private fun StepBarChart(
    bars: List<StepBarState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        verticalAlignment = Alignment.Bottom,
    ) {
        bars.forEach { bar ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                Text(
                    text = if (bar.steps == 0) "--" else bar.steps.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(18.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(999.dp),
                        ),
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(bar.progress)
                            .background(
                                color = HealthPrimary,
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun List<StepEntry>.toStepDetailUiState(
    days: List<LocalDate>,
    period: TrendsPeriod,
    targetSteps: Int,
): StepDetailUiState {
    val entriesByDate = associateBy(StepEntry::date)
    val totalSteps = filter { it.date in days }.sumOf { it.steps }
    val loggedEntries = filter { it.date in days }
    val averageSteps = if (loggedEntries.isEmpty()) 0 else totalSteps / loggedEntries.size

    return StepDetailUiState(
        selectedPeriod = period,
        bars = days.map { date ->
            val steps = entriesByDate[date]?.steps ?: 0
            StepBarState(
                label = if (period == TrendsPeriod.WEEKLY) date.toWeekLabel() else date.dayOfMonth.toString(),
                steps = steps,
                progress = clampProgress(steps.toFloat(), targetSteps.toFloat()),
            )
        },
        totalStepsLabel = "$totalSteps adım",
        averageStepsLabel = "$averageSteps adım",
        targetLabel = "$targetSteps adım",
        hasData = loggedEntries.any { it.steps > 0 },
    )
}

private fun emptyStepDetailUiState(): StepDetailUiState {
    return StepDetailUiState(
        selectedPeriod = TrendsPeriod.WEEKLY,
        bars = emptyList(),
        totalStepsLabel = "0 adım",
        averageStepsLabel = "0 adım",
        targetLabel = "8000 adım",
        hasData = false,
    )
}

private fun LocalDate.toWeekLabel(): String {
    return when (dayOfWeek.value) {
        1 -> "P"
        2 -> "S"
        3 -> "Ç"
        4 -> "P"
        5 -> "C"
        6 -> "C"
        else -> "P"
    }
}
