package com.burak.healthapp.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.burak.healthapp.data.repository.SettingsRepository
import com.burak.healthapp.data.repository.TrendsRepository
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.ui.model.InsightCardState
import com.burak.healthapp.ui.model.TrendChartState
import com.burak.healthapp.ui.model.TrendsUiState
import com.burak.healthapp.ui.model.WeeklyCalorieBarState
import com.burak.healthapp.ui.model.WeeklyCaloriesCardState
import com.burak.healthapp.ui.root.healthApplication
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class TrendsViewModel(
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
                snapshot.toUiState(avatarInitials = settings.userProfile.avatarInitials)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyUiState(),
        )

    fun selectPeriod(period: TrendsPeriod) {
        selectedPeriod.value = period
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TrendsViewModel(
                    settingsRepository = healthApplication().container.settingsRepository,
                    trendsRepository = healthApplication().container.trendsRepository,
                )
            }
        }
    }
}

private fun TrendsSnapshot.toUiState(avatarInitials: String): TrendsUiState {
    val locale = Locale.forLanguageTag("tr")
    val sleepHours = averageSleepMinutes.toInt() / 60
    val sleepMinutes = averageSleepMinutes.toInt() % 60
    val periodSubtitle = if (period == TrendsPeriod.WEEKLY) "Bu hafta" else "Bu ay"

    return TrendsUiState(
        avatarInitials = avatarInitials,
        selectedPeriod = period,
        insights = listOf(
            InsightCardState(
                title = "Günlük Ortalama Protein",
                value = String.format(locale, "%.0f g", averageProteinGrams),
                subtitle = periodSubtitle,
            ),
            InsightCardState(
                title = "Ortalama Su",
                value = String.format(locale, "%.0f ml", averageWaterMl),
                subtitle = periodSubtitle,
            ),
            InsightCardState(
                title = "Ortalama Uyku",
                value = "${sleepHours}s ${sleepMinutes}d",
                subtitle = periodSubtitle,
            ),
            InsightCardState(
                title = "Ortalama Adım",
                value = String.format(locale, "%.0f adım", averageSteps),
                subtitle = periodSubtitle,
            ),
        ),
        weeklyCaloriesCard = if (period == TrendsPeriod.WEEKLY) {
            WeeklyCaloriesCardState(
                averageCaloriesLabel = String.format(locale, "%.0f kcal", averageCalories),
                subtitle = "Pazartesi - Pazar",
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
                title = "Kilo Trendi",
                subtitle = "Turkuaz çizgiyle yumuşak akış.",
                points = weightPoints,
            ),
            TrendChartState(
                title = "Adım Trendi",
                subtitle = "Günlük hedefe göre adım akışı.",
                points = stepPoints,
            ),
        ),
    )
}

private fun emptyUiState(): TrendsUiState {
    return TrendsUiState(
        avatarInitials = "M",
        selectedPeriod = TrendsPeriod.WEEKLY,
        insights = emptyList(),
        weeklyCaloriesCard = null,
        charts = emptyList(),
    )
}
