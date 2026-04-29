package com.burak.healthapp.domain.repository

import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.CaffeineEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.TodaySnapshot
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DashboardRepository {
    fun observeToday(date: LocalDate = LocalDate.now()): Flow<TodaySnapshot>

    fun observeMealsForDate(date: LocalDate = LocalDate.now()): Flow<List<MealEntry>>

    fun observeHydrationBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<HydrationEntry>>

    fun observeLatestMeasurement(): Flow<BodyMeasurementEntry?>

    fun observeWeightHistory(): Flow<List<BodyMeasurementEntry>>

    fun observeSleepSessionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSession>>

    fun observeStepsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntry>>

    fun observeCaffeineForDate(date: LocalDate = LocalDate.now()): Flow<List<CaffeineEntry>>

    fun observeCaffeineBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<CaffeineEntry>>

    suspend fun saveMealEntry(entry: MealEntry)

    suspend fun deleteMealEntry(id: Long)

    suspend fun deleteHydrationEntry(id: Long)

    suspend fun deleteSleepForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteExerciseForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteSmokingForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteSupplementDoseForDate(templateId: Long, date: LocalDate = LocalDate.now())

    suspend fun deleteStepsForDate(date: LocalDate = LocalDate.now())

    suspend fun deleteBodyMeasurement(id: Long)

    suspend fun addHydration(amountMl: Int, date: LocalDate = LocalDate.now())

    suspend fun saveSleepSession(session: SleepSession)

    suspend fun saveExerciseEntry(entry: ExerciseEntry, date: LocalDate = LocalDate.now())

    suspend fun saveSmokingCount(count: Int, date: LocalDate = LocalDate.now())

    suspend fun incrementSmokingCount(date: LocalDate = LocalDate.now(), delta: Int = 1)

    suspend fun saveSupplementDoseEntries(
        entries: List<SupplementDoseEntry>,
        date: LocalDate = LocalDate.now(),
    )

    suspend fun saveBodyMeasurement(entry: BodyMeasurementEntry)

    suspend fun saveWeightMeasurement(weightKg: Float, date: LocalDate = LocalDate.now())

    suspend fun recordStepSensorValue(sensorValue: Int, date: LocalDate = LocalDate.now())

    suspend fun addCaffeine(entry: CaffeineEntry)

    suspend fun deleteCaffeine(id: Long)
}
