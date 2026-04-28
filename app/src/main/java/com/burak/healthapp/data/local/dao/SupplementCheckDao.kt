package com.burak.healthapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.burak.healthapp.data.local.entity.SupplementCheckEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SupplementCheckDao {
    @Query("SELECT * FROM supplement_checks WHERE date = :date")
    fun observeForDate(date: LocalDate): Flow<List<SupplementCheckEntity>>

    @Upsert
    suspend fun upsert(check: SupplementCheckEntity)

    @Query("DELETE FROM supplement_checks")
    suspend fun deleteAll()
}
