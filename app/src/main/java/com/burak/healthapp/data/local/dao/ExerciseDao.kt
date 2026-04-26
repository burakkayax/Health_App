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
interface ExerciseDao {
    @Query("SELECT * FROM exercise_entries ORDER BY date ASC")
    suspend fun getAll(): List<ExerciseEntryEntity>

    @Query("SELECT * FROM exercise_entries WHERE date = :date ORDER BY id DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<ExerciseEntryEntity?>

    @Query("SELECT * FROM exercise_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<ExerciseEntryEntity>>

    @Query("SELECT * FROM exercise_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): ExerciseEntryEntity?

    @Upsert
    suspend fun upsert(entry: ExerciseEntryEntity)

    @Query("DELETE FROM exercise_entries WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate)
}
