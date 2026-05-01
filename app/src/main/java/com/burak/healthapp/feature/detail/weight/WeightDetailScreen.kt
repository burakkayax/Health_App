package com.burak.healthapp.feature.detail.weight

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.R
import com.burak.healthapp.core.performance.DebugRoutePerformanceTrace
import com.burak.healthapp.core.performance.PerformanceLogger
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.adaptive.isCompact
import com.burak.healthapp.core.ui.components.BmiGaugeChart
import com.burak.healthapp.core.ui.components.CardHeaderDestructiveButton
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.WeightTrendChart
import com.burak.healthapp.core.ui.model.BmiGaugeState
import com.burak.healthapp.core.ui.model.buildWeightTrendChartState
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.calculation.calculateBodyMassIndex
import com.burak.healthapp.domain.calculation.classifyBodyMassIndex
import com.burak.healthapp.domain.calculation.normalizeBodyMassIndexToGauge
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.feature.detail.weight.WeightDetailUiState
import com.burak.healthapp.feature.detail.weight.WeightHistoryItemState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WeightDetailViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    val uiState = combine(
        settingsRepository.settings,
        dashboardRepository.observeWeightHistory(),
    ) { settings, measurements ->
        PerformanceLogger.measure("WeightDetail:state_build") {
            measurements.toWeightDetailUiState(
                heightCm = settings.userProfile.heightCm,
                targetWeightKg = settings.goalSettings.targetWeightKg,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeightDetailUiState(
            chartPoints = emptyList(),
            historyItems = emptyList(),
            bmiGauge = BmiGaugeState(helperMessage = "VKİ için en az bir kilo kaydı gerekli."),
        ),
    )

    fun deleteMeasurement(id: Long) {
        viewModelScope.launch {
            dashboardRepository.deleteBodyMeasurement(id)
        }
    }
}

@Composable
fun WeightDetailRoute(
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    DebugRoutePerformanceTrace("WeightDetailRoute")
    val viewModel: WeightDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeightDetailContent(
        state = uiState,
        onDeleteMeasurement = viewModel::deleteMeasurement,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun WeightDetailContent(
    state: WeightDetailUiState,
    onDeleteMeasurement: (Long) -> Unit,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
    if (!windowSizeClass.isCompact) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(HealthSpacing.sm)
                .testTag("weight_detail_screen")
                .testTag("weight_detail_adaptive_two_pane"),
            horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
            ) {
                item { WeightChartCard(state = state) }
                item { WeightBmiCard(state = state) }
            }
            WeightHistoryList(
                state = state,
                onDeleteMeasurement = onDeleteMeasurement,
                modifier = Modifier.weight(1f),
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("weight_detail_screen"),
        contentPadding = PaddingValues(
            start = HealthSpacing.sm,
            end = HealthSpacing.sm,
            top = HealthSpacing.xs,
            bottom = HealthSpacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            WeightChartCard(state = state)
        }
        item {
            WeightBmiCard(state = state)
        }
        item {
            Text(
                text = stringResource(R.string.weight_detail_history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (state.historyItems.isEmpty()) {
            item {
                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_detail_history_empty"),
                ) {
                    Text(
                        text = stringResource(R.string.weight_detail_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.historyItems, key = WeightHistoryItemState::id) { item ->
                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_history_item_${item.id}"),
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
                                text = item.dateLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = item.weightLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        CardHeaderDestructiveButton(
                            label = stringResource(R.string.common_delete),
                            contentDescription = stringResource(R.string.content_description_delete_weight),
                            modifier = Modifier.testTag("weight_history_delete_${item.id}"),
                            onClick = { onDeleteMeasurement(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightChartCard(state: WeightDetailUiState) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_detail_chart_card"),
    ) {
        Text(
            text = stringResource(R.string.weight_detail_chart_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (state.chartPoints.isEmpty()) {
            Text(
                modifier = Modifier.padding(top = HealthSpacing.sm),
                text = stringResource(R.string.weight_detail_chart_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.historyItems.firstOrNull()?.let { latestItem ->
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = stringResource(R.string.weight_detail_latest_record, latestItem.weightLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.weightChart?.let { chart ->
                WeightTrendChart(
                    state = chart,
                    startLabel = stringResource(R.string.weight_chart_start, chart.startWeightKg),
                    targetLabel = stringResource(R.string.weight_chart_target, chart.targetWeightKg),
                    currentLabel = stringResource(R.string.weight_chart_current, chart.currentWeightKg),
                    progressLabel = stringResource(
                        R.string.weight_chart_progress,
                        (chart.progress * 100).toInt(),
                    ),
                    modifier = Modifier
                        .padding(top = HealthSpacing.sm)
                        .testTag("weight_detail_chart"),
                )
            }
        }
    }
}

@Composable
private fun WeightBmiCard(state: WeightDetailUiState) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_detail_bmi_card"),
    ) {
        Text(
            text = stringResource(R.string.weight_detail_bmi_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(top = HealthSpacing.xs),
            text = stringResource(R.string.weight_detail_bmi_helper),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BmiGaugeChart(
            modifier = Modifier
                .padding(top = HealthSpacing.sm)
                .testTag("weight_detail_bmi_gauge"),
            state = state.bmiGauge,
        )
        val helperMessage = state.bmiGauge.helperMessage
        if (helperMessage != null) {
            Text(
                modifier = Modifier
                    .padding(top = HealthSpacing.xs)
                    .testTag("weight_detail_bmi_helper"),
                text = helperMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WeightHistoryList(
    state: WeightDetailUiState,
    onDeleteMeasurement: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HealthSpacing.sm),
    ) {
        item {
            Text(
                text = stringResource(R.string.weight_detail_history_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        if (state.historyItems.isEmpty()) {
            item {
                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weight_detail_history_empty"),
                ) {
                    Text(
                        text = stringResource(R.string.weight_detail_history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.historyItems, key = WeightHistoryItemState::id) { item ->
                WeightHistoryItem(
                    item = item,
                    onDeleteMeasurement = onDeleteMeasurement,
                )
            }
        }
    }
}

@Composable
private fun WeightHistoryItem(
    item: WeightHistoryItemState,
    onDeleteMeasurement: (Long) -> Unit,
) {
    HealthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("weight_history_item_${item.id}"),
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
                    text = item.dateLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = item.weightLabel,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            CardHeaderDestructiveButton(
                label = stringResource(R.string.common_delete),
                contentDescription = stringResource(R.string.content_description_delete_weight),
                modifier = Modifier.testTag("weight_history_delete_${item.id}"),
                onClick = { onDeleteMeasurement(item.id) },
            )
        }
    }
}

private fun List<BodyMeasurementEntry>.toWeightDetailUiState(
    heightCm: Float?,
    targetWeightKg: Float,
): WeightDetailUiState {
    val locale = Locale.forLanguageTag("tr")
    val historyFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    val chartFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
    val chartMeasurements = groupBy(BodyMeasurementEntry::date)
        .mapNotNull { (_, entries) -> entries.maxByOrNull(BodyMeasurementEntry::recordedAt) }
        .sortedBy(BodyMeasurementEntry::date)
    val chartPoints = chartMeasurements.map { measurement ->
        TrendPoint(
            label = measurement.date.format(chartFormatter),
            value = measurement.weightKg,
        )
    }
    val latestMeasurement = maxWithOrNull(
        compareBy<BodyMeasurementEntry> { it.date }.thenBy { it.recordedAt },
    )
    val bmi = calculateBodyMassIndex(latestMeasurement?.weightKg, heightCm)

    return WeightDetailUiState(
        chartPoints = chartPoints,
        weightChart = buildWeightTrendChartState(
            points = chartPoints,
            targetWeightKg = targetWeightKg,
        ),
        historyItems = sortedWith(
            compareByDescending<BodyMeasurementEntry> { it.date }.thenByDescending { it.recordedAt },
        ).map { measurement ->
            WeightHistoryItemState(
                id = measurement.id,
                dateLabel = measurement.date.format(historyFormatter),
                weightLabel = String.format(locale, "%.1f kg", measurement.weightKg),
            )
        },
        bmiGauge = when {
            heightCm == null -> BmiGaugeState(
                helperMessage = "VKİ için boyunu profilinden ekle.",
            )

            bmi == null -> BmiGaugeState(
                helperMessage = "VKİ için en az bir kilo kaydı gerekli.",
            )

            else -> BmiGaugeState(
                indicatorFraction = normalizeBodyMassIndexToGauge(bmi),
                valueLabel = String.format(locale, "%.1f • %s", bmi, classifyBodyMassIndex(bmi)),
            )
        },
    )
}
