package com.burak.healthapp.data.repository

import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.CaffeineDao
import com.burak.healthapp.data.local.dao.ExerciseDao
import com.burak.healthapp.data.local.dao.HydrationDao
import com.burak.healthapp.data.local.dao.MealDao
import com.burak.healthapp.data.local.dao.SleepDao
import com.burak.healthapp.data.local.dao.SmokingDao
import com.burak.healthapp.data.local.dao.StepDao
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.CaffeineEntryEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.mapper.toDomain
import com.burak.healthapp.domain.calculation.WeightMeasurementSample
import com.burak.healthapp.domain.calculation.averageCalories
import com.burak.healthapp.domain.calculation.averageProtein
import com.burak.healthapp.domain.calculation.averageSleepMinutes
import com.burak.healthapp.domain.calculation.averageSteps
import com.burak.healthapp.domain.calculation.averageWaterMl
import com.burak.healthapp.domain.calculation.buildCalendarWeekDays
import com.burak.healthapp.domain.calculation.buildInterpolatedWeightTrendPoints
import com.burak.healthapp.domain.calculation.buildStepTrendPoints
import com.burak.healthapp.domain.calculation.buildWeeklyCalories
import com.burak.healthapp.domain.calculation.calculateSleepStabilityMetrics
import com.burak.healthapp.domain.calculation.clipWeightTrendDays
import com.burak.healthapp.domain.calculation.metricDateWindowFor
import com.burak.healthapp.domain.calculation.previousMetricDateWindowFor
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.TrendsPeriod
import com.burak.healthapp.domain.model.TrendsSnapshot
import com.burak.healthapp.domain.repository.SettingsRepository
import com.burak.healthapp.domain.repository.TrendsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Duration
import java.time.LocalDate

class TrendsRepositoryImpl(
    private val settingsRepository: SettingsRepository,
    private val mealDao: MealDao,
    private val hydrationDao: HydrationDao,
    private val sleepDao: SleepDao,
    private val stepDao: StepDao,
    private val caffeineDao: CaffeineDao,
    private val smokingDao: SmokingDao,
    private val exerciseDao: ExerciseDao,
    private val measurementDao: BodyMeasurementDao,
) : TrendsRepository {
    override fun observeTrends(period: TrendsPeriod, endDate: LocalDate): Flow<TrendsSnapshot> {
        val currentWindow = metricDateWindowFor(endDate, period)
        val previousWindow = previousMetricDateWindowFor(endDate, period)
        val dataDays = currentWindow.days()
        val previousDays = previousWindow.days()
        val startDate = currentWindow.startDate
        val queryStartDate = previousWindow.startDate
        val finalDate = currentWindow.endDateInclusive
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
            stepDao.observeBetween(queryStartDate, finalDate),
            caffeineDao.observeBetween(queryStartDate, finalDate),
            smokingDao.observeBetween(queryStartDate, finalDate),
            exerciseDao.observeBetween(queryStartDate, finalDate),
            weightMeasurements,
        ) { stepEntities, caffeineEntities, smokingEntities, exerciseEntities, weightContext ->
            SecondaryTrendContext(
                steps = stepEntities.map(StepEntryEntity::toDomain),
                caffeine = caffeineEntities.map(CaffeineEntryEntity::toDomain),
                smoking = smokingEntities.map(SmokingEntryEntity::toDomain),
                exercise = exerciseEntities.map(ExerciseEntryEntity::toDomain),
                weightContext = weightContext,
            )
        }

        return combine(
            settingsRepository.settings,
            mealDao.observeBetween(queryStartDate, finalDate),
            hydrationDao.observeBetween(queryStartDate, finalDate),
            sleepDao.observeBetween(queryStartDate, finalDate),
            stepAndWeight,
        ) { settings, mealEntities, hydrationEntities, sleepEntities, secondary ->
            val meals = mealEntities.map(MealEntryEntity::toDomain)
            val hydration = hydrationEntities.map(HydrationEntryEntity::toDomain)
            val sleeps = sleepEntities.map(SleepSessionEntity::toDomain)
            val weightMeasurementsForChart = secondary.weightContext.measurements.map { measurement ->
                WeightMeasurementSample(
                    date = measurement.date,
                    weightKg = measurement.weightKg,
                )
            }
            val clippedWeightDays = clipWeightTrendDays(
                days = dataDays,
                earliestMeasurementDate = secondary.weightContext.earliestMeasurementDate,
            )
            val currentMeals = meals.filterInDays(dataDays, MealEntry::date)
            val previousMeals = meals.filterInDays(previousDays, MealEntry::date)
            val currentHydration = hydration.filterInDays(dataDays, HydrationEntry::date)
            val previousHydration = hydration.filterInDays(previousDays, HydrationEntry::date)
            val currentSleeps = sleeps.filter { session -> session.sessionDate in dataDays }
            val previousSleeps = sleeps.filter { session -> session.sessionDate in previousDays }
            val currentSteps = secondary.steps.filterInDays(dataDays, StepEntry::date)
            val previousSteps = secondary.steps.filterInDays(previousDays, StepEntry::date)
            val currentCaffeine = secondary.caffeine.filterInDays(dataDays, CaffeineEntry::date)
            val previousCaffeine = secondary.caffeine.filterInDays(previousDays, CaffeineEntry::date)
            val currentSmoking = secondary.smoking.filterInDays(dataDays, SmokingEntry::date)
            val previousSmoking = secondary.smoking.filterInDays(previousDays, SmokingEntry::date)
            val currentExercise = secondary.exercise.filterInDays(dataDays, ExerciseEntry::date)
            val previousExercise = secondary.exercise.filterInDays(previousDays, ExerciseEntry::date)
            val currentMeasurements = secondary.weightContext.measurements
                .filter { measurement -> measurement.date in dataDays }
                .sortedBy(BodyMeasurementEntity::date)

            TrendsSnapshot(
                period = period,
                days = dataDays,
                previousDays = previousDays,
                averageProteinGrams = averageProtein(currentMeals, dataDays),
                previousAverageProteinGrams = averageProtein(previousMeals, previousDays),
                averageSleepMinutes = averageSleepMinutes(currentSleeps, dataDays),
                previousAverageSleepMinutes = averageSleepMinutes(previousSleeps, previousDays),
                averageWaterMl = averageWaterMl(currentHydration, dataDays),
                previousAverageWaterMl = averageWaterMl(previousHydration, previousDays),
                averageSteps = averageSteps(currentSteps, dataDays),
                previousAverageSteps = averageSteps(previousSteps, previousDays),
                averageCalories = averageCalories(currentMeals, dataDays),
                previousAverageCalories = averageCalories(previousMeals, previousDays),
                averageCaffeineMg = averageCaffeineMg(currentCaffeine, dataDays),
                previousAverageCaffeineMg = averageCaffeineMg(previousCaffeine, previousDays),
                averageSmokingCount = averageSmokingCount(currentSmoking, dataDays),
                previousAverageSmokingCount = averageSmokingCount(previousSmoking, previousDays),
                exerciseTotalMinutes = currentExercise.sumOf(ExerciseEntry::durationMinutes),
                previousExerciseTotalMinutes = previousExercise.sumOf(ExerciseEntry::durationMinutes),
                exerciseActiveDays = currentExercise.activeDays(),
                previousExerciseActiveDays = previousExercise.activeDays(),
                waterGoalMetDays = currentHydration.totalByDate(HydrationEntry::date, HydrationEntry::amountMl).countAtLeast(settings.goalSettings.waterTargetMl),
                stepGoalMetDays = currentSteps.associate { entry -> entry.date to entry.steps }.countAtLeast(settings.goalSettings.dailyStepTarget),
                sleepGoalMetDays = currentSleeps.countSleepGoalDays(settings),
                caffeineUnderLimitDays = dataDays.size - currentCaffeine.totalByDate(CaffeineEntry::date, CaffeineEntry::estimatedMg).countAbove(settings.goalSettings.dailyCaffeineLimitMg),
                caffeineOverLimitDays = currentCaffeine.totalByDate(CaffeineEntry::date, CaffeineEntry::estimatedMg).countAbove(settings.goalSettings.dailyCaffeineLimitMg),
                caffeineAfterCutoffDays = currentCaffeine.countAfterCutoffDays(settings),
                smokingUnderLimitDays = dataDays.size - currentSmoking.associate { entry -> entry.date to entry.count }.countSmokingOverLimit(settings),
                smokingOverLimitDays = currentSmoking.associate { entry -> entry.date to entry.count }.countSmokingOverLimit(settings),
                smokingZeroDays = dataDays.size - currentSmoking.count { entry -> entry.count > 0 },
                nutritionLoggedDays = currentMeals.distinctDateCount(MealEntry::date),
                hydrationLoggedDays = currentHydration.distinctDateCount(HydrationEntry::date),
                sleepLoggedDays = currentSleeps.map { session -> session.sessionDate }.distinct().size,
                stepLoggedDays = currentSteps.distinctDateCount(StepEntry::date),
                caffeineLoggedDays = currentCaffeine.distinctDateCount(CaffeineEntry::date),
                smokingLoggedDays = currentSmoking.distinctDateCount(SmokingEntry::date),
                exerciseLoggedDays = currentExercise.activeDays(),
                weightRecordCount = currentMeasurements.size,
                weightStartKg = currentMeasurements.firstOrNull()?.weightKg,
                weightEndKg = currentMeasurements.lastOrNull()?.weightKg,
                weeklyCalories = buildWeeklyCalories(
                    entries = currentMeals,
                    days = weeklyDays,
                    targetCalories = settings.goalSettings.dailyCaloriesTarget,
                ),
                weightPoints = buildInterpolatedWeightTrendPoints(
                    days = clippedWeightDays,
                    measurements = weightMeasurementsForChart,
                ),
                stepPoints = buildStepTrendPoints(
                    entries = currentSteps,
                    days = dataDays,
                ),
                sleepStability = calculateSleepStabilityMetrics(
                    sessions = currentSleeps,
                    targetBedtime = settings.goalSettings.sleepTargetBedtime,
                    targetWakeTime = settings.goalSettings.sleepTargetWakeTime,
                ),
            )
        }
    }
}

private data class WeightChartContext(
    val measurements: List<BodyMeasurementEntity>,
    val earliestMeasurementDate: LocalDate?,
)

private data class SecondaryTrendContext(
    val steps: List<StepEntry>,
    val caffeine: List<CaffeineEntry>,
    val smoking: List<SmokingEntry>,
    val exercise: List<ExerciseEntry>,
    val weightContext: WeightChartContext,
)

private fun <T> List<T>.filterInDays(
    days: List<LocalDate>,
    dateOf: (T) -> LocalDate,
): List<T> = filter { entry -> dateOf(entry) in days }

private fun <T> List<T>.totalByDate(
    dateOf: (T) -> LocalDate,
    valueOf: (T) -> Int,
): Map<LocalDate, Int> = groupBy(dateOf).mapValues { (_, entries) -> entries.sumOf(valueOf) }

private fun Map<LocalDate, Int>.countAtLeast(target: Int): Int = values.count { value -> value >= target }

private fun Map<LocalDate, Int>.countAbove(limit: Int): Int = values.count { value -> value > limit }

private fun Map<LocalDate, Int>.countSmokingOverLimit(settings: SettingsState): Int {
    val limit = settings.goalSettings.smokeDailyLimit
    return values.count { count -> if (limit <= 0) count > 0 else count >= limit }
}

private fun List<CaffeineEntry>.countAfterCutoffDays(settings: SettingsState): Int {
    val cutoff = settings.goalSettings.caffeineCutoffTime
    return groupBy(CaffeineEntry::date).count { (_, entries) ->
        entries.any { entry -> !entry.time.isBefore(cutoff) }
    }
}

private fun List<SleepSession>.countSleepGoalDays(settings: SettingsState): Int {
    val targetMinutes = settings.goalSettings.sleepTargetMinutes
    return count { session ->
        Duration.between(session.startTime, session.endTime).toMinutes() >= targetMinutes
    }
}

private fun List<ExerciseEntry>.activeDays(): Int = filter { entry -> entry.durationMinutes > 0 }
    .map(ExerciseEntry::date)
    .distinct()
    .size

private fun <T> List<T>.distinctDateCount(dateOf: (T) -> LocalDate): Int = map(dateOf).distinct().size

private fun averageCaffeineMg(entries: List<CaffeineEntry>, days: List<LocalDate>): Float =
    if (days.isEmpty()) 0f else entries.sumOf(CaffeineEntry::estimatedMg).toFloat() / days.size

private fun averageSmokingCount(entries: List<SmokingEntry>, days: List<LocalDate>): Float =
    if (days.isEmpty()) 0f else entries.sumOf(SmokingEntry::count).toFloat() / days.size
