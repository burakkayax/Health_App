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
interface MealDao {
    @Query("SELECT * FROM meal_entries ORDER BY date ASC, mealType ASC, createdAt ASC")
    suspend fun getAll(): List<MealEntryEntity>

    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY mealType ASC, createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<MealEntryEntity>>

    @Upsert
    suspend fun upsert(entry: MealEntryEntity)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meal_entries")
    suspend fun deleteAll()
}
