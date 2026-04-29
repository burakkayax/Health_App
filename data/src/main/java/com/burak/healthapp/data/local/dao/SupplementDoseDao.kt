package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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
