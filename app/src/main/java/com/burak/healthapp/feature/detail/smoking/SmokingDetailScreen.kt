package com.burak.healthapp.feature.detail.smoking

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
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
import com.burak.healthapp.core.ui.components.EmptyGhostChart
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.InsightCard
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.core.ui.components.MetricMonthRingGrid
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.metricWeekdayLabels
import com.burak.healthapp.core.ui.components.weekDayShortLabel
import com.burak.healthapp.core.ui.format.formatMetricCount
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthSuccess
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.detail.DetailSkeletonContent
import com.burak.healthapp.feature.detail.buildMonthGridDays
import com.burak.healthapp.feature.detail.buildPeriodDays
import com.burak.healthapp.feature.today.SmokingStatus
import com.burak.healthapp.feature.today.smokingStatusForCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

private val smokingHistoryDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "d MMMM yyyy",
    Locale.forLanguageTag("tr"),
)

private val smokingMonthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "d MMMM",
    Locale.forLanguageTag("tr"),
)

@Immutable
data class SmokingDayBarState(
    val date: LocalDate,
    val count: Int,
    val progress: Float,
    val status: SmokingStatus,
)

@Immutable
data class SmokingHistoryItemState(
    val date: LocalDate,
    val dateLabel: String,
    val count: Int,
)

@Immutable
data class SmokingDetailUiState(
    val selectedPeriod: TrendsPeriod = TrendsPeriod.WEEKLY,
    val bars: List<SmokingDayBarState> = emptyList(),
    val monthDays: List<MetricDayRingState> = emptyList(),
    val averageCount: Int = 0,
    val totalCount: Int = 0,
    val limit: Int = 0,
    val loggedDays: Int = 0,
    val entries: List<SmokingHistoryItemState> = emptyList(),
    val hasPeriodData: Boolean = false,
    val isLoading: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SmokingDetailViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = combine(selectedDate, selectedPeriod) { date, period -> date to period }
        .flatMapLatest { (date, period) ->
            val periodDays = buildPeriodDays(date, period)
            val startDate = periodDays.firstOrNull() ?: date
            val endDate = periodDays.lastOrNull() ?: date
            combine(
                settingsRepository.settings,
                dashboardRepository.observeSmokingBetween(startDate, endDate),
            ) { settings, entries ->
                PerformanceLogger.measure("SmokingDetail:state_build") {
                    buildSmokingDetailUiState(
                        selectedDate = date,
                        selectedPeriod = period,
                        entries = entries,
                        limit = settings.goalSettings.smokeDailyLimit,
                        periodDays = periodDays,
                    )
                }
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SmokingDetailUiState(isLoading = true),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch {
            dashboardRepository.deleteSmokingForDate(date)
        }
    }
}

@Composable
fun SmokingDetailRoute(
    selectedDate: LocalDate,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    DebugRoutePerformanceTrace("SmokingDetailRoute")
    val viewModel: SmokingDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    SmokingDetailContent(
        state = uiState,
        onSelectPeriod = viewModel::selectPeriod,
        onDelete = viewModel::deleteEntry,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun SmokingDetailContent(
    state: SmokingDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
    onDelete: (LocalDate) -> Unit,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    if (state.isLoading) {
        DetailSkeletonContent()
        return
    }

    if (!windowSizeClass.isCompact) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(HealthSpacing.sm)
                .testTag("smoking_detail_screen")
                .testTag("smoking_detail_adaptive_two_pane"),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                SmokingPeriodSelector(state.selectedPeriod, onSelectPeriod)
                SmokingChartCard(state)
                SmokingSummaryCards(state, compact = false)
            }
            SmokingEntryList(
                entries = state.entries,
                onDelete = onDelete,
                modifier = Modifier.weight(1f),
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("smoking_detail_screen"),
        contentPadding = PaddingValues(HealthSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item { SmokingPeriodSelector(state.selectedPeriod, onSelectPeriod) }
        item { SmokingChartCard(state) }
        item { SmokingSummaryCards(state, compact = true) }
        item {
            SmokingEntryList(
                entries = state.entries,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun SmokingPeriodSelector(
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
private fun SmokingChartCard(state: SmokingDetailUiState) {
    HealthCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                if (state.selectedPeriod == TrendsPeriod.WEEKLY) {
                    R.string.smoking_detail_weekly_chart
                } else {
                    R.string.smoking_detail_monthly_chart
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!state.hasPeriodData) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.sm),
                text = stringResource(R.string.smoking_detail_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.selectedPeriod == TrendsPeriod.MONTHLY) {
            MetricMonthRingGrid(
                days = state.monthDays,
                weekdayLabels = metricWeekdayLabels(),
                modifier = Modifier.padding(top = HealthSpacing.sm),
                testTag = "smoking_month_ring_grid",
                activeColor = HealthCarbs,
                targetMetColor = MaterialTheme.colorScheme.error,
            )
        } else {
            SmokingWeekBarChart(
                bars = state.bars,
                modifier = Modifier.padding(top = HealthSpacing.sm),
            )
        }
    }
}

@Composable
private fun SmokingWeekBarChart(
    bars: List<SmokingDayBarState>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp, max = 220.dp),
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
                    text = if (bar.count == 0) "--" else formatMetricCount(bar.count),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
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
                            .fillMaxHeight(bar.progress.coerceIn(0f, 1f))
                            .background(
                                color = bar.status.toSmokingDetailColor(),
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Text(
                    text = weekDayShortLabel(bar.date),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun SmokingSummaryCards(
    state: SmokingDetailUiState,
    compact: Boolean,
) {
    val first: @Composable () -> Unit = {
        InsightCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.smoking_detail_average_count),
            value = stringResource(R.string.format_count, state.averageCount),
            subtitle = stringResource(R.string.common_average_selected_period),
        )
    }
    val second: @Composable () -> Unit = {
        InsightCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.smoking_detail_total_count),
            value = stringResource(R.string.format_count, state.totalCount),
            subtitle = stringResource(R.string.smoking_detail_limit, state.limit),
        )
    }
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
            first()
            second()
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
            Column(modifier = Modifier.weight(1f)) { first() }
            Column(modifier = Modifier.weight(1f)) { second() }
        }
    }
}

@Composable
private fun SmokingEntryList(
    entries: List<SmokingHistoryItemState>,
    onDelete: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.testTag("smoking_detail_entry_list"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.smoking_detail_entries),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (entries.isEmpty()) {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.smoking_detail_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                EmptyGhostChart(modifier = Modifier.padding(top = HealthSpacing.sm))
            }
        } else {
            entries.forEach { entry ->
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs)) {
                            Text(
                                text = entry.dateLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.format_count, entry.count),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        CardHeaderDestructiveButton(
                            label = stringResource(R.string.common_delete),
                            modifier = Modifier.testTag("smoking_entry_delete_${entry.date}"),
                            contentDescription = stringResource(R.string.content_description_delete_smoking_entry),
                            onClick = { onDelete(entry.date) },
                        )
                    }
                }
            }
        }
    }
}

internal fun buildSmokingDetailUiState(
    selectedDate: LocalDate,
    selectedPeriod: TrendsPeriod,
    entries: List<SmokingEntry>,
    limit: Int,
    periodDays: List<LocalDate> = buildPeriodDays(selectedDate, selectedPeriod),
): SmokingDetailUiState {
    val days = periodDays
    val entriesByDate = entries.groupBy(SmokingEntry::date)
    val safeLimit = limit.coerceAtLeast(1)
    val totals = days.map { day -> day to entriesByDate[day].orEmpty().sumOf(SmokingEntry::count) }
    val totalCount = totals.sumOf { (_, count) -> count }
    val loggedTotals = totals.map { (_, count) -> count }.filter { count -> count > 0 }

    return SmokingDetailUiState(
        selectedPeriod = selectedPeriod,
        bars = totals.map { (day, count) ->
            SmokingDayBarState(
                date = day,
                count = count,
                progress = clampProgress(count.toFloat(), safeLimit.toFloat()),
                status = smokingStatusForCount(count, limit),
            )
        },
        monthDays = if (selectedPeriod == TrendsPeriod.MONTHLY) {
            buildSmokingMonthRingDays(selectedDate, entriesByDate, safeLimit)
        } else {
            emptyList()
        },
        averageCount = if (days.isEmpty()) 0 else totalCount / days.size,
        totalCount = totalCount,
        limit = limit,
        loggedDays = loggedTotals.size,
        entries = entries
            .filter { entry -> entry.date in days }
            .sortedByDescending(SmokingEntry::date)
            .map { entry ->
                SmokingHistoryItemState(
                    date = entry.date,
                    dateLabel = entry.date.format(smokingHistoryDateFormatter),
                    count = entry.count,
                )
            },
        hasPeriodData = totals.any { (_, count) -> count > 0 },
    )
}

private fun buildSmokingMonthRingDays(
    anchorDate: LocalDate,
    entriesByDate: Map<LocalDate, List<SmokingEntry>>,
    limit: Int,
): List<MetricDayRingState> {
    val today = LocalDate.now()

    return buildMonthGridDays(anchorDate).map { date ->
        val isInCurrentMonth = date.month == anchorDate.month && date.year == anchorDate.year
        val count = if (isInCurrentMonth) entriesByDate[date].orEmpty().sumOf(SmokingEntry::count) else 0
        MetricDayRingState(
            dayLabel = date.dayOfMonth.toString(),
            progress = clampProgress(count.toFloat(), limit.toFloat()),
            hasData = count > 0,
            isInCurrentMonth = isInCurrentMonth,
            isTargetMet = false,
            isOverLimit = limit > 0 && count > limit,
            dateLabel = date.format(smokingMonthDateFormatter),
            valueLabel = "${formatMetricCount(count)} adet",
            isToday = date == today,
        )
    }
}

@Composable
private fun SmokingStatus.toSmokingDetailColor() = when (this) {
    SmokingStatus.PASSIVE -> MaterialTheme.colorScheme.onSurfaceVariant
    SmokingStatus.SAFE -> HealthSuccess
    SmokingStatus.NEUTRAL -> HealthPrimary
    SmokingStatus.WARNING -> HealthCarbs
    SmokingStatus.DANGER -> MaterialTheme.colorScheme.error
}
