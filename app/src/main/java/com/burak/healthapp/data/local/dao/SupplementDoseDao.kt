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
interface SupplementDoseDao {
    @Query("SELECT * FROM supplement_dose_entries ORDER BY date ASC, loggedAt ASC")
    suspend fun getAll(): List<SupplementDoseEntryEntity>

    @Query("SELECT * FROM supplement_dose_entries WHERE date = :date ORDER BY loggedAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<SupplementDoseEntryEntity>>

    @Upsert
    suspend fun upsertAll(entries: List<SupplementDoseEntryEntity>)

    @Query("DELETE FROM supplement_dose_entries WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate)

    @Query("DELETE FROM supplement_dose_entries WHERE templateId = :templateId AND date = :date")
    suspend fun deleteForTemplateAndDate(templateId: Long, date: LocalDate)

    @Query("DELETE FROM supplement_dose_entries")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceForDate(date: LocalDate, entries: List<SupplementDoseEntryEntity>) {
        deleteForDate(date)
        if (entries.isNotEmpty()) {
            upsertAll(entries)
        }
    }
}
