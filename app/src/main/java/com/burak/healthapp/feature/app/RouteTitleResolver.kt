package com.burak.healthapp.feature.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.burak.healthapp.R
import com.burak.healthapp.core.ui.navigation.CaffeineDetailDestination
import com.burak.healthapp.core.ui.navigation.HydrationDetailDestination
import com.burak.healthapp.core.ui.navigation.MealHistoryDestination
import com.burak.healthapp.core.ui.navigation.ProfileDestination
import com.burak.healthapp.core.ui.navigation.ProfileGoalsDestination
import com.burak.healthapp.core.ui.navigation.SleepDetailDestination
import com.burak.healthapp.core.ui.navigation.StepDetailDestination
import com.burak.healthapp.core.ui.navigation.TodayDestination
import com.burak.healthapp.core.ui.navigation.TrendsDestination
import com.burak.healthapp.core.ui.navigation.WeightDetailDestination
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun resolveTitle(currentRoute: String, selectedDate: LocalDate): String {
    val today = LocalDate.now()
    return when (currentRoute) {
        TrendsDestination.route -> stringResource(TrendsDestination.titleRes)
        MealHistoryDestination.route -> {
            if (selectedDate == today) {
                stringResource(R.string.route_today_meals)
            } else {
                stringResource(
                    R.string.route_date_meals,
                    selectedDate.format(screenDateFormatter()),
                )
            }
        }
        ProfileDestination.route -> stringResource(ProfileDestination.titleRes)
        ProfileGoalsDestination.route -> stringResource(ProfileGoalsDestination.titleRes)
        WeightDetailDestination.route -> stringResource(WeightDetailDestination.titleRes)
        SleepDetailDestination.route -> stringResource(SleepDetailDestination.titleRes)
        StepDetailDestination.route -> stringResource(StepDetailDestination.titleRes)
        HydrationDetailDestination.route -> stringResource(HydrationDetailDestination.titleRes)
        CaffeineDetailDestination.route -> stringResource(CaffeineDetailDestination.titleRes)
        else -> {
            if (selectedDate == today) {
                stringResource(TodayDestination.titleRes)
            } else {
                selectedDate.format(screenDateFormatter())
            }
        }
    }
}

private fun screenDateFormatter(): DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("tr"))
