package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HydrationDao {
    @Query("SELECT * FROM hydration_entries ORDER BY date ASC, createdAt ASC")
    suspend fun getAll(): List<HydrationEntryEntity>

    @Query("SELECT * FROM hydration_entries WHERE date = :date ORDER BY createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<HydrationEntryEntity>>

    @Query("SELECT * FROM hydration_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<HydrationEntryEntity>>

    @Upsert
    suspend fun upsert(entry: HydrationEntryEntity)

    @Query("DELETE FROM hydration_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM hydration_entries")
    suspend fun deleteAll()
}
