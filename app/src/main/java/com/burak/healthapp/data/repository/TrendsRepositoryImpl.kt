package com.burak.healthapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.burak.healthapp.core.datastore.SettingsKeys
import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.ExerciseDao
import com.burak.healthapp.data.local.dao.HydrationDao
import com.burak.healthapp.data.local.dao.MealDao
import com.burak.healthapp.data.local.dao.SleepDao
import com.burak.healthapp.data.local.dao.SmokingDao
import com.burak.healthapp.data.local.dao.StepDao
import com.burak.healthapp.data.local.dao.SupplementDoseDao
import com.burak.healthapp.data.local.dao.SupplementTemplateDao
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
import com.burak.healthapp.data.local.mapper.createSupplementTemplatesFromNames
import com.burak.healthapp.data.local.mapper.toDomain
import com.burak.healthapp.data.local.mapper.toEntity
import com.burak.healthapp.domain.calculation.WeightMeasurementSample
import com.burak.healthapp.domain.calculation.averageCalories
import com.burak.healthapp.domain.calculation.averageProtein
import com.burak.healthapp.domain.calculation.averageSleepMinutes
import com.burak.healthapp.domain.calculation.averageSteps
import com.burak.healthapp.domain.calculation.averageWaterMl
import com.burak.healthapp.domain.calculation.buildCalendarWeekDays
import com.burak.healthapp.domain.calculation.buildInterpolatedWeightTrendPoints
import com.burak.healthapp.domain.calculation.buildMonthToDateDays
import com.burak.healthapp.domain.calculation.buildStepTrendPoints
import com.burak.healthapp.domain.calculation.buildWeeklyCalories
import com.burak.healthapp.domain.calculation.buildWeekToDateDays
import com.burak.healthapp.domain.calculation.clipWeightTrendDays
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.TodaySnapshot
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.repository.DashboardRepository
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.repository.TrendsRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TrendsRepositoryImpl(
    private val settingsRepository: SettingsRepository,
    private val mealDao: MealDao,
    private val hydrationDao: HydrationDao,
    private val sleepDao: SleepDao,
    private val stepDao: StepDao,
    private val measurementDao: BodyMeasurementDao,
) : TrendsRepository {
    override fun observeTrends(period: TrendsPeriod, endDate: LocalDate): Flow<TrendsSnapshot> {
        val dataDays = if (period == TrendsPeriod.WEEKLY) {
            buildWeekToDateDays(endDate)
        } else {
            buildMonthToDateDays(endDate)
        }
        val startDate = dataDays.first()
        val finalDate = endDate
        val weeklyDays = buildCalendarWeekDays(endDate)
        val weightMeasurements = combine(
            measurementDao.observeBetween(startDate, finalDate),
            measurementDao.observeLatestOnOrBefore(startDate),
            measurementDao.observeEarliestOnOrAfter(finalDate),
            measurementDao.observeEarliest(),
        ) { inWindow, beforeBoundary, afterBoundary, earliestMeasurement ->
            WeightChartContext(
                measurements = (listOfNotNull(beforeBoundary) + inWindow + listOfNotNull(afterBoundary))
                    .groupBy(BodyMeasurementEntity::date)
                    .mapNotNull { (_, entries) ->
                        entries.maxByOrNull(BodyMeasurementEntity::recordedAt)
                    }
                    .sortedBy(BodyMeasurementEntity::date),
                earliestMeasurementDate = earliestMeasurement?.date,
            )
        }

        val stepAndWeight = combine(
            stepDao.observeBetween(startDate, finalDate),
            weightMeasurements,
        ) { stepEntities, weightContext ->
            stepEntities to weightContext
        }

        return combine(
            settingsRepository.settings,
            mealDao.observeBetween(startDate, finalDate),
            hydrationDao.observeBetween(startDate, finalDate),
            sleepDao.observeBetween(startDate, finalDate),
            stepAndWeight,
        ) { settings, mealEntities, hydrationEntities, sleepEntities, stepWeightContext ->
            val (stepEntities, weightContext) = stepWeightContext
            val meals = mealEntities.map(MealEntryEntity::toDomain)
            val hydration = hydrationEntities.map(HydrationEntryEntity::toDomain)
            val sleeps = sleepEntities.map(SleepSessionEntity::toDomain)
            val steps = stepEntities.map(StepEntryEntity::toDomain)
            val weightMeasurementsForChart = weightContext.measurements.map { measurement ->
                WeightMeasurementSample(
                    date = measurement.date,
                    weightKg = measurement.weightKg,
                )
            }
            val clippedWeightDays = clipWeightTrendDays(
                days = dataDays,
                earliestMeasurementDate = weightContext.earliestMeasurementDate,
            )

            TrendsSnapshot(
                period = period,
                averageProteinGrams = averageProtein(meals, dataDays),
                averageSleepMinutes = averageSleepMinutes(sleeps, dataDays),
                averageWaterMl = averageWaterMl(hydration, dataDays),
                averageSteps = averageSteps(steps, dataDays),
                averageCalories = averageCalories(meals, dataDays),
                weeklyCalories = buildWeeklyCalories(
                    entries = meals,
                    days = weeklyDays,
                    targetCalories = settings.goalSettings.dailyCaloriesTarget,
                ),
                weightPoints = buildInterpolatedWeightTrendPoints(
                    days = clippedWeightDays,
                    measurements = weightMeasurementsForChart,
                ),
                stepPoints = buildStepTrendPoints(
                    entries = steps,
                    days = dataDays,
                ),
            )
        }
    }
}

private data class WeightChartContext(
    val measurements: List<BodyMeasurementEntity>,
    val earliestMeasurementDate: LocalDate?,
)
