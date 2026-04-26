package com.burak.healthapp.ui.weightdetail

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.burak.healthapp.data.repository.DashboardRepository
import com.burak.healthapp.data.repository.SettingsRepository
import com.burak.healthapp.domain.calculation.calculateBodyMassIndex
import com.burak.healthapp.domain.calculation.classifyBodyMassIndex
import com.burak.healthapp.domain.calculation.normalizeBodyMassIndexToGauge
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.TrendPoint
import com.burak.healthapp.ui.components.HealthCard
import com.burak.healthapp.ui.components.BmiGaugeChart
import com.burak.healthapp.ui.components.SmoothTrendChart
import com.burak.healthapp.ui.model.BmiGaugeState
import com.burak.healthapp.ui.model.WeightDetailUiState
import com.burak.healthapp.ui.model.WeightHistoryItemState
import com.burak.healthapp.ui.root.healthApplication
import com.burak.healthapp.ui.theme.HealthSpacing
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeightDetailViewModel(
    settingsRepository: SettingsRepository,
    private val dashboardRepository: DashboardRepository,
) : ViewModel() {
    val uiState = combine(
        settingsRepository.settings,
        dashboardRepository.observeWeightHistory(),
    ) { settings, measurements ->
        measurements.toWeightDetailUiState(heightCm = settings.userProfile.heightCm)
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                WeightDetailViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                    dashboardRepository = healthApplication().container.dashboardRepository,
                )
            }
        }
    }
}

@Composable
fun WeightDetailRoute() {
    val viewModel: WeightDetailViewModel = viewModel(factory = WeightDetailViewModel.Factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeightDetailContent(
        state = uiState,
        onDeleteMeasurement = viewModel::deleteMeasurement,
    )
}

@Composable
fun WeightDetailContent(
    state: WeightDetailUiState,
    onDeleteMeasurement: (Long) -> Unit,
) {
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
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weight_detail_chart_card"),
            ) {
                Text(
                    text = "Kilo Geçmişi Grafiği",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (state.chartPoints.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(top = HealthSpacing.sm),
                        text = "Henüz kilo kaydı yok. Kilo kartındaki + Ekle aksiyonuyla kayıt oluşturmaya başla.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.historyItems.firstOrNull()?.let { latestItem ->
                        Text(
                            modifier = Modifier.padding(top = HealthSpacing.xs),
                            text = "Son kayıt ${latestItem.weightLabel}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    SmoothTrendChart(
                        modifier = Modifier
                            .padding(top = HealthSpacing.sm)
                            .testTag("weight_detail_chart"),
                        points = state.chartPoints,
                        chartHeight = 240.dp,
                    )
                }
            }
        }
        item {
            HealthCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weight_detail_bmi_card"),
            ) {
                Text(
                    text = "Vücut Kitle İndeksi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    modifier = Modifier.padding(top = HealthSpacing.xs),
                    text = "Boyun ve en son kilo kaydın üzerinden güncel VKİ durumunu gösterir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BmiGaugeChart(
                    modifier = Modifier
                        .padding(top = HealthSpacing.sm)
                        .testTag("weight_detail_bmi_gauge"),
                    state = state.bmiGauge,
                )
                if (state.bmiGauge.helperMessage != null) {
                    Text(
                        modifier = Modifier
                            .padding(top = HealthSpacing.xs)
                            .testTag("weight_detail_bmi_helper"),
                        text = state.bmiGauge.helperMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            Text(
                text = "Girilenler Geçmişi",
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
                        text = "Henüz geçmiş kilo kaydı bulunmuyor.",
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
                        IconButton(
                            modifier = Modifier.testTag("weight_history_delete_${item.id}"),
                            onClick = { onDeleteMeasurement(item.id) },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Kilo kaydını sil",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun List<BodyMeasurementEntry>.toWeightDetailUiState(heightCm: Float?): WeightDetailUiState {
    val locale = Locale.forLanguageTag("tr")
    val historyFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    val chartFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
    val chartMeasurements = groupBy(BodyMeasurementEntry::date)
        .mapNotNull { (_, entries) -> entries.maxByOrNull(BodyMeasurementEntry::recordedAt) }
        .sortedBy(BodyMeasurementEntry::date)
    val latestMeasurement = maxWithOrNull(
        compareBy<BodyMeasurementEntry> { it.date }.thenBy { it.recordedAt },
    )
    val bmi = calculateBodyMassIndex(latestMeasurement?.weightKg, heightCm)

    return WeightDetailUiState(
        chartPoints = chartMeasurements.map { measurement ->
            TrendPoint(
                label = measurement.date.format(chartFormatter),
                value = measurement.weightKg,
            )
        },
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
