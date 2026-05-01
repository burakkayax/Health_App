package com.burak.healthapp.feature.detail.hydration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.R
import com.burak.healthapp.core.performance.DebugRoutePerformanceTrace
import com.burak.healthapp.core.performance.PerformanceLogger
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.adaptive.isCompact
import com.burak.healthapp.core.ui.components.CardHeaderDestructiveButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.InsightCard
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.core.ui.components.MetricMonthRingGrid
import com.burak.healthapp.core.ui.components.ProgressBarRow
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.metricWeekdayLabels
import com.burak.healthapp.core.ui.components.weekDayShortLabel
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthWater
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HydrationDetailViewModel @Inject constructor(
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
                PerformanceLogger.measure("HydrationDetail:state_build") {
                    buildHydrationDetailUiState(
                        entries = entries,
                        selectedDate = date,
                        period = period,
                        targetMl = settings.goalSettings.waterTargetMl,
                    )
                }
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
}

@Composable
fun HydrationDetailRoute(
    selectedDate: LocalDate,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    DebugRoutePerformanceTrace("HydrationDetailRoute")
    val viewModel: HydrationDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    HydrationDetailContent(
        state = uiState,
        onSelectPeriod = viewModel::selectPeriod,
        onDeleteHydration = viewModel::deleteHydrationEntry,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun HydrationDetailContent(
    state: HydrationDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
    onDeleteHydration: (Long) -> Unit,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    if (!windowSizeClass.isCompact) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(HealthSpacing.sm)
                .testTag("hydration_detail_screen")
                .testTag("hydration_detail_adaptive_two_pane"),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                item {
                    HydrationPeriodSelector(
                        selectedPeriod = state.selectedPeriod,
                        onSelectPeriod = onSelectPeriod,
                    )
                }
                item {
                    HydrationPeriodCard(state = state)
                }
                item {
                    HydrationSummaryAndAverage(
                        state = state,
                        compact = false,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                hydrationEntriesSection(
                    state = state,
                    onDeleteHydration = onDeleteHydration,
                )
            }
        }
        return
    }

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
            HydrationPeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onSelectPeriod = onSelectPeriod,
            )
        }
        item {
            HydrationPeriodCard(state = state)
        }
        item {
            HydrationSummaryAndAverage(
                state = state,
                compact = windowSizeClass.isCompact,
            )
        }
        hydrationEntriesSection(
            state = state,
            onDeleteHydration = onDeleteHydration,
        )
    }
}

@Composable
private fun HydrationPeriodSelector(
    selectedPeriod: TrendsPeriod,
    onSelectPeriod: (TrendsPeriod) -> Unit,
) {
    SegmentedControl(
        modifier = Modifier.fillMaxWidth(),
        options = listOf(
            stringResource(R.string.common_weekly),
            stringResource(R.string.common_monthly),
        ),
        selectedIndex = if (selectedPeriod == TrendsPeriod.WEEKLY) 0 else 1,
        onSelectionChange = { index ->
            onSelectPeriod(if (index == 0) TrendsPeriod.WEEKLY else TrendsPeriod.MONTHLY)
        },
    )
}

@Composable
private fun HydrationPeriodCard(state: HydrationDetailUiState) {
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
            HydrationWeekBarChart(
                days = state.periodDays,
                modifier = Modifier
                    .padding(top = HealthSpacing.sm)
                    .testTag("hydration_week_bar_chart"),
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.hydrationEntriesSection(
    state: HydrationDetailUiState,
    onDeleteHydration: (Long) -> Unit,
) {
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
            HydrationHistoryItem(
                item = item,
                onDeleteHydration = onDeleteHydration,
            )
        }
    }
}

@Composable
private fun HydrationHistoryItem(
    item: HydrationHistoryItemState,
    onDeleteHydration: (Long) -> Unit,
) {
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
                    text = stringResource(
                        R.string.today_format_ml_formatted,
                        formatWholeNumber(item.amountMl),
                    ),
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

@Composable
private fun HydrationSummaryAndAverage(
    state: HydrationDetailUiState,
    compact: Boolean,
) {
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
            HydrationSummaryCard(state = state)
            HydrationAverageCard(state = state)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
            HydrationSummaryCard(
                state = state,
                modifier = Modifier.weight(1f),
            )
            HydrationAverageCard(
                state = state,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HydrationSummaryCard(
    state: HydrationDetailUiState,
    modifier: Modifier = Modifier,
) {
    HealthCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hydration_detail_summary_card"),
    ) {
        val targetLabel = stringResource(
            R.string.today_format_ml_formatted,
            formatWholeNumber(state.targetMl),
        )
        Text(
            text = stringResource(R.string.hydration_detail_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        ProgressBarRow(
            modifier = Modifier.padding(top = HealthSpacing.sm),
            label = stringResource(
                R.string.today_format_ml_formatted,
                formatWholeNumber(state.totalMl),
            ),
            valueLabel = stringResource(R.string.hydration_detail_target, targetLabel),
            progress = state.progress,
            color = HealthWater,
        )
    }
}

@Composable
private fun HydrationAverageCard(
    state: HydrationDetailUiState,
    modifier: Modifier = Modifier,
) {
    InsightCard(
        modifier = modifier.fillMaxWidth(),
        title = stringResource(R.string.hydration_detail_average_title),
        value = stringResource(
            R.string.today_format_ml_formatted,
            formatWholeNumber(state.averageMl),
        ),
        subtitle = stringResource(R.string.common_average_logged_days),
    )
}

@Composable
private fun HydrationWeekBarChart(
    days: List<HydrationSummaryDayState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 220.dp),
        horizontalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEachIndexed { index, day ->
            val dayLabel = day.date?.let { weekDayShortLabel(it) } ?: day.label
            val valueLabel = stringResource(
                R.string.today_format_ml_formatted,
                formatWholeNumber(day.amountMl),
            )
            val percent = (day.progress.coerceIn(0f, 1f) * 100).toInt()
            val description = when {
                day.amountMl <= 0 -> stringResource(R.string.metric_day_ring_no_data, dayLabel)
                day.progress >= 1f -> stringResource(R.string.metric_day_ring_target_met, dayLabel, valueLabel)
                else -> stringResource(R.string.metric_day_ring_progress, dayLabel, valueLabel, percent)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .semantics {
                        contentDescription = description
                    }
                    .testTag("hydration_week_bar_$index"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                Text(
                    text = formatCompactWaterAmountMl(day.amountMl),
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
                            .fillMaxHeight(day.progress.coerceIn(0f, 1f))
                            .background(
                                color = HealthWater,
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
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
                date = day,
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
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr"))

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
            dateLabel = date.format(dateFormatter),
            valueLabel = "${formatWholeNumber(amount)} ml",
            isToday = date == LocalDate.now(),
        )
    }
}

internal fun formatCompactWaterAmountMl(amountMl: Int): String {
    if (amountMl <= 0) return "--"
    if (amountMl % 1000 == 0) return "${amountMl / 1000}L"

    val liters = amountMl / 1000f
    return String.format(Locale.US, "%.1fL", liters)
}

private fun emptyHydrationDetailUiState(): HydrationDetailUiState = HydrationDetailUiState(
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

private fun LocalDate.toWeekLabel(): String = when (dayOfWeek.value) {
    1 -> "Pzt"
    2 -> "Sal"
    3 -> "Çar"
    4 -> "Per"
    5 -> "Cum"
    6 -> "Cmt"
    else -> "Paz"
}
