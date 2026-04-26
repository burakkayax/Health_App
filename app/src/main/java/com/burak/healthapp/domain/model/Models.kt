package com.burak.healthapp.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

enum class MealType(val label: String) {
    BREAKFAST("Kahvaltı"),
    LUNCH("Öğle"),
    DINNER("Akşam"),
    SNACK("Ara Öğün"),
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM,
}

enum class TrendsPeriod {
    WEEKLY,
    MONTHLY,
}

enum class ExerciseType(val label: String) {
    WEIGHTS("Ağırlık"),
    RUN("Koşu"),
    WALK("Yürüyüş"),
    BIKE("Bisiklet"),
    YOGA("Yoga"),
}

enum class ExerciseIntensity(val label: String) {
    LOW("Düşük"),
    MEDIUM("Orta"),
    HIGH("Yüksek"),
}

data class MealEntry(
    val id: Long = 0,
    val date: LocalDate,
    val mealType: MealType,
    val name: String,
    val calories: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val proteinGrams: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class HydrationEntry(
    val id: Long = 0,
    val date: LocalDate,
    val amountMl: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class SleepSession(
    val id: Long = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    val sessionDate: LocalDate
        get() = endTime.toLocalDate()
}

data class ExerciseEntry(
    val id: Long = 0,
    val date: LocalDate,
    val type: ExerciseType,
    val durationMinutes: Int,
    val intensity: ExerciseIntensity,
)

data class SmokingEntry(
    val id: Long = 0,
    val date: LocalDate,
    val count: Int,
)

data class StepEntry(
    val id: Long = 0,
    val date: LocalDate,
    val steps: Int,
    val sensorBaseline: Int? = null,
    val lastSensorValue: Int? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class SupplementTemplate(
    val id: Long = 0,
    val name: String,
    val targetAmount: Float,
    val unitLabel: String,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
)

data class SupplementDoseEntry(
    val id: Long = 0,
    val templateId: Long,
    val date: LocalDate,
    val amount: Float,
    val loggedAt: LocalDateTime = LocalDateTime.now(),
)

data class BodyMeasurementEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float,
    val shoulderCm: Float,
    val waistCm: Float,
    val hipCm: Float,
    val recordedAt: LocalDateTime = LocalDateTime.now(),
)

data class GoalSettings(
    val dailyCaloriesTarget: Int = 2200,
    val proteinTargetGrams: Int = 160,
    val carbTargetGrams: Int = 220,
    val fatTargetGrams: Int = 70,
    val waterTargetMl: Int = 2500,
    val dailyStepTarget: Int = 8000,
    val sleepTargetBedtime: LocalTime = LocalTime.of(23, 0),
    val sleepTargetWakeTime: LocalTime = LocalTime.of(7, 0),
    val exerciseTargetDaysPerWeek: Int = 4,
    val exerciseTargetDurationMinutes: Int = 45,
    val smokeDailyLimit: Int = 0,
    val baselineWeightKg: Float = 78f,
    val targetWeightKg: Float = 74f,
    val baselineShoulderCm: Float = 118f,
    val baselineWaistCm: Float = 88f,
    val baselineHipCm: Float = 99f,
) {
    val sleepTargetMinutes: Int
        get() {
            val bedtimeMinutes = sleepTargetBedtime.toSecondOfDay() / 60
            var wakeMinutes = sleepTargetWakeTime.toSecondOfDay() / 60
            if (wakeMinutes <= bedtimeMinutes) {
                wakeMinutes += (24 * 60)
            }
            return (wakeMinutes - bedtimeMinutes).coerceAtLeast(0)
    }
}

data class WaterReminderSettings(
    val enabled: Boolean = false,
    val startTime: LocalTime = LocalTime.of(9, 0),
    val endTime: LocalTime = LocalTime.of(21, 0),
    val intervalMinutes: Int = 60,
)

data class UserProfile(
    val name: String = "Misafir",
    val avatarInitials: String = "M",
    val heightCm: Float? = null,
) {
    companion object {
        fun fromName(name: String, heightCm: Float? = null): UserProfile {
            val trimmed = name.trim().ifBlank { "Misafir" }
            val initials = trimmed
                .split(" ")
                .filter { it.isNotBlank() }
                .take(2)
                .joinToString("") { it.first().uppercase() }
                .ifBlank { "M" }
            return UserProfile(
                name = trimmed,
                avatarInitials = initials,
                heightCm = heightCm,
            )
        }
    }
}

data class SettingsState(
    val onboardingCompleted: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val goalSettings: GoalSettings = GoalSettings(),
    val waterReminderSettings: WaterReminderSettings = WaterReminderSettings(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

data class DayNutritionTotal(
    val calories: Int = 0,
    val carbsGrams: Int = 0,
    val fatGrams: Int = 0,
    val proteinGrams: Int = 0,
)

data class TodaySnapshot(
    val settings: SettingsState,
    val meals: List<MealEntry>,
    val hydrationEntries: List<HydrationEntry>,
    val sleepSessionForDate: SleepSession?,
    val exerciseEntryForDate: ExerciseEntry?,
    val weekExerciseEntries: List<ExerciseEntry>,
    val smokingEntryForDate: SmokingEntry?,
    val stepEntryForDate: StepEntry?,
    val weekStepEntries: List<StepEntry>,
    val supplementTemplates: List<SupplementTemplate>,
    val supplementDoseEntries: List<SupplementDoseEntry>,
    val measurementForDate: BodyMeasurementEntry?,
)

data class TrendPoint(
    val label: String,
    val value: Float,
)

data class CalorieBarPoint(
    val label: String,
    val calories: Int,
    val progress: Float,
)

data class TrendsSnapshot(
    val period: TrendsPeriod,
    val averageProteinGrams: Float,
    val averageSleepMinutes: Float,
    val averageWaterMl: Float,
    val averageSteps: Float,
    val averageCalories: Float,
    val weeklyCalories: List<CalorieBarPoint>,
    val weightPoints: List<TrendPoint>,
    val stepPoints: List<TrendPoint>,
)
