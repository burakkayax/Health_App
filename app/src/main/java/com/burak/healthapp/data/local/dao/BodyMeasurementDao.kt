package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementCheckEntity
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements ORDER BY date ASC, recordedAt ASC")
    suspend fun getAll(): List<BodyMeasurementEntity>

    @Query("SELECT * FROM body_measurements WHERE date = :date ORDER BY recordedAt DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY date ASC, recordedAt ASC")
    fun observeAll(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY date ASC, recordedAt ASC LIMIT 1")
    fun observeEarliest(): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatest(): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date = :date ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getForDate(date: LocalDate): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date <= :date ORDER BY date DESC, recordedAt DESC LIMIT 1")
    suspend fun getLatestOnOrBefore(date: LocalDate): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date <= :date ORDER BY date DESC, recordedAt DESC LIMIT 1")
    fun observeLatestOnOrBefore(date: LocalDate): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE date >= :date ORDER BY date ASC, recordedAt DESC LIMIT 1")
    fun observeEarliestOnOrAfter(date: LocalDate): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, recordedAt ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<BodyMeasurementEntity>>

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Upsert
    suspend fun upsert(measurement: BodyMeasurementEntity)
}
