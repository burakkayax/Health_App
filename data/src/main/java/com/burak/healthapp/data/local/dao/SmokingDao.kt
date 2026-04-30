package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SmokingDao {
    @Query("SELECT * FROM smoking_entries ORDER BY date ASC")
    suspend fun getAll(): List<SmokingEntryEntity>

    @Query("SELECT * FROM smoking_entries WHERE date = :date ORDER BY id DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<SmokingEntryEntity?>

    @Query("SELECT * FROM smoking_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SmokingEntryEntity>>

    @Query("SELECT * FROM smoking_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): SmokingEntryEntity?

    @Upsert
    suspend fun upsert(entry: SmokingEntryEntity)

    @Query("DELETE FROM smoking_entries WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate)

    @Query("DELETE FROM smoking_entries")
    suspend fun deleteAll()
}
