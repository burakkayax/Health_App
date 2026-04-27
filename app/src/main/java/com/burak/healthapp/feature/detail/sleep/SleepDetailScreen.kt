package com.burak.healthapp.feature.detail.sleep

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.calculation.buildSleepFeedback
import com.burak.healthapp.domain.calculation.calculateSleepDurationMinutes
import com.burak.healthapp.domain.calculation.calculateSleepRegularityStandardDeviation
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.calculation.formatLocalTime
import com.burak.healthapp.domain.calculation.formatMinutesAsSleepLabel
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.core.ui.components.MetricMonthRingGrid
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.metricWeekdayLabels
import com.burak.healthapp.feature.detail.sleep.SleepBarState
import com.burak.healthapp.feature.detail.sleep.SleepCalendarDayState
import com.burak.healthapp.feature.detail.sleep.SleepCalendarWeekState
import com.burak.healthapp.feature.detail.sleep.SleepDetailUiState
import com.burak.healthapp.feature.detail.sleep.SleepRegularityState
import com.burak.healthapp.feature.detail.sleep.SleepRegularityStatus
import com.burak.healthapp.feature.root.healthApplication
import com.burak.healthapp.core.ui.theme.HealthCarbs
import com.burak.healthapp.core.ui.theme.HealthSleep
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.core.ui.theme.HealthSuccess
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class SleepDetailViewModel(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = combine(selectedDate, selectedPeriod) { date, period -> date to period }
        .flatMapLatest { (date, period) ->
            val startDate = if (period == TrendsPeriod.MONTHLY) {
                date.withDayOfMonth(1)
            } else {
                date.minusDays(29)
            }
            combine(
                settingsRepository.settings,
                dashboardRepository.observeSleepSessionsBetween(
                    startDate = startDate,
                    endDate = date,
                ),
            ) { settings, sessions ->
                sessions.toSleepDetailUiState(
                    anchorDate = date,
                    period = period,
                    goals = settings.goalSettings,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySleepDetailUiState(),
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
                SleepDetailViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                    dashboardRepository = healthApplication().container.dashboardRepository,
                )
            }
        }
    }
}

@Composable
fun SleepDetailRoute(
    selectedDate: LocalDate,
) {
    val viewModel: SleepDetailViewModel = viewModel(factory = SleepDetailViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    SleepDetailContent(
        state = uiState,
        onSelectPeriod = viewModel::selectPeriod,
    )
}

@Composable
fun SleepDetailContent(
    state: SleepDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("sleep_detail_screen"),
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
                    .testTag("sleep_detail_chart_card"),
            ) {
                Text(
                    text = "Uyku İstikrarı",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = "Hedef ${state.targetLabel} • ${state.bedtimeLabel} - ${state.wakeLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!state.hasData) {
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        text = "Henüz bu pencere için uyku kaydı yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.selectedPeriod == TrendsPeriod.MONTHLY) {
                    SleepMonthlyCalendar(
                        weeks = state.calendarWeeks,
                        modifier = Modifier
                            .padding(top = HealthSpacing.sm)
                            .testTag("sleep_month_calendar"),
                    )
                } else if (state.hasData) {
                    SleepConsistencyChart(
                        bars = state.bars,
                        modifier = Modifier
                            .padding(top = HealthSpacing.sm)
                            .testTag("sleep_detail_chart"),
                    )
                }
            }
        }
        item {
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sleep_regularity_card"),
            ) {
                Text(
                    text = state.regularity.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = regularityColor(state.regularity.status),
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = state.regularity.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.regularity.progress != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = HealthSpacing.sm)
                            .height(10.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(999.dp),
                            ),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(state.regularity.progress)
                                .height(10.dp)
                                .background(
                                    color = regularityColor(state.regularity.status),
                                    shape = RoundedCornerShape(999.dp),
                                ),
                        )
                    }
                }
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.sm),
                    text = state.regularity.helperLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SleepMonthlyCalendar(
    weeks: List<SleepCalendarWeekState>,
    modifier: Modifier = Modifier,
) {
    MetricMonthRingGrid(
        days = weeks.flatMap { week ->
            week.days.map { day ->
                MetricDayRingState(
                    dayLabel = day.dayLabel,
                    progress = day.progress,
                    hasData = day.hasData,
                    isInCurrentMonth = day.isInCurrentMonth,
                    isTargetMet = day.isTargetMet,
                    dateLabel = day.dateLabel,
                    valueLabel = day.durationLabel,
                    isToday = day.isToday,
                )
            }
        },
        weekdayLabels = metricWeekdayLabels(),
        modifier = modifier,
        testTag = "sleep_month_calendar",
        activeColor = HealthSleep,
    )
}
@Composable
private fun SleepCalendarDayCell(
    day: SleepCalendarDayState,
    modifier: Modifier = Modifier,
) {
    val activeColor = when {
        !day.isInCurrentMonth -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        day.isTargetMet -> HealthSuccess
        day.hasData -> HealthSleep
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }
    val textColor = if (day.isInCurrentMonth) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    }
    val trackColor = if (day.isInCurrentMonth) {
        activeColor.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .testTag("sleep_month_day_${day.dayLabel}"),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(36.dp)) {
            val strokeWidth = 3.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            if (day.hasData && day.isInCurrentMonth) {
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * day.progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            text = day.dayLabel,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SleepConsistencyChart(
    bars: List<SleepBarState>,
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
                    text = bar.durationLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .widthIn(min = 10.dp)
                        .weight(1f)
                        .height(160.dp)
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
                            color = HealthSleep,
                                shape = RoundedCornerShape(999.dp),
                            ),
                    )
                }
                Text(
                    text = bar.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun List<SleepSession>.toSleepDetailUiState(
    anchorDate: LocalDate,
    period: TrendsPeriod,
    goals: GoalSettings,
): SleepDetailUiState {
    val sessionsByDate = groupBy(SleepSession::sessionDate)
        .mapValues { (_, entries) -> entries.maxByOrNull(SleepSession::endTime) }

    val weeklyDays = (6L downTo 0L).map(anchorDate::minusDays)
    val monthlyDays = (29L downTo 0L).map(anchorDate::minusDays)
    val displayDays = if (period == TrendsPeriod.WEEKLY) weeklyDays else monthlyDays
    val targetMinutes = goals.sleepTargetMinutes.toFloat().coerceAtLeast(1f)
    val calendarWeeks = buildSleepCalendarWeeks(
        anchorDate = anchorDate,
        sessionsByDate = sessionsByDate,
        targetMinutes = targetMinutes,
    )

    val bars = displayDays.map { date ->
        val duration = calculateSleepDurationMinutes(sessionsByDate[date])
        SleepBarState(
            label = if (period == TrendsPeriod.WEEKLY) {
                date.toWeekLabel()
            } else {
                date.dayOfMonth.toString()
            },
            progress = clampProgress(duration.toFloat(), targetMinutes),
            durationLabel = if (duration == 0) "--" else formatMinutesAsSleepLabel(duration),
        )
    }

    val regularitySessions = weeklyDays.mapNotNull { sessionsByDate[it] }
    val standardDeviation = calculateSleepRegularityStandardDeviation(regularitySessions)
    val selectedSession = sessionsByDate[anchorDate]

    return SleepDetailUiState(
        selectedPeriod = period,
        bars = bars,
        regularity = if (standardDeviation == null) {
            SleepRegularityState(
                title = "Uyku Düzeni",
                subtitle = "Yeterli veri yok",
                helperLabel = "Daha fazla uyku kaydı ekledikçe analiz görünür.",
                progress = null,
                status = SleepRegularityStatus.EMPTY,
                isEmpty = true,
            )
        } else {
            when {
                standardDeviation < 30f -> SleepRegularityState(
                    title = "Düzenli",
                    subtitle = "Son 7 günde uyku saatlerin oldukça stabil.",
                    helperLabel = buildSleepFeedback(selectedSession, goals),
                    progress = 1f,
                    status = SleepRegularityStatus.REGULAR,
                )

                standardDeviation <= 60f -> SleepRegularityState(
                    title = "Değişken",
                    subtitle = "Son 7 günde uyku saatlerin orta seviyede değişiyor.",
                    helperLabel = buildSleepFeedback(selectedSession, goals),
                    progress = 0.55f,
                    status = SleepRegularityStatus.VARIABLE,
                )

                else -> SleepRegularityState(
                    title = "Düzensiz",
                    subtitle = "Son 7 günde yatış veya uyanış saatlerin oldukça oynak.",
                    helperLabel = buildSleepFeedback(selectedSession, goals),
                    progress = 0.25f,
                    status = SleepRegularityStatus.IRREGULAR,
                )
            }
        },
        hasData = if (period == TrendsPeriod.MONTHLY) {
            calendarWeeks.any { week -> week.days.any { day -> day.isInCurrentMonth && day.hasData } }
        } else {
            bars.any { it.progress > 0f }
        },
        targetLabel = formatMinutesAsSleepLabel(goals.sleepTargetMinutes),
        bedtimeLabel = formatLocalTime(goals.sleepTargetBedtime),
        wakeLabel = formatLocalTime(goals.sleepTargetWakeTime),
        calendarWeeks = calendarWeeks,
    )
}

internal fun buildSleepCalendarWeeks(
    anchorDate: LocalDate,
    sessionsByDate: Map<LocalDate, SleepSession?>,
    targetMinutes: Float,
): List<SleepCalendarWeekState> {
    val monthStart = anchorDate.withDayOfMonth(1)
    val monthEnd = anchorDate.withDayOfMonth(anchorDate.lengthOfMonth())
    val gridStart = monthStart.minusDays((monthStart.dayOfWeek.value - 1).toLong())
    val gridEnd = monthEnd.plusDays((7 - monthEnd.dayOfWeek.value).toLong())
    val dayCount = java.time.temporal.ChronoUnit.DAYS.between(gridStart, gridEnd).toInt() + 1
    val dateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr"))

    return (0 until dayCount)
        .map { offset -> gridStart.plusDays(offset.toLong()) }
        .chunked(7)
        .map { days ->
            SleepCalendarWeekState(
                days = days.map { date ->
                    val isInCurrentMonth = date.month == anchorDate.month && date.year == anchorDate.year
                    val duration = if (isInCurrentMonth) {
                        calculateSleepDurationMinutes(sessionsByDate[date])
                    } else {
                        0
                    }
                    val progress = clampProgress(duration.toFloat(), targetMinutes)
                    SleepCalendarDayState(
                        dayLabel = date.dayOfMonth.toString(),
                        progress = progress,
                        hasData = duration > 0,
                        isInCurrentMonth = isInCurrentMonth,
                        isTargetMet = duration > 0 && progress >= 1f,
                        dateLabel = date.format(dateFormatter),
                        durationLabel = if (duration == 0) "--" else formatMinutesAsSleepLabel(duration),
                        isToday = date == LocalDate.now(),
                    )
                },
            )
        }
}

private fun emptySleepDetailUiState(): SleepDetailUiState {
    return SleepDetailUiState(
        selectedPeriod = TrendsPeriod.WEEKLY,
        bars = emptyList(),
        regularity = SleepRegularityState(
            title = "Uyku Düzeni",
            subtitle = "Yeterli veri yok",
            helperLabel = "Daha fazla uyku kaydı ekledikçe analiz görünür.",
            progress = null,
            status = SleepRegularityStatus.EMPTY,
            isEmpty = true,
        ),
        hasData = false,
        targetLabel = "0s 0d",
        bedtimeLabel = "23:00",
        wakeLabel = "07:00",
        calendarWeeks = emptyList(),
    )
}

@Composable
private fun regularityColor(status: SleepRegularityStatus) = when (status) {
    SleepRegularityStatus.EMPTY -> MaterialTheme.colorScheme.onSurface
    SleepRegularityStatus.REGULAR -> HealthSuccess
    SleepRegularityStatus.VARIABLE -> HealthCarbs
    SleepRegularityStatus.IRREGULAR -> MaterialTheme.colorScheme.error
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
