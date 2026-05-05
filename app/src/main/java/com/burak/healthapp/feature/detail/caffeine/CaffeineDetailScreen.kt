package com.burak.healthapp.feature.detail.caffeine

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
import com.burak.healthapp.core.ui.format.formatWholeNumber
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.calculation.formatHourMinute
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.detail.DetailSkeletonContent
import com.burak.healthapp.feature.detail.buildMonthGridDays
import com.burak.healthapp.feature.detail.buildPeriodDays
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

private val caffeineMonthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "d MMMM",
    Locale.forLanguageTag("tr"),
)

@Immutable
data class CaffeineBarState(
    val label: String,
    val totalMg: Int,
    val progress: Float,
    val date: LocalDate? = null,
    val isOverLimit: Boolean = false,
)

@Immutable
data class CaffeineDetailUiState(
    val selectedPeriod: TrendsPeriod = TrendsPeriod.WEEKLY,
    val entries: List<CaffeineEntry> = emptyList(),
    val bars: List<CaffeineBarState> = emptyList(),
    val monthDays: List<MetricDayRingState> = emptyList(),
    val totalTodayMg: Int = 0,
    val periodAverageMg: Int = 0,
    val lastTimeLabel: String = "--",
    val limitMg: Int = 300,
    val hasPeriodData: Boolean = false,
    val isLoading: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CaffeineDetailViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = combine(selectedDate, selectedPeriod) { date, period -> date to period }
        .flatMapLatest { (date, period) ->
            val days = buildPeriodDays(date, period)
            val startDate = days.firstOrNull() ?: date
            val endDate = days.lastOrNull() ?: date
            combine(
                settingsRepository.settings,
                dashboardRepository.observeCaffeineBetween(startDate, endDate),
            ) { settings, entries ->
                PerformanceLogger.measure("CaffeineDetail:state_build") {
                    val entriesByDate = entries.groupBy(CaffeineEntry::date)
                    val todayEntries = entriesByDate[date].orEmpty()
                    val limit = settings.goalSettings.dailyCaffeineLimitMg
                    val periodTotals = days.map { day -> day to entriesByDate[day].orEmpty().sumOf(CaffeineEntry::estimatedMg) }
                    val periodTotalMg = periodTotals.sumOf { (_, total) -> total }
                    CaffeineDetailUiState(
                        selectedPeriod = period,
                        entries = todayEntries.sortedWith(compareBy(CaffeineEntry::time).thenBy(CaffeineEntry::createdAt)),
                        bars = periodTotals.map { (day, total) ->
                            CaffeineBarState(
                                label = day.dayOfMonth.toString(),
                                totalMg = total,
                                progress = clampProgress(total.toFloat(), limit.toFloat()),
                                date = day,
                                isOverLimit = total > limit,
                            )
                        },
                        monthDays = if (period == TrendsPeriod.MONTHLY) {
                            buildCaffeineMonthRingDays(
                                anchorDate = date,
                                entriesByDate = entriesByDate,
                                dailyLimitMg = limit,
                            )
                        } else {
                            emptyList()
                        },
                        totalTodayMg = todayEntries.sumOf(CaffeineEntry::estimatedMg),
                        periodAverageMg = if (days.isEmpty()) 0 else periodTotalMg / days.size,
                        lastTimeLabel = todayEntries.maxByOrNull(CaffeineEntry::time)?.time?.let(::formatHourMinute) ?: "--",
                        limitMg = limit,
                        hasPeriodData = periodTotals.any { (_, total) -> total > 0 },
                    )
                }
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CaffeineDetailUiState(isLoading = true),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            dashboardRepository.deleteCaffeine(id)
        }
    }
}

@Composable
fun CaffeineDetailRoute(
    selectedDate: LocalDate,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    DebugRoutePerformanceTrace("CaffeineDetailRoute")
    val viewModel: CaffeineDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    CaffeineDetailContent(
        state = uiState,
        onSelectPeriod = viewModel::selectPeriod,
        onDelete = viewModel::deleteEntry,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun CaffeineDetailContent(
    state: CaffeineDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
    onDelete: (Long) -> Unit,
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
                .testTag("caffeine_detail_screen")
                .testTag("caffeine_detail_adaptive_two_pane"),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                CaffeinePeriodSelector(
                    selectedPeriod = state.selectedPeriod,
                    onSelectPeriod = onSelectPeriod,
                )
                CaffeinePeriodCard(state = state)
                CaffeineTodayTotalCard(state = state)
                CaffeineMetricRow(state = state)
            }
            CaffeineEntryList(
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
            .testTag("caffeine_detail_screen"),
        contentPadding = PaddingValues(HealthSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            CaffeinePeriodSelector(
                selectedPeriod = state.selectedPeriod,
                onSelectPeriod = onSelectPeriod,
            )
        }
        item {
            CaffeinePeriodCard(state = state)
        }
        item {
            CaffeineTodayTotalCard(state = state)
        }
        item {
            CaffeineMetricRow(state = state)
        }
        item {
            CaffeineEntryList(
                entries = state.entries,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun CaffeinePeriodSelector(
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
private fun CaffeinePeriodCard(state: CaffeineDetailUiState) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("caffeine_detail_period_card"),
    ) {
        Text(
            text = stringResource(
                if (state.selectedPeriod == TrendsPeriod.WEEKLY) {
                    R.string.caffeine_detail_weekly_chart
                } else {
                    R.string.caffeine_detail_monthly_chart
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!state.hasPeriodData) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.sm),
                text = stringResource(R.string.caffeine_detail_no_period_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.selectedPeriod == TrendsPeriod.MONTHLY) {
            MetricMonthRingGrid(
                days = state.monthDays,
                weekdayLabels = metricWeekdayLabels(),
                modifier = Modifier.padding(top = HealthSpacing.sm),
                testTag = "caffeine_month_ring_grid",
                activeColor = HealthPrimary,
            )
        } else {
            CaffeineWeekChart(
                bars = state.bars,
                modifier = Modifier
                    .padding(top = HealthSpacing.sm)
                    .testTag("caffeine_week_bar_chart"),
            )
        }
    }
}

@Composable
private fun CaffeineTodayTotalCard(state: CaffeineDetailUiState) {
    InsightCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.caffeine_detail_today_total),
        value = stringResource(
            R.string.caffeine_today_total_formatted_format,
            formatWholeNumber(state.totalTodayMg),
            formatWholeNumber(state.limitMg),
        ),
        subtitle = stringResource(
            if (state.totalTodayMg > state.limitMg) {
                R.string.caffeine_limit_over
            } else {
                R.string.caffeine_estimate_notice
            },
        ),
    )
}

@Composable
private fun CaffeineMetricRow(state: CaffeineDetailUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
        InsightCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.caffeine_detail_weekly_average),
            value = stringResource(
                R.string.caffeine_today_total_formatted_format,
                formatWholeNumber(state.periodAverageMg),
                formatWholeNumber(state.limitMg),
            ),
            subtitle = stringResource(R.string.common_average_selected_period),
        )
        InsightCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.caffeine_detail_last_time),
            value = state.lastTimeLabel,
            subtitle = stringResource(R.string.caffeine_detail_entries),
        )
    }
}

@Composable
private fun CaffeineEntryList(
    entries: List<CaffeineEntry>,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.testTag("caffeine_detail_entry_list"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.caffeine_detail_entries),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (entries.isEmpty()) {
            Text(
                text = stringResource(R.string.caffeine_detail_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            EmptyGhostChart(modifier = Modifier.padding(top = HealthSpacing.sm))
        } else {
            entries.forEach { entry ->
                HealthCard(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = entry.customName ?: entry.drinkType.detailLabel(),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(
                                    R.string.caffeine_detail_record_format,
                                    formatHourMinute(entry.time),
                                    formatWholeNumber(entry.estimatedMg),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        CardHeaderDestructiveButton(
                            label = stringResource(R.string.common_delete),
                            modifier = Modifier.testTag("caffeine_entry_delete_${entry.id}"),
                            contentDescription = stringResource(R.string.content_description_delete_caffeine_entry),
                            onClick = { onDelete(entry.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaffeineWeekChart(
    bars: List<CaffeineBarState>,
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
            val label = bar.date?.let { weekDayShortLabel(it) } ?: bar.label
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.xs),
            ) {
                Text(
                    text = if (bar.totalMg == 0) {
                        "--"
                    } else {
                        stringResource(R.string.format_mg_count, formatWholeNumber(bar.totalMg))
                    },
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
                            .fillMaxHeight(bar.progress)
                            .background(
                                color = if (bar.isOverLimit) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    HealthPrimary
                                },
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun CaffeineDrinkType.detailLabel(): String = when (this) {
    CaffeineDrinkType.TURKISH_COFFEE -> stringResource(R.string.caffeine_type_turkish_coffee)
    CaffeineDrinkType.FILTER_COFFEE -> stringResource(R.string.caffeine_type_filter_coffee)
    CaffeineDrinkType.ESPRESSO -> stringResource(R.string.caffeine_type_espresso)
    CaffeineDrinkType.AMERICANO -> stringResource(R.string.caffeine_type_americano)
    CaffeineDrinkType.LATTE -> stringResource(R.string.caffeine_type_latte)
    CaffeineDrinkType.CAPPUCCINO -> stringResource(R.string.caffeine_type_cappuccino)
    CaffeineDrinkType.BLACK_TEA -> stringResource(R.string.caffeine_type_black_tea)
    CaffeineDrinkType.GREEN_TEA -> stringResource(R.string.caffeine_type_green_tea)
    CaffeineDrinkType.ENERGY_DRINK -> stringResource(R.string.caffeine_type_energy_drink)
    CaffeineDrinkType.COLA -> stringResource(R.string.caffeine_type_cola)
    CaffeineDrinkType.OTHER -> stringResource(R.string.caffeine_type_other)
}

internal fun buildCaffeineMonthRingDays(
    anchorDate: LocalDate,
    entriesByDate: Map<LocalDate, List<CaffeineEntry>>,
    dailyLimitMg: Int,
): List<MetricDayRingState> {
    val target = dailyLimitMg.toFloat().coerceAtLeast(1f)
    val today = LocalDate.now()

    return buildMonthGridDays(anchorDate).map { date ->
        val isInCurrentMonth = date.month == anchorDate.month && date.year == anchorDate.year
        val amount = if (isInCurrentMonth) entriesByDate[date].orEmpty().sumOf(CaffeineEntry::estimatedMg) else 0
        val progress = clampProgress(amount.toFloat(), target)
        MetricDayRingState(
            dayLabel = date.dayOfMonth.toString(),
            progress = progress,
            hasData = amount > 0,
            isInCurrentMonth = isInCurrentMonth,
            isTargetMet = false,
            isOverLimit = amount > dailyLimitMg,
            dateLabel = date.format(caffeineMonthDateFormatter),
            valueLabel = "${formatWholeNumber(amount)} mg",
            isToday = date == today,
        )
    }
}
