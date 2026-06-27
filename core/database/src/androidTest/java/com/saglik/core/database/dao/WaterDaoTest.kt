package com.saglik.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.saglik.core.database.AppDatabase
import com.saglik.core.database.entity.WaterEntryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WaterDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: WaterDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java,
        ).build()
        dao = db.waterDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadEntry() = runTest {
        val entry = WaterEntryEntity(
            id = "1",
            amountMl = 250,
            recordedAt = 1000L,
            source = "USER_ENTERED"
        )

        dao.insert(entry)

        val flow = dao.getWaterEntries(0L, 2000L)
        val entries = flow.first()
        
        assertEquals(1, entries.size)
        assertEquals(250, entries[0].amountMl)
    }
}
