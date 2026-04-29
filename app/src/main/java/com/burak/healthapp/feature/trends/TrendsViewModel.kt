package com.burak.healthapp.feature.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.model.WeeklyCalorieBarState
import com.burak.healthapp.core.ui.model.buildWeightTrendChartState
import com.burak.healthapp.core.ui.text.UiText
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.repository.TrendsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TrendsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val trendsRepository: TrendsRepository,
) : ViewModel() {
    private val selectedPeriod = MutableStateFlow(TrendsPeriod.WEEKLY)

    val uiState = selectedPeriod
        .flatMapLatest { period ->
            combine(
                settingsRepository.settings,
                trendsRepository.observeTrends(period),
            ) { settings, snapshot ->
                snapshot.toUiState(
                    avatarInitials = settings.userProfile.avatarInitials,
                    targetWeightKg = settings.goalSettings.targetWeightKg,
                )
            }
                .flowOn(Dispatchers.Default)
                .distinctUntilChanged()
        }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyUiState(),
        )

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }
}

private fun TrendsSnapshot.toUiState(
    avatarInitials: String,
    targetWeightKg: Float,
): TrendsUiState {
    val sleepHours = averageSleepMinutes.toInt() / 60
    val sleepMinutes = averageSleepMinutes.toInt() % 60
    val periodSubtitle = stringRes(
        if (period == TrendsPeriod.WEEKLY) {
            R.string.trends_period_week
        } else {
            R.string.trends_period_month
        },
    )

    return TrendsUiState(
        avatarInitials = avatarInitials,
        selectedPeriod = period,
        insights = listOf(
            InsightCardState(
                title = stringRes(R.string.trends_insight_avg_protein),
                value = stringRes(R.string.format_grams_float, averageProteinGrams),
                subtitle = periodSubtitle,
                hasData = averageProteinGrams > 0f,
            ),
            InsightCardState(
                title = stringRes(R.string.trends_insight_avg_water),
                value = stringRes(R.string.format_ml_float, averageWaterMl),
                subtitle = periodSubtitle,
                hasData = averageWaterMl > 0f,
            ),
            InsightCardState(
                title = stringRes(R.string.trends_insight_avg_sleep),
                value = stringRes(R.string.format_sleep_short, sleepHours, sleepMinutes),
                subtitle = periodSubtitle,
                hasData = averageSleepMinutes > 0f,
            ),
            InsightCardState(
                title = stringRes(R.string.trends_insight_avg_steps),
                value = stringRes(R.string.format_steps_float, averageSteps),
                subtitle = periodSubtitle,
                hasData = averageSteps > 0f,
            ),
        ),
        weeklyCaloriesCard = if (period == TrendsPeriod.WEEKLY) {
            WeeklyCaloriesCardState(
                averageCaloriesLabel = stringRes(R.string.format_kcal_float, averageCalories),
                subtitle = stringRes(R.string.trends_week_range),
                bars = weeklyCalories.map { bar ->
                    WeeklyCalorieBarState(
                        label = bar.label,
                        calories = bar.calories,
                        progress = bar.progress,
                    )
                },
            )
        } else {
            null
        },
        charts = listOf(
            TrendChartState(
                title = stringRes(R.string.trends_step_chart_title),
                subtitle = stringRes(R.string.trends_step_chart_subtitle),
                points = stepPoints,
            ),
        ),
        weightChart = buildWeightTrendChartState(
            points = weightPoints,
            targetWeightKg = targetWeightKg,
        )?.let { chart ->
            WeightTrendChartCardState(
                title = stringRes(R.string.trends_weight_chart_title),
                subtitle = stringRes(R.string.trends_weight_chart_subtitle),
                chart = chart,
            )
        },
    )
}

private fun emptyUiState(): TrendsUiState = TrendsUiState(
    avatarInitials = "M",
    selectedPeriod = TrendsPeriod.WEEKLY,
    insights = emptyList(),
    weeklyCaloriesCard = null,
    charts = emptyList(),
)

private fun stringRes(resId: Int, vararg args: Any): UiText = UiText.StringResource(resId = resId, args = args.toList())
