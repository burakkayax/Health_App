package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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

    @Query("DELETE FROM exercise_entries")
    suspend fun deleteAll()
}
