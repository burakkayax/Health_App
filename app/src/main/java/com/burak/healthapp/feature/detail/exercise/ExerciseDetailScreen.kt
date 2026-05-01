package com.burak.healthapp.feature.detail.exercise

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
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.detail.buildMonthGridDays
import com.burak.healthapp.feature.detail.buildPeriodDays
import com.burak.healthapp.feature.today.labelResId
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

private val exerciseHistoryDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "d MMMM yyyy",
    Locale.forLanguageTag("tr"),
)

private val exerciseMonthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "d MMMM",
    Locale.forLanguageTag("tr"),
)

@Immutable
data class ExerciseDayBarState(
    val date: LocalDate,
    val durationMinutes: Int,
    val progress: Float,
)

@Immutable
data class ExerciseHistoryItemState(
    val date: LocalDate,
    val dateLabel: String,
    val type: ExerciseType,
    val intensity: ExerciseIntensity,
    val durationMinutes: Int,
)

@Immutable
data class ExerciseDetailUiState(
    val selectedPeriod: TrendsPeriod = TrendsPeriod.WEEKLY,
    val bars: List<ExerciseDayBarState> = emptyList(),
    val monthDays: List<MetricDayRingState> = emptyList(),
    val averageDurationMinutes: Int = 0,
    val totalDurationMinutes: Int = 0,
    val activeDays: Int = 0,
    val entries: List<ExerciseHistoryItemState> = emptyList(),
    val hasPeriodData: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = combine(selectedDate, selectedPeriod) { date, period -> date to period }
        .flatMapLatest { (date, period) ->
            val periodDays = buildPeriodDays(date, period)
            val startDate = periodDays.firstOrNull() ?: date
            combine(
                settingsRepository.settings,
                dashboardRepository.observeExerciseBetween(startDate, date),
            ) { settings, entries ->
                PerformanceLogger.measure("ExerciseDetail:state_build") {
                    buildExerciseDetailUiState(
                        selectedDate = date,
                        selectedPeriod = period,
                        entries = entries,
                        dailyTargetMinutes = settings.goalSettings.exerciseTargetDurationMinutes,
                        periodDays = periodDays,
                    )
                }
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ExerciseDetailUiState(),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch {
            dashboardRepository.deleteExerciseForDate(date)
        }
    }
}

@Composable
fun ExerciseDetailRoute(
    selectedDate: LocalDate,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    DebugRoutePerformanceTrace("ExerciseDetailRoute")
    val viewModel: ExerciseDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    ExerciseDetailContent(
        state = uiState,
        onSelectPeriod = viewModel::selectPeriod,
        onDelete = viewModel::deleteEntry,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun ExerciseDetailContent(
    state: ExerciseDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
    onDelete: (LocalDate) -> Unit,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    if (!windowSizeClass.isCompact) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(HealthSpacing.sm)
                .testTag("exercise_detail_screen")
                .testTag("exercise_detail_adaptive_two_pane"),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                ExercisePeriodSelector(state.selectedPeriod, onSelectPeriod)
                ExerciseChartCard(state)
                ExerciseSummaryCards(state, compact = false)
            }
            ExerciseEntryList(
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
            .testTag("exercise_detail_screen"),
        contentPadding = PaddingValues(HealthSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item { ExercisePeriodSelector(state.selectedPeriod, onSelectPeriod) }
        item { ExerciseChartCard(state) }
        item { ExerciseSummaryCards(state, compact = true) }
        item {
            ExerciseEntryList(
                entries = state.entries,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun ExercisePeriodSelector(
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
private fun ExerciseChartCard(state: ExerciseDetailUiState) {
    HealthCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(
                if (state.selectedPeriod == TrendsPeriod.WEEKLY) {
                    R.string.exercise_detail_weekly_chart
                } else {
                    R.string.exercise_detail_monthly_chart
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (!state.hasPeriodData) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.sm),
                text = stringResource(R.string.exercise_detail_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (state.selectedPeriod == TrendsPeriod.MONTHLY) {
            MetricMonthRingGrid(
                days = state.monthDays,
                weekdayLabels = metricWeekdayLabels(),
                modifier = Modifier.padding(top = HealthSpacing.sm),
                testTag = "exercise_month_ring_grid",
                activeColor = HealthPrimary,
            )
        } else {
            ExerciseWeekBarChart(
                bars = state.bars,
                modifier = Modifier.padding(top = HealthSpacing.sm),
            )
        }
    }
}

@Composable
private fun ExerciseWeekBarChart(
    bars: List<ExerciseDayBarState>,
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
                    text = if (bar.durationMinutes == 0) {
                        "--"
                    } else {
                        stringResource(
                            R.string.exercise_detail_duration_formatted,
                            formatWholeNumber(bar.durationMinutes),
                        )
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
                            .fillMaxHeight(bar.progress.coerceIn(0f, 1f))
                            .background(
                                color = HealthPrimary,
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
private fun ExerciseSummaryCards(
    state: ExerciseDetailUiState,
    compact: Boolean,
) {
    val average: @Composable () -> Unit = {
        InsightCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.exercise_detail_average_duration),
            value = stringResource(
                R.string.exercise_detail_duration_formatted,
                formatWholeNumber(state.averageDurationMinutes),
            ),
            subtitle = stringResource(R.string.common_average_selected_period),
        )
    }
    val total: @Composable () -> Unit = {
        InsightCard(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(R.string.exercise_detail_total_duration),
            value = stringResource(
                R.string.exercise_detail_duration_formatted,
                formatWholeNumber(state.totalDurationMinutes),
            ),
            subtitle = stringResource(R.string.exercise_detail_active_days, state.activeDays),
        )
    }
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
            average()
            total()
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
            Column(modifier = Modifier.weight(1f)) { average() }
            Column(modifier = Modifier.weight(1f)) { total() }
        }
    }
}

@Composable
private fun ExerciseEntryList(
    entries: List<ExerciseHistoryItemState>,
    onDelete: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.testTag("exercise_detail_entry_list"),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        Text(
            text = stringResource(R.string.exercise_detail_entries),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (entries.isEmpty()) {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.exercise_detail_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            entries.forEach { entry ->
                HealthCard(modifier = Modifier.fillMaxWidth()) {
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
                                text = entry.dateLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(entry.type.labelResId),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(
                                    R.string.exercise_detail_entry_format,
                                    formatWholeNumber(entry.durationMinutes),
                                    stringResource(entry.intensity.labelResId),
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        CardHeaderDestructiveButton(
                            label = stringResource(R.string.common_delete),
                            modifier = Modifier.testTag("exercise_entry_delete_${entry.date}"),
                            contentDescription = stringResource(R.string.content_description_delete_exercise_entry),
                            onClick = { onDelete(entry.date) },
                        )
                    }
                }
            }
        }
    }
}

internal fun buildExerciseDetailUiState(
    selectedDate: LocalDate,
    selectedPeriod: TrendsPeriod,
    entries: List<ExerciseEntry>,
    dailyTargetMinutes: Int,
    periodDays: List<LocalDate> = buildPeriodDays(selectedDate, selectedPeriod),
): ExerciseDetailUiState {
    val startDate = periodDays.firstOrNull() ?: selectedDate
    val days = periodDays
    val entriesByDate = entries.groupBy(ExerciseEntry::date)
    val target = dailyTargetMinutes.coerceAtLeast(1)
    val totals = days.map { day -> day to entriesByDate[day].orEmpty().sumOf(ExerciseEntry::durationMinutes) }
    val totalDuration = totals.sumOf { (_, duration) -> duration }
    val activeDays = totals.count { (_, duration) -> duration > 0 }

    return ExerciseDetailUiState(
        selectedPeriod = selectedPeriod,
        bars = totals.map { (day, duration) ->
            ExerciseDayBarState(
                date = day,
                durationMinutes = duration,
                progress = clampProgress(duration.toFloat(), target.toFloat()),
            )
        },
        monthDays = if (selectedPeriod == TrendsPeriod.MONTHLY) {
            buildExerciseMonthRingDays(selectedDate, entriesByDate, target)
        } else {
            emptyList()
        },
        averageDurationMinutes = if (days.isEmpty()) 0 else totalDuration / days.size,
        totalDurationMinutes = totalDuration,
        activeDays = activeDays,
        entries = entries
            .filter { entry -> entry.date in startDate..selectedDate }
            .sortedByDescending(ExerciseEntry::date)
            .map { entry ->
                ExerciseHistoryItemState(
                    date = entry.date,
                    dateLabel = entry.date.format(exerciseHistoryDateFormatter),
                    type = entry.type,
                    intensity = entry.intensity,
                    durationMinutes = entry.durationMinutes,
                )
            },
        hasPeriodData = totals.any { (_, duration) -> duration > 0 },
    )
}

private fun buildExerciseMonthRingDays(
    anchorDate: LocalDate,
    entriesByDate: Map<LocalDate, List<ExerciseEntry>>,
    targetMinutes: Int,
): List<MetricDayRingState> {
    val today = LocalDate.now()

    return buildMonthGridDays(anchorDate).map { date ->
        val isInCurrentMonth = date.month == anchorDate.month && date.year == anchorDate.year
        val duration = if (isInCurrentMonth) entriesByDate[date].orEmpty().sumOf(ExerciseEntry::durationMinutes) else 0
        val progress = clampProgress(duration.toFloat(), targetMinutes.toFloat())
        MetricDayRingState(
            dayLabel = date.dayOfMonth.toString(),
            progress = progress,
            hasData = duration > 0,
            isInCurrentMonth = isInCurrentMonth,
            isTargetMet = duration > 0 && progress >= 1f,
            dateLabel = date.format(exerciseMonthDateFormatter),
            valueLabel = "${formatWholeNumber(duration)} dk",
            isToday = date == today,
        )
    }
}
