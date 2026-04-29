package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.StepEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StepDao {
    @Query("SELECT * FROM step_entries ORDER BY date ASC, updatedAt ASC")
    suspend fun getAll(): List<StepEntryEntity>

    @Query("SELECT * FROM step_entries WHERE date = :date LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<StepEntryEntity?>

    @Query("SELECT * FROM step_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<StepEntryEntity>>

    @Query("SELECT * FROM step_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): StepEntryEntity?

    @Query("SELECT * FROM step_entries ORDER BY date DESC, updatedAt DESC LIMIT 1")
    suspend fun getLatest(): StepEntryEntity?

    @Query("DELETE FROM step_entries WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate)

    @Query("DELETE FROM step_entries")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsert(entry: StepEntryEntity)
}
