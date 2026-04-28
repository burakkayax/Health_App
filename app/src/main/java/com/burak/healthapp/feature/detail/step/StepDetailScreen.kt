package com.burak.healthapp.feature.detail.step

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.InsightCard
import com.burak.healthapp.core.ui.components.MetricDayRingState
import com.burak.healthapp.core.ui.components.MetricMonthRingGrid
import com.burak.healthapp.core.ui.components.RoundedPillButton
import com.burak.healthapp.core.ui.components.SegmentedControl
import com.burak.healthapp.core.ui.components.metricWeekdayLabels
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.core.ui.text.asString
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.app.hasActivityRecognitionPermission
import com.burak.healthapp.feature.app.hasStepCounterSensor
import com.burak.healthapp.feature.detail.step.StepBarState
import com.burak.healthapp.feature.detail.step.StepDetailUiState
import com.burak.healthapp.feature.root.healthApplication
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

@OptIn(ExperimentalCoroutinesApi::class)
class StepDetailViewModel(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = combine(selectedDate, selectedPeriod) { date, period -> date to period }
        .distinctUntilChanged()
        .flatMapLatest { (date, period) ->
            val displayDays = if (period == TrendsPeriod.WEEKLY) {
                (6L downTo 0L).map(date::minusDays)
            } else {
                val monthStart = date.withDayOfMonth(1)
                (0L..java.time.temporal.ChronoUnit.DAYS.between(monthStart, date)).map(monthStart::plusDays)
            }
            combine(
                settingsRepository.settings,
                dashboardRepository.observeStepsBetween(displayDays.first(), date),
            ) { settings, entries ->
                entries.toStepDetailUiState(
                    days = displayDays,
                    anchorDate = date,
                    period = period,
                    targetSteps = settings.goalSettings.dailyStepTarget,
                    stepTrackingEnabled = settings.stepTrackingEnabled,
                )
            }
                .distinctUntilChanged()
        }
        .distinctUntilChanged()
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

    fun updateStepTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateStepTrackingEnabled(enabled)
        }
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
    val context = LocalContext.current
    val hasStepSensor = remember { context.hasStepCounterSensor() }
    var stepTrackingMessage by remember { mutableStateOf<UiText?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            stepTrackingMessage = null
            viewModel.updateStepTrackingEnabled(true)
        } else {
            stepTrackingMessage = UiText.StringResource(R.string.profile_step_tracking_permission_required)
            viewModel.updateStepTrackingEnabled(false)
        }
    }

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    StepDetailContent(
        state = uiState,
        stepTrackingMessage = stepTrackingMessage,
        onSelectPeriod = viewModel::selectPeriod,
        onEnableStepTracking = {
            when {
                !hasStepSensor -> {
                    stepTrackingMessage = UiText.StringResource(R.string.profile_step_tracking_no_sensor)
                    viewModel.updateStepTrackingEnabled(false)
                }
                context.hasActivityRecognitionPermission() -> {
                    stepTrackingMessage = null
                    viewModel.updateStepTrackingEnabled(true)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
                else -> {
                    stepTrackingMessage = null
                    viewModel.updateStepTrackingEnabled(true)
                }
            }
        },
    )
}

@Composable
fun StepDetailContent(
    state: StepDetailUiState,
    onSelectPeriod: (TrendsPeriod) -> Unit,
    stepTrackingMessage: UiText? = null,
    onEnableStepTracking: () -> Unit = {},
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
        if (!state.stepTrackingEnabled) {
            item {
                StepTrackingDisabledCard(
                    message = stepTrackingMessage,
                    onEnable = onEnableStepTracking,
                )
            }
        }
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
                }
                if (state.selectedPeriod == TrendsPeriod.MONTHLY) {
                    MetricMonthRingGrid(
                        days = state.monthDays,
                        weekdayLabels = metricWeekdayLabels(),
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        testTag = "step_month_ring_grid",
                        activeColor = HealthPrimary,
                    )
                } else if (state.hasData) {
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
private fun StepTrackingDisabledCard(
    message: UiText?,
    onEnable: () -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("step_tracking_disabled_card"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.step_tracking_disabled_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = stringResource(R.string.step_tracking_disabled_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                message?.let {
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = it.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            RoundedPillButton(
                label = stringResource(R.string.step_tracking_enable),
                modifier = Modifier.testTag("step_tracking_enable_button"),
                containerColor = HealthPrimary,
                contentColor = Color.White,
                onClick = onEnable,
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
    anchorDate: LocalDate,
    period: TrendsPeriod,
    targetSteps: Int,
    stepTrackingEnabled: Boolean,
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
        monthDays = if (period == TrendsPeriod.MONTHLY) {
            buildStepMonthRingDays(
                anchorDate = anchorDate,
                entriesByDate = entriesByDate,
                targetSteps = targetSteps,
            )
        } else {
            emptyList()
        },
        totalStepsLabel = "$totalSteps adım",
        averageStepsLabel = "$averageSteps adım",
        targetLabel = "$targetSteps adım",
        hasData = loggedEntries.any { it.steps > 0 },
        stepTrackingEnabled = stepTrackingEnabled,
    )
}

private fun emptyStepDetailUiState(): StepDetailUiState = StepDetailUiState(
    selectedPeriod = TrendsPeriod.WEEKLY,
    bars = emptyList(),
    monthDays = emptyList(),
    totalStepsLabel = "0 adım",
    averageStepsLabel = "0 adım",
    targetLabel = "${DefaultHealthGoals.DAILY_STEPS} adım",
    hasData = false,
    stepTrackingEnabled = false,
)

internal fun buildStepMonthRingDays(
    anchorDate: LocalDate,
    entriesByDate: Map<LocalDate, StepEntry>,
    targetSteps: Int,
): List<MetricDayRingState> {
    val monthStart = anchorDate.withDayOfMonth(1)
    val monthEnd = anchorDate.withDayOfMonth(anchorDate.lengthOfMonth())
    val gridStart = monthStart.minusDays((monthStart.dayOfWeek.value - 1).toLong())
    val gridEnd = monthEnd.plusDays((7 - monthEnd.dayOfWeek.value).toLong())
    val dayCount = java.time.temporal.ChronoUnit.DAYS.between(gridStart, gridEnd).toInt() + 1
    val target = targetSteps.toFloat().coerceAtLeast(1f)

    return (0 until dayCount).map { offset ->
        val date = gridStart.plusDays(offset.toLong())
        val isInCurrentMonth = date.month == anchorDate.month && date.year == anchorDate.year
        val steps = if (isInCurrentMonth) entriesByDate[date]?.steps ?: 0 else 0
        val progress = clampProgress(steps.toFloat(), target)
        MetricDayRingState(
            dayLabel = date.dayOfMonth.toString(),
            progress = progress,
            hasData = steps > 0,
            isInCurrentMonth = isInCurrentMonth,
            isTargetMet = steps > 0 && progress >= 1f,
            dateLabel = date.format(stepMonthDateFormatter),
            valueLabel = "$steps adım",
            isToday = date == LocalDate.now(),
        )
    }
}

private val stepMonthDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(
    "d MMMM",
    Locale.forLanguageTag("tr"),
)

private fun LocalDate.toWeekLabel(): String = when (dayOfWeek.value) {
    1 -> "P"
    2 -> "S"
    3 -> "Ç"
    4 -> "P"
    5 -> "C"
    6 -> "C"
    else -> "P"
}
