package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.CaffeineEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CaffeineDao {
    @Query("SELECT * FROM caffeine_entries ORDER BY date ASC, time ASC, createdAt ASC")
    suspend fun getAll(): List<CaffeineEntryEntity>

    @Query("SELECT * FROM caffeine_entries WHERE date = :date ORDER BY time ASC, createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<CaffeineEntryEntity>>

    @Query("SELECT * FROM caffeine_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, time ASC, createdAt ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<CaffeineEntryEntity>>

    @Upsert
    suspend fun upsert(entry: CaffeineEntryEntity)

    @Query("DELETE FROM caffeine_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM caffeine_entries")
    suspend fun deleteAll()
}
