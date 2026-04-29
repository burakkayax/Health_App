package com.burak.healthapp.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.burak.healthapp.core.datastore.SettingsKeys
import com.burak.healthapp.data.local.dao.BodyMeasurementDao
import com.burak.healthapp.data.local.dao.SupplementTemplateDao
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
import com.burak.healthapp.data.local.mapper.DEFAULT_SUPPLEMENT_NAMES
import com.burak.healthapp.data.local.mapper.createSupplementTemplatesFromNames
import com.burak.healthapp.data.local.mapper.toDomain
import com.burak.healthapp.data.local.mapper.toEntity
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.DashboardCardConfig
import com.burak.healthapp.domain.model.DashboardCardType
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.SettingsState
import com.burak.healthapp.domain.model.SupplementTemplate
import com.burak.healthapp.domain.model.ThemeMode
import com.burak.healthapp.domain.model.UserProfile
import com.burak.healthapp.domain.model.WaterReminderSettings
import com.burak.healthapp.domain.model.defaultDashboardCardConfig
import com.burak.healthapp.domain.model.sanitizeDashboardCardConfig
import com.burak.healthapp.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalTime

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val templateDao: SupplementTemplateDao,
    private val measurementDao: BodyMeasurementDao,
) : SettingsRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val dashboardCardConfigSerializer = ListSerializer(DashboardCardConfig.serializer())

    override val settings: Flow<SettingsState> = dataStore.data.map { preferences ->
        val legacySleepTarget = preferences[SettingsKeys.sleepTarget] ?: DefaultHealthGoals.LEGACY_SLEEP_TARGET_MINUTES
        val fallbackSleepSchedule = deriveSleepScheduleFromLegacy(legacySleepTarget)
        val bedtime = preferences[SettingsKeys.sleepTargetBedtime]?.let(LocalTime::parse)
            ?: fallbackSleepSchedule.first
        val wakeTime = preferences[SettingsKeys.sleepTargetWakeTime]?.let(LocalTime::parse)
            ?: fallbackSleepSchedule.second

        SettingsState(
            onboardingCompleted = preferences[SettingsKeys.onboardingComplete] ?: false,
            userProfile = UserProfile(
                name = preferences[SettingsKeys.userName] ?: "Misafir",
                avatarInitials = preferences[SettingsKeys.avatarInitials] ?: "M",
                heightCm = preferences[SettingsKeys.heightCm],
            ),
            goalSettings = GoalSettings(
                dailyCaloriesTarget = preferences[SettingsKeys.dailyCaloriesTarget] ?: DefaultHealthGoals.DAILY_CALORIES,
                proteinTargetGrams = preferences[SettingsKeys.proteinTarget] ?: DefaultHealthGoals.PROTEIN_GRAMS,
                carbTargetGrams = preferences[SettingsKeys.carbTarget] ?: DefaultHealthGoals.CARB_GRAMS,
                fatTargetGrams = preferences[SettingsKeys.fatTarget] ?: DefaultHealthGoals.FAT_GRAMS,
                waterTargetMl = preferences[SettingsKeys.waterTarget] ?: DefaultHealthGoals.WATER_TARGET_ML,
                dailyStepTarget = preferences[SettingsKeys.dailyStepTarget] ?: DefaultHealthGoals.DAILY_STEPS,
                dailyCaffeineLimitMg = preferences[SettingsKeys.dailyCaffeineLimitMg] ?: DefaultHealthGoals.DAILY_CAFFEINE_LIMIT_MG,
                caffeineCutoffTime = preferences[SettingsKeys.caffeineCutoffTime]?.let(LocalTime::parse)
                    ?: DefaultHealthGoals.CAFFEINE_CUTOFF_TIME,
                caffeineSleepBufferHours = preferences[SettingsKeys.caffeineSleepBufferHours] ?: DefaultHealthGoals.CAFFEINE_SLEEP_BUFFER_HOURS,
                sleepTargetBedtime = bedtime,
                sleepTargetWakeTime = wakeTime,
                exerciseTargetDaysPerWeek = preferences[SettingsKeys.exerciseTargetDays] ?: DefaultHealthGoals.EXERCISE_DAYS_PER_WEEK,
                exerciseTargetDurationMinutes = preferences[SettingsKeys.exerciseTargetDuration] ?: DefaultHealthGoals.EXERCISE_DURATION_MINUTES,
                smokeDailyLimit = preferences[SettingsKeys.smokeDailyLimit] ?: DefaultHealthGoals.SMOKE_DAILY_LIMIT,
                baselineWeightKg = preferences[SettingsKeys.baselineWeight] ?: DefaultHealthGoals.BASELINE_WEIGHT_KG,
                targetWeightKg = preferences[SettingsKeys.targetWeight] ?: DefaultHealthGoals.TARGET_WEIGHT_KG,
                baselineShoulderCm = preferences[SettingsKeys.baselineShoulder] ?: DefaultHealthGoals.BASELINE_SHOULDER_CM,
                baselineWaistCm = preferences[SettingsKeys.baselineWaist] ?: DefaultHealthGoals.BASELINE_WAIST_CM,
                baselineHipCm = preferences[SettingsKeys.baselineHip] ?: DefaultHealthGoals.BASELINE_HIP_CM,
            ),
            waterReminderSettings = WaterReminderSettings(
                enabled = preferences[SettingsKeys.waterReminderEnabled] ?: false,
                startTime = preferences[SettingsKeys.waterReminderStartTime]?.let(LocalTime::parse)
                    ?: DefaultHealthGoals.WATER_REMINDER_START_TIME,
                endTime = preferences[SettingsKeys.waterReminderEndTime]?.let(LocalTime::parse)
                    ?: DefaultHealthGoals.WATER_REMINDER_END_TIME,
                intervalMinutes = preferences[SettingsKeys.waterReminderIntervalMinutes] ?: DefaultHealthGoals.WATER_REMINDER_INTERVAL_MINUTES,
            ),
            waterReminderSnoozedDate = preferences[SettingsKeys.waterReminderSnoozedDate]?.let(LocalDate::parse),
            stepTrackingEnabled = preferences[SettingsKeys.stepTrackingEnabled] ?: false,
            dashboardCards = preferences[SettingsKeys.dashboardCardConfig]
                ?.let(::decodeDashboardCardConfig)
                ?: defaultDashboardCardConfig(),
            themeMode = ThemeMode.entries.firstOrNull { mode ->
                mode.name == preferences[SettingsKeys.themeMode]
            } ?: ThemeMode.SYSTEM,
        )
    }

    override fun observeSupplementTemplates(): Flow<List<SupplementTemplate>> = templateDao.observeActive().map { templates ->
        templates.map(SupplementTemplateEntity::toDomain)
    }

    override suspend fun completeOnboarding(
        profile: UserProfile,
        goals: GoalSettings,
        initialMeasurement: BodyMeasurementEntry,
        supplements: List<String>,
    ) {
        updateProfile(profile)
        updateGoalSettings(goals)
        measurementDao.upsert(initialMeasurement.toEntity())
        replaceSupplementTemplates(
            createSupplementTemplatesFromNames(
                names = supplements.ifEmpty { DEFAULT_SUPPLEMENT_NAMES },
            ),
        )
        dataStore.edit { preferences ->
            preferences[SettingsKeys.onboardingComplete] = true
        }
    }

    override suspend fun updateGoalSettings(goals: GoalSettings) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.dailyCaloriesTarget] = goals.dailyCaloriesTarget
            preferences[SettingsKeys.proteinTarget] = goals.proteinTargetGrams
            preferences[SettingsKeys.carbTarget] = goals.carbTargetGrams
            preferences[SettingsKeys.fatTarget] = goals.fatTargetGrams
            preferences[SettingsKeys.waterTarget] = goals.waterTargetMl
            preferences[SettingsKeys.dailyStepTarget] = goals.dailyStepTarget
            preferences[SettingsKeys.dailyCaffeineLimitMg] = goals.dailyCaffeineLimitMg
            preferences[SettingsKeys.caffeineCutoffTime] = goals.caffeineCutoffTime.toString()
            preferences[SettingsKeys.caffeineSleepBufferHours] = goals.caffeineSleepBufferHours
            preferences[SettingsKeys.sleepTargetBedtime] = goals.sleepTargetBedtime.toString()
            preferences[SettingsKeys.sleepTargetWakeTime] = goals.sleepTargetWakeTime.toString()
            preferences[SettingsKeys.exerciseTargetDays] = goals.exerciseTargetDaysPerWeek
            preferences[SettingsKeys.exerciseTargetDuration] = goals.exerciseTargetDurationMinutes
            preferences[SettingsKeys.smokeDailyLimit] = goals.smokeDailyLimit
            preferences[SettingsKeys.baselineWeight] = goals.baselineWeightKg
            preferences[SettingsKeys.targetWeight] = goals.targetWeightKg
            preferences[SettingsKeys.baselineShoulder] = goals.baselineShoulderCm
            preferences[SettingsKeys.baselineWaist] = goals.baselineWaistCm
            preferences[SettingsKeys.baselineHip] = goals.baselineHipCm
        }
    }

    override suspend fun updateWaterReminderSettings(settings: WaterReminderSettings) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.waterReminderEnabled] = settings.enabled
            preferences[SettingsKeys.waterReminderStartTime] = settings.startTime.toString()
            preferences[SettingsKeys.waterReminderEndTime] = settings.endTime.toString()
            preferences[SettingsKeys.waterReminderIntervalMinutes] = settings.intervalMinutes.coerceAtLeast(DefaultHealthGoals.MIN_WATER_REMINDER_INTERVAL_MINUTES)
        }
    }

    override suspend fun updateWaterReminderSnoozedDate(date: LocalDate?) {
        dataStore.edit { preferences ->
            if (date == null) {
                preferences.remove(SettingsKeys.waterReminderSnoozedDate)
            } else {
                preferences[SettingsKeys.waterReminderSnoozedDate] = date.toString()
            }
        }
    }

    override suspend fun updateStepTrackingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.stepTrackingEnabled] = enabled
        }
    }

    override suspend fun updateDashboardCardVisibility(
        type: DashboardCardType,
        isVisible: Boolean,
    ) {
        val updated = settings.first().dashboardCards.map { config ->
            if (config.type == type) config.copy(isVisible = isVisible) else config
        }
        saveDashboardCardConfig(updated)
    }

    override suspend fun moveDashboardCard(type: DashboardCardType, newIndex: Int) {
        val current = settings.first().dashboardCards
            .sortedBy(DashboardCardConfig::sortOrder)
            .toMutableList()
        val currentIndex = current.indexOfFirst { it.type == type }
        if (currentIndex == -1) return

        val item = current.removeAt(currentIndex)
        current.add(newIndex.coerceIn(0, current.size), item)
        saveDashboardCardConfig(current)
    }

    override suspend fun resetDashboardCardsToDefault() {
        saveDashboardCardConfig(defaultDashboardCardConfig())
    }

    override suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.userName] = profile.name
            preferences[SettingsKeys.avatarInitials] = profile.avatarInitials
            if (profile.heightCm != null) {
                preferences[SettingsKeys.heightCm] = profile.heightCm
            } else {
                preferences.remove(SettingsKeys.heightCm)
            }
        }
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsKeys.themeMode] = mode.name
        }
    }

    override suspend fun replaceSupplementTemplates(templates: List<SupplementTemplate>) {
        val sanitizedTemplates = templates
            .map { template ->
                template.copy(
                    name = template.name.trim(),
                    targetAmount = template.targetAmount.takeIf { it > 0f } ?: 1f,
                    unitLabel = template.unitLabel.trim().ifBlank { "doz" },
                )
            }
            .filter { it.name.isNotBlank() }
            .distinctBy { it.name.lowercase() }

        val current = templateDao.getAll()
        val currentById = current.associateBy { it.id }

        val updated = sanitizedTemplates.mapIndexed { index, template ->
            val existing = currentById[template.id]
            if (existing != null) {
                existing.copy(
                    name = template.name,
                    targetAmount = template.targetAmount,
                    unitLabel = template.unitLabel,
                    isActive = true,
                    sortOrder = index,
                )
            } else {
                SupplementTemplateEntity(
                    name = template.name,
                    targetAmount = template.targetAmount,
                    unitLabel = template.unitLabel,
                    isActive = true,
                    sortOrder = index,
                )
            }
        }

        templateDao.upsertAll(updated)

        val keptIds = sanitizedTemplates.mapNotNull { template ->
            currentById[template.id]?.id
        }.toSet()
        val deactivateIds = current
            .filter { template -> template.id !in keptIds }
            .map { it.id }

        if (deactivateIds.isNotEmpty()) {
            templateDao.deactivate(deactivateIds)
        }
    }

    private fun decodeDashboardCardConfig(raw: String): List<DashboardCardConfig>? = runCatching {
        sanitizeDashboardCardConfig(json.decodeFromString(dashboardCardConfigSerializer, raw))
    }.getOrNull()

    private suspend fun saveDashboardCardConfig(config: List<DashboardCardConfig>) {
        val sanitized = sanitizeDashboardCardConfig(
            config.mapIndexed { index, item -> item.copy(sortOrder = index) },
        )
        dataStore.edit { preferences ->
            preferences[SettingsKeys.dashboardCardConfig] = json.encodeToString(dashboardCardConfigSerializer, sanitized)
        }
    }
}

private fun deriveSleepScheduleFromLegacy(totalMinutes: Int): Pair<LocalTime, LocalTime> {
    val bedtime = DefaultHealthGoals.SLEEP_BEDTIME
    val wakeTime = bedtime.plusMinutes(totalMinutes.coerceAtLeast(0).toLong())
    return bedtime to wakeTime
}
