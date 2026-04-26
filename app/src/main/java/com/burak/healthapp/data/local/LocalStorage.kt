package com.burak.healthapp.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Upsert
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow

class HealthTypeConverters {
    @TypeConverter
    fun localDateToString(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun localTimeToString(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun stringToLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)
}

@Entity(tableName = "meal_entries")
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val mealType: String,
    val name: String,
    val calories: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinGrams: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "hydration_entries")
data class HydrationEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionDate: LocalDate,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)

@Entity(
    tableName = "exercise_entries",
    indices = [
        Index(value = ["date"], unique = true),
    ],
)
data class ExerciseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val type: String,
    val durationMinutes: Int,
    val intensity: String,
)

@Entity(
    tableName = "smoking_entries",
    indices = [
        Index(value = ["date"], unique = true),
    ],
)
data class SmokingEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val count: Int,
)

@Entity(
    tableName = "step_entries",
    indices = [
        Index(value = ["date"], unique = true),
    ],
)
data class StepEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val steps: Int,
    val sensorBaseline: Int?,
    val lastSensorValue: Int?,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "supplement_templates")
data class SupplementTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Float,
    val unitLabel: String,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
)

@Entity(
    tableName = "supplement_checks",
    foreignKeys = [
        ForeignKey(
            entity = SupplementTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["templateId", "date"], unique = true),
    ],
)
data class SupplementCheckEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val date: LocalDate,
    val isChecked: Boolean,
    val checkedAt: LocalDateTime? = null,
)

@Entity(
    tableName = "supplement_dose_entries",
    foreignKeys = [
        ForeignKey(
            entity = SupplementTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["templateId", "date"], unique = true),
    ],
)
data class SupplementDoseEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val date: LocalDate,
    val amount: Float,
    val loggedAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float,
    val shoulderCm: Float,
    val waistCm: Float,
    val hipCm: Float,
    val recordedAt: LocalDateTime = LocalDateTime.now(),
)

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY mealType ASC, createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<MealEntryEntity>>

    @Upsert
    suspend fun upsert(entry: MealEntryEntity)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface HydrationDao {
    @Query("SELECT * FROM hydration_entries WHERE date = :date ORDER BY createdAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<HydrationEntryEntity>>

    @Query("SELECT * FROM hydration_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, createdAt ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<HydrationEntryEntity>>

    @Upsert
    suspend fun upsert(entry: HydrationEntryEntity)

    @Query("DELETE FROM hydration_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions WHERE sessionDate = :date ORDER BY endTime DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions ORDER BY endTime DESC LIMIT 1")
    fun observeLatest(): Flow<SleepSessionEntity?>

    @Query("SELECT * FROM sleep_sessions WHERE sessionDate BETWEEN :startDate AND :endDate ORDER BY sessionDate ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE sessionDate = :date ORDER BY endTime DESC LIMIT 1")
    suspend fun getForDate(date: LocalDate): SleepSessionEntity?

    @Query("DELETE FROM sleep_sessions WHERE sessionDate = :date")
    suspend fun deleteForDate(date: LocalDate)

    @Upsert
    suspend fun upsert(session: SleepSessionEntity)
}

@Dao
interface ExerciseDao {
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
}

@Dao
interface SmokingDao {
    @Query("SELECT * FROM smoking_entries WHERE date = :date ORDER BY id DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<SmokingEntryEntity?>

    @Query("SELECT * FROM smoking_entries WHERE date = :date LIMIT 1")
    suspend fun getForDate(date: LocalDate): SmokingEntryEntity?

    @Upsert
    suspend fun upsert(entry: SmokingEntryEntity)

    @Query("DELETE FROM smoking_entries WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate)
}

@Dao
interface StepDao {
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

    @Upsert
    suspend fun upsert(entry: StepEntryEntity)
}

@Dao
interface SupplementTemplateDao {
    @Query("SELECT * FROM supplement_templates WHERE isActive = 1 ORDER BY sortOrder ASC, name ASC")
    fun observeActive(): Flow<List<SupplementTemplateEntity>>

    @Query("SELECT * FROM supplement_templates ORDER BY sortOrder ASC, name ASC")
    suspend fun getAll(): List<SupplementTemplateEntity>

    @Upsert
    suspend fun upsertAll(templates: List<SupplementTemplateEntity>)

    @Query("UPDATE supplement_templates SET isActive = 0 WHERE id IN (:ids)")
    suspend fun deactivate(ids: List<Long>)
}

@Dao
interface SupplementCheckDao {
    @Query("SELECT * FROM supplement_checks WHERE date = :date")
    fun observeForDate(date: LocalDate): Flow<List<SupplementCheckEntity>>

    @Upsert
    suspend fun upsert(check: SupplementCheckEntity)
}

@Dao
interface SupplementDoseDao {
    @Query("SELECT * FROM supplement_dose_entries WHERE date = :date ORDER BY loggedAt ASC")
    fun observeForDate(date: LocalDate): Flow<List<SupplementDoseEntryEntity>>

    @Upsert
    suspend fun upsertAll(entries: List<SupplementDoseEntryEntity>)

    @Query("DELETE FROM supplement_dose_entries WHERE date = :date")
    suspend fun deleteForDate(date: LocalDate)

    @Query("DELETE FROM supplement_dose_entries WHERE templateId = :templateId AND date = :date")
    suspend fun deleteForTemplateAndDate(templateId: Long, date: LocalDate)

    @Transaction
    suspend fun replaceForDate(date: LocalDate, entries: List<SupplementDoseEntryEntity>) {
        deleteForDate(date)
        if (entries.isNotEmpty()) {
            upsertAll(entries)
        }
    }
}

@Dao
interface BodyMeasurementDao {
    @Query("SELECT * FROM body_measurements WHERE date = :date ORDER BY recordedAt DESC LIMIT 1")
    fun observeForDate(date: LocalDate): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC LIMIT 1")
    fun observeLatest(): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY date ASC, recordedAt ASC")
    fun observeAll(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY date ASC, recordedAt ASC LIMIT 1")
    fun observeEarliest(): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getLatest(): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date = :date ORDER BY recordedAt DESC LIMIT 1")
    suspend fun getForDate(date: LocalDate): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date <= :date ORDER BY date DESC, recordedAt DESC LIMIT 1")
    suspend fun getLatestOnOrBefore(date: LocalDate): BodyMeasurementEntity?

    @Query("SELECT * FROM body_measurements WHERE date <= :date ORDER BY date DESC, recordedAt DESC LIMIT 1")
    fun observeLatestOnOrBefore(date: LocalDate): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE date >= :date ORDER BY date ASC, recordedAt DESC LIMIT 1")
    fun observeEarliestOnOrAfter(date: LocalDate): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, recordedAt ASC")
    fun observeBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<BodyMeasurementEntity>>

    @Query("DELETE FROM body_measurements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Upsert
    suspend fun upsert(measurement: BodyMeasurementEntity)
}

@Database(
    entities = [
        MealEntryEntity::class,
        HydrationEntryEntity::class,
        SleepSessionEntity::class,
        ExerciseEntryEntity::class,
        SmokingEntryEntity::class,
        SupplementTemplateEntity::class,
        SupplementCheckEntity::class,
        SupplementDoseEntryEntity::class,
        BodyMeasurementEntity::class,
        StepEntryEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(HealthTypeConverters::class)
abstract class HealthDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun hydrationDao(): HydrationDao
    abstract fun sleepDao(): SleepDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun smokingDao(): SmokingDao
    abstract fun stepDao(): StepDao
    abstract fun supplementTemplateDao(): SupplementTemplateDao
    abstract fun supplementCheckDao(): SupplementCheckDao
    abstract fun supplementDoseDao(): SupplementDoseDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
}
