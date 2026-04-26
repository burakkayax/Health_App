package com.burak.healthapp.feature.detail.hydration

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.CardHeaderDestructiveButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.InsightCard
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.core.ui.components.MetricMonthRingGrid
import com.burak.healthapp.core.ui.components.ProgressBarRow
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.metricWeekdayLabels
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthWater
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.root.healthApplication
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HydrationDetailViewModel(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = combine(selectedDate, selectedPeriod) { date, period -> date to period }
        .flatMapLatest { (date, period) ->
            val startDate = if (period == TrendsPeriod.WEEKLY) {
                date.minusDays(6)
            } else {
                date.withDayOfMonth(1)
            }
            combine(
                settingsRepository.settings,
                dashboardRepository.observeHydrationBetween(startDate, date),
            ) { settings, entries ->
                buildHydrationDetailUiState(
                    entries = entries,
                    selectedDate = date,
                    period = period,
                    targetMl = settings.goalSettings.waterTargetMl,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyHydrationDetailUiState(),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }

    fun deleteHydrationEntry(id: Long) {
        viewModelScope.launch {
            dashboardRepository.deleteHydrationEntry(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HydrationDetailViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                    dashboardRepository = healthApplication().container.dashboardRepository,
                )
            }
        }
    }
}

@Composable
fun HydrationDetailRoute(
    selectedDate: LocalDate,
) {
    val viewModel: HydrationDetailViewModel = viewModel(factory = HydrationDetailViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    HydrationDetailContent(
        state = uiState,
        onSelectPeriod = viewModel::selectPeriod,
        onDeleteHydration = viewModel::deleteHydrationEntry,
    )
}

@Composable
fun HydrationDetailContent(
    state: HydrationDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
    onDeleteHydration: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("hydration_detail_screen"),
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
                options = listOf(
                    stringResource(R.string.common_weekly),
                    stringResource(R.string.common_monthly),
                ),
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
                    .testTag("hydration_detail_summary_card"),
            ) {
                val targetLabel = stringResource(R.string.today_format_ml, state.targetMl)
                Text(
                    text = stringResource(R.string.hydration_detail_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                ProgressBarRow(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    label = stringResource(R.string.today_format_ml, state.totalMl),
                    valueLabel = stringResource(R.string.hydration_detail_target, targetLabel),
                    progress = state.progress,
                    color = HealthWater,
                )
            }
        }
        item {
            InsightCard(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.hydration_detail_total_title),
                value = stringResource(R.string.today_format_ml, state.totalMl),
                subtitle = if (state.selectedPeriod == TrendsPeriod.WEEKLY) {
                    stringResource(R.string.hydration_detail_period_weekly)
                } else {
                    stringResource(R.string.hydration_detail_period_monthly)
                },
            )
        }
        item {
            InsightCard(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.hydration_detail_average_title),
                value = stringResource(R.string.today_format_ml, state.averageMl),
                subtitle = stringResource(R.string.hydration_detail_logged_days_subtitle),
            )
        }
        item {
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hydration_detail_period_card"),
            ) {
                Text(
                    text = stringResource(R.string.hydration_detail_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (!state.hasPeriodData) {
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        text = stringResource(R.string.hydration_detail_no_period_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.selectedPeriod == TrendsPeriod.MONTHLY) {
                    MetricMonthRingGrid(
                        days = state.monthDays,
                        weekdayLabels = metricWeekdayLabels(),
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        testTag = "hydration_month_ring_grid",
                        activeColor = HealthWater,
                    )
                } else {
                    Column(
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                    ) {
                        state.periodDays.forEach { day ->
                            ProgressBarRow(
                                label = day.label,
                                valueLabel = stringResource(R.string.today_format_ml, day.amountMl),
                                progress = day.progress,
                                color = HealthWater,
                            )
                        }
                    }
                }
            }
        }
        item {
            Text(
                text = stringResource(R.string.hydration_detail_entries_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (state.entries.isEmpty()) {
            item {
                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hydration_detail_empty"),
                ) {
                    Text(
                        text = stringResource(R.string.hydration_detail_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.entries, key = HydrationHistoryItemState::id) { item ->
                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hydration_history_item_${item.id}"),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
                        ) {
                            Text(
                                text = stringResource(R.string.today_format_ml, item.amountMl),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = item.timeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        CardHeaderDestructiveButton(
                            label = stringResource(R.string.common_delete),
                            modifier = Modifier.testTag("hydration_history_delete_${item.id}"),
                            contentDescription = stringResource(R.string.content_description_delete_hydration),
                            onClick = { onDeleteHydration(item.id) },
                        )
                    }
                }
            }
        }
    }
}

internal fun buildHydrationDetailUiState(
    entries: List<HydrationEntry>,
    selectedDate: LocalDate,
    period: TrendsPeriod,
    targetMl: Int,
): HydrationDetailUiState {
    val locale = Locale.forLanguageTag("tr")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale)
    val periodDays = if (period == TrendsPeriod.WEEKLY) {
        (6L downTo 0L).map(selectedDate::minusDays)
    } else {
        val monthStart = selectedDate.withDayOfMonth(1)
        (0L..java.time.temporal.ChronoUnit.DAYS.between(monthStart, selectedDate)).map(monthStart::plusDays)
    }
    val entriesByDate = entries.groupBy(HydrationEntry::date)
    val selectedEntries = entries
        .filter { entry -> entry.date == selectedDate }
        .sortedByDescending(HydrationEntry::createdAt)
    val selectedTotal = selectedEntries.sumOf(HydrationEntry::amountMl)
    val periodTotals = periodDays.map { day -> day to entriesByDate[day].orEmpty().sumOf(HydrationEntry::amountMl) }
    val loggedTotals = periodTotals.map { (_, amount) -> amount }.filter { amount -> amount > 0 }
    val averageMl = if (loggedTotals.isEmpty()) 0 else loggedTotals.sum() / loggedTotals.size
    val target = targetMl.toFloat().coerceAtLeast(1f)

    return HydrationDetailUiState(
        selectedPeriod = period,
        targetMl = targetMl,
        totalMl = selectedTotal,
        averageMl = averageMl,
        progress = clampProgress(selectedTotal.toFloat(), target),
        entries = selectedEntries.map { entry ->
            HydrationHistoryItemState(
                id = entry.id,
                amountMl = entry.amountMl,
                timeLabel = entry.createdAt.toLocalTime().format(timeFormatter),
            )
        },
        periodDays = periodTotals.map { (day, amount) ->
            HydrationSummaryDayState(
                label = if (period == TrendsPeriod.WEEKLY) day.toWeekLabel() else day.dayOfMonth.toString(),
                amountMl = amount,
                progress = clampProgress(amount.toFloat(), target),
            )
        },
        monthDays = if (period == TrendsPeriod.MONTHLY) {
            buildHydrationMonthRingDays(
                anchorDate = selectedDate,
                entriesByDate = entriesByDate,
                targetMl = targetMl,
            )
        } else {
            emptyList()
        },
        hasPeriodData = periodTotals.any { (_, amount) -> amount > 0 },
    )
}

internal fun buildHydrationMonthRingDays(
    anchorDate: LocalDate,
    entriesByDate: Map<LocalDate, List<HydrationEntry>>,
    targetMl: Int,
): List<MetricDayRingState> {
    val monthStart = anchorDate.withDayOfMonth(1)
    val monthEnd = anchorDate.withDayOfMonth(anchorDate.lengthOfMonth())
    val gridStart = monthStart.minusDays((monthStart.dayOfWeek.value - 1).toLong())
    val gridEnd = monthEnd.plusDays((7 - monthEnd.dayOfWeek.value).toLong())
    val dayCount = java.time.temporal.ChronoUnit.DAYS.between(gridStart, gridEnd).toInt() + 1
    val target = targetMl.toFloat().coerceAtLeast(1f)

    return (0 until dayCount).map { offset ->
        val date = gridStart.plusDays(offset.toLong())
        val isInCurrentMonth = date.month == anchorDate.month && date.year == anchorDate.year
        val amount = if (isInCurrentMonth) entriesByDate[date].orEmpty().sumOf(HydrationEntry::amountMl) else 0
        val progress = clampProgress(amount.toFloat(), target)
        MetricDayRingState(
            dayLabel = date.dayOfMonth.toString(),
            progress = progress,
            hasData = amount > 0,
            isInCurrentMonth = isInCurrentMonth,
            isTargetMet = amount > 0 && progress >= 1f,
        )
    }
}

private fun emptyHydrationDetailUiState(): HydrationDetailUiState {
    return HydrationDetailUiState(
        selectedPeriod = TrendsPeriod.WEEKLY,
        targetMl = 0,
        totalMl = 0,
        averageMl = 0,
        progress = 0f,
        entries = emptyList(),
        periodDays = emptyList(),
        monthDays = emptyList(),
        hasPeriodData = false,
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
