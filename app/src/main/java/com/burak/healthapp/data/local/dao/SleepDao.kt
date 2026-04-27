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
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY sessionDate ASC, endTime ASC")
    suspend fun getAll(): List<SleepSessionEntity>

    @Query("SELECT * FROM sleep_sessions WHERE sessionDate = :date ORDER BY endTime DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions ORDER BY endTime DESC LIMIT 1")
    fun observeLatest(): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions WHERE sessionDate BETWEEN :startDate AND :endDate ORDER BY sessionDate ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE sessionDate = :date ORDER BY endTime DESC LIMIT 1")
    suspend fun getForDate(date: LocalDate): SleepSessionEntity?

    @Query("DELETE FROM sleep_sessions WHERE sessionDate = :date")
    suspend fun deleteForDate(date: LocalDate)

    @Query("DELETE FROM sleep_sessions")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsert(session: SleepSessionEntity)
}
