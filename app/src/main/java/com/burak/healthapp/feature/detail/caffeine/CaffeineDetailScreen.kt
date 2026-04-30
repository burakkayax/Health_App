package com.burak.healthapp.feature.detail.caffeine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.burak.healthapp.core.ui.adaptive.HealthWindowSizeClass
import com.burak.healthapp.core.ui.adaptive.isCompact
import com.burak.healthapp.core.ui.components.HealthCard
import com.burak.healthapp.core.ui.components.InsightCard
import com.burak.healthapp.core.ui.theme.HealthPrimary
import com.burak.healthapp.core.ui.theme.HealthSpacing
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.model.CaffeineDrinkType
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
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
import javax.inject.Inject

@Immutable
data class CaffeineBarState(
    val label: String,
    val totalMg: Int,
    val progress: Float,
)

@Immutable
data class CaffeineDetailUiState(
    val entries: List<CaffeineEntry> = emptyList(),
    val bars: List<CaffeineBarState> = emptyList(),
    val totalTodayMg: Int = 0,
    val weeklyAverageMg: Int = 0,
    val lastTimeLabel: String = "--",
    val limitMg: Int = 300,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CaffeineDetailViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    val uiState = selectedDate
        .flatMapLatest { date ->
            val days = (6L downTo 0L).map(date::minusDays)
            combine(
                settingsRepository.settings,
                dashboardRepository.observeCaffeineBetween(days.first(), date),
            ) { settings, entries ->
                val entriesByDate = entries.groupBy(CaffeineEntry::date)
                val todayEntries = entriesByDate[date].orEmpty()
                val limit = settings.goalSettings.dailyCaffeineLimitMg
                CaffeineDetailUiState(
                    entries = todayEntries.sortedWith(compareBy(CaffeineEntry::time).thenBy(CaffeineEntry::createdAt)),
                    bars = days.map { day ->
                        val total = entriesByDate[day].orEmpty().sumOf(CaffeineEntry::estimatedMg)
                        CaffeineBarState(
                            label = day.dayOfWeek.name.take(1),
                            totalMg = total,
                            progress = clampProgress(total.toFloat(), limit.toFloat()),
                        )
                    },
                    totalTodayMg = todayEntries.sumOf(CaffeineEntry::estimatedMg),
                    weeklyAverageMg = if (entries.isEmpty()) 0 else entries.sumOf(CaffeineEntry::estimatedMg) / days.size,
                    lastTimeLabel = todayEntries.maxByOrNull(CaffeineEntry::time)?.time?.toString() ?: "--",
                    limitMg = limit,
                )
            }
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CaffeineDetailUiState(),
        )

    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
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
    val viewModel: CaffeineDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedDate) {
        viewModel.setSelectedDate(selectedDate)
    }

    CaffeineDetailContent(
        state = uiState,
        onDelete = viewModel::deleteEntry,
        windowSizeClass = windowSizeClass,
    )
}

@Composable
fun CaffeineDetailContent(
    state: CaffeineDetailUiState,
    onDelete: (Long) -> Unit,
    windowSizeClass: HealthWindowSizeClass = HealthWindowSizeClass.COMPACT,
) {
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
                CaffeineTodayTotalCard(state = state)
                CaffeineMetricRow(state = state)
                HealthCard(modifier = Modifier.fillMaxWidth()) {
                    CaffeineWeekChart(bars = state.bars)
                }
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
            CaffeineTodayTotalCard(state = state)
        }
        item {
            CaffeineMetricRow(state = state)
        }
        item {
            HealthCard(modifier = Modifier.fillMaxWidth()) {
                CaffeineWeekChart(bars = state.bars)
            }
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
private fun CaffeineTodayTotalCard(state: CaffeineDetailUiState) {
    InsightCard(
        modifier = Modifier.fillMaxWidth(),
        title = stringResource(R.string.caffeine_detail_today_total),
        value = stringResource(R.string.caffeine_today_total_format, state.totalTodayMg, state.limitMg),
        subtitle = stringResource(R.string.caffeine_estimate_notice),
    )
}

@Composable
private fun CaffeineMetricRow(state: CaffeineDetailUiState) {
    Row(horizontalArrangement = Arrangement.spacedBy(HealthSpacing.sm)) {
        InsightCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.caffeine_detail_weekly_average),
            value = stringResource(R.string.caffeine_today_total_format, state.weeklyAverageMg, state.limitMg),
            subtitle = stringResource(R.string.common_weekly),
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
        } else {
            entries.forEach { entry ->
                HealthCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete(entry.id) },
                ) {
                    Text(
                        text = entry.customName ?: entry.drinkType.detailLabel(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.xs),
                        text = stringResource(
                            R.string.caffeine_detail_record_format,
                            entry.time.toString(),
                            entry.estimatedMg,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CaffeineWeekChart(bars: List<CaffeineBarState>) {
    Row(
        modifier = Modifier
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
                    text = if (bar.totalMg == 0) "--" else "${bar.totalMg} mg",
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
                                color = HealthPrimary,
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
