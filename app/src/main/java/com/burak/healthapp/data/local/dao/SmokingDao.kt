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
interface SmokingDao {
    @Query("SELECT * FROM smoking_entries ORDER BY date ASC")
    suspend fun getAll(): List<SmokingEntryEntity>

    @Query("SELECT * FROM smoking_entries WHERE date = :date ORDER BY id DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<SmokingEntryEntity?>

    @Query("SELECT * FROM smoking_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): SmokingEntryEntity?

    @Upsert
    suspend fun upsert(entry: SmokingEntryEntity)

    @Query("DELETE FROM smoking_entries WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate)
}
