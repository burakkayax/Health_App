package com.burak.healthapp.feature.today

import com.burak.healthapp.domain.calculation.calculateHydrationTotal
import com.burak.healthapp.domain.calculation.calculateNutritionTotals
import com.burak.healthapp.domain.calculation.calculateSleepDurationMinutes
import com.burak.healthapp.domain.calculation.clampProgress
import com.burak.healthapp.domain.calculation.countExerciseDays
import com.burak.healthapp.domain.calculation.directionAwareProgress
import com.burak.healthapp.domain.calculation.formatClockRange
import com.burak.healthapp.domain.calculation.formatMinutesAsSleepLabel
import com.burak.healthapp.domain.calculation.formatSleepDuration
import com.burak.healthapp.domain.config.DefaultHealthGoals
import com.burak.healthapp.domain.model.GoalSettings
import com.burak.healthapp.domain.model.TodaySnapshot
import java.util.Locale

internal fun snapshotToUiState(snapshot: TodaySnapshot): TodayUiState {
    val totals = calculateNutritionTotals(snapshot.meals)
    val hydrationTotal = calculateHydrationTotal(snapshot.hydrationEntries)
    val goals = snapshot.settings.goalSettings
    val sleepMinutes = calculateSleepDurationMinutes(snapshot.sleepSessionForDate)
    val locale = Locale.forLanguageTag("tr")
    val currentDoseByTemplate = snapshot.supplementDoseEntries
        .groupBy { it.templateId }
        .mapValues { (_, entries) -> entries.sumOf { it.amount.toDouble() }.toFloat() }
    val weightState = snapshot.measurementForDate.toWeightCardState(goals, locale)
    val exerciseDays = countExerciseDays(snapshot.weekExerciseEntries)
    val exerciseEntry = snapshot.exerciseEntryForDate
    val smokingEntry = snapshot.smokingEntryForDate
    val stepCount = snapshot.stepEntryForDate?.steps ?: 0
    val weekSteps = snapshot.weekStepEntries.sumOf { it.steps }

    return TodayUiState(
        userName = snapshot.settings.userProfile.name,
        avatarInitials = snapshot.settings.userProfile.avatarInitials,
        goalSettings = goals,
        latestMeasurement = snapshot.measurementForDate,
        nutrition = NutritionCardState(
            currentCalories = totals.calories,
            targetCalories = goals.dailyCaloriesTarget,
            progress = clampProgress(totals.calories.toFloat(), goals.dailyCaloriesTarget.toFloat()),
            macros = listOf(
                MacroRingState(
                    label = "Karb",
                    current = totals.carbsGrams,
                    target = goals.carbTargetGrams,
                    progress = clampProgress(totals.carbsGrams.toFloat(), goals.carbTargetGrams.toFloat()),
                ),
                MacroRingState(
                    label = "Yağ",
                    current = totals.fatGrams,
                    target = goals.fatTargetGrams,
                    progress = clampProgress(totals.fatGrams.toFloat(), goals.fatTargetGrams.toFloat()),
                ),
                MacroRingState(
                    label = "Protein",
                    current = totals.proteinGrams,
                    target = goals.proteinTargetGrams,
                    progress = clampProgress(totals.proteinGrams.toFloat(), goals.proteinTargetGrams.toFloat()),
                    isEmphasized = true,
                ),
            ),
            entries = snapshot.meals,
        ),
        weight = weightState,
        exercise = ExerciseCardState(
            type = exerciseEntry?.type,
            durationMinutes = exerciseEntry?.durationMinutes ?: 0,
            intensity = exerciseEntry?.intensity,
            progress = clampProgress(
                (exerciseEntry?.durationMinutes ?: 0).toFloat(),
                goals.exerciseTargetDurationMinutes.toFloat(),
            ),
            title = exerciseEntry?.type?.label ?: "Antrenman eklenmedi",
            durationLabel = if (exerciseEntry != null) {
                "${exerciseEntry.durationMinutes} dk"
            } else {
                "Süre eklenmedi"
            },
            intensityLabel = exerciseEntry?.intensity?.label ?: "Yoğunluk seçilmedi",
            helperLabel = "Bu hafta $exerciseDays / ${goals.exerciseTargetDaysPerWeek} gün",
        ),
        hydration = HydrationCardState(
            currentMl = hydrationTotal,
            targetMl = goals.waterTargetMl,
            progress = clampProgress(hydrationTotal.toFloat(), goals.waterTargetMl.toFloat()),
            entries = snapshot.hydrationEntries,
        ),
        sleep = SleepCardState(
            durationLabel = formatSleepDuration(snapshot.sleepSessionForDate),
            timeRangeLabel = formatClockRange(snapshot.sleepSessionForDate),
            targetLabel = formatMinutesAsSleepLabel(goals.sleepTargetMinutes),
            progress = clampProgress(sleepMinutes.toFloat(), goals.sleepTargetMinutes.toFloat()),
        ),
        smoking = smokingEntry.toSmokingCardState(goals),
        steps = StepCardState(
            currentSteps = stepCount,
            targetSteps = goals.dailyStepTarget,
            progress = clampProgress(stepCount.toFloat(), goals.dailyStepTarget.toFloat()),
            headline = "$stepCount adım",
            supportingLabel = "Hedef ${goals.dailyStepTarget} adım",
            helperLabel = "Bu hafta $weekSteps adım",
        ),
        supplements = SupplementCardState(
            items = snapshot.supplementTemplates.map { template ->
                val currentAmount = currentDoseByTemplate[template.id] ?: 0f
                SupplementItemState(
                    id = template.id,
                    name = template.name,
                    currentAmount = currentAmount,
                    targetAmount = template.targetAmount,
                    unitLabel = template.unitLabel,
                    progress = clampProgress(currentAmount, template.targetAmount),
                )
            },
        ),
        dashboardCards = snapshot.settings.dashboardCards,
    )
}

private fun com.burak.healthapp.domain.model.BodyMeasurementEntry?.toWeightCardState(
    goals: GoalSettings,
    locale: Locale,
): WeightCardState {
    if (this == null) {
        return WeightCardState(
            currentWeightKg = null,
            targetWeightKg = goals.targetWeightKg,
            progress = 0f,
            hasMeasurement = false,
            headline = "Kayıt yok",
            supportingLabel = "Bu tarih için kilo eklenmedi",
            helperLabel = String.format(locale, "Hedef %.1f kg", goals.targetWeightKg),
        )
    }

    return WeightCardState(
        currentWeightKg = weightKg,
        targetWeightKg = goals.targetWeightKg,
        progress = directionAwareProgress(
            baseline = goals.baselineWeightKg,
            current = weightKg,
            target = goals.targetWeightKg,
        ),
        hasMeasurement = true,
        headline = String.format(locale, "%.1f kg", weightKg),
        supportingLabel = String.format(locale, "Hedef %.1f kg", goals.targetWeightKg),
        helperLabel = String.format(locale, "Başlangıç %.1f kg", goals.baselineWeightKg),
    )
}

internal fun emptyUiState(): TodayUiState = TodayUiState(
    userName = "",
    avatarInitials = "M",
    goalSettings = GoalSettings(),
    latestMeasurement = null,
    nutrition = NutritionCardState(
        currentCalories = 0,
        targetCalories = 0,
        progress = 0f,
        macros = emptyList(),
        entries = emptyList(),
    ),
    weight = WeightCardState(
        currentWeightKg = null,
        targetWeightKg = 0f,
        progress = 0f,
        hasMeasurement = false,
        headline = "Kayıt yok",
        supportingLabel = "Bu tarih için kilo eklenmedi",
        helperLabel = "",
    ),
    exercise = ExerciseCardState(
        type = null,
        durationMinutes = 0,
        intensity = null,
        progress = 0f,
        title = "Antrenman eklenmedi",
        durationLabel = "Süre eklenmedi",
        intensityLabel = "Yoğunluk seçilmedi",
        helperLabel = "",
    ),
    hydration = HydrationCardState(
        currentMl = 0,
        targetMl = 0,
        progress = 0f,
    ),
    sleep = SleepCardState(
        durationLabel = "Henüz kayıt yok",
        timeRangeLabel = "Saat ekle",
        targetLabel = "8s 0d",
        progress = 0f,
    ),
    smoking = SmokingCardState(
        count = 0,
        limit = 0,
        progress = 0f,
        headline = "0 adet",
        supportingLabel = "Limit ayarlanmadı",
        helperLabel = "İstersen profilden günlük limit ekleyebilirsin.",
        status = SmokingStatus.PASSIVE,
    ),
    steps = StepCardState(
        currentSteps = 0,
        targetSteps = DefaultHealthGoals.DAILY_STEPS,
        progress = 0f,
        headline = "0 adım",
        supportingLabel = "Hedef 8000 adım",
        helperLabel = "Telefon hareket ettikçe otomatik güncellenir.",
    ),
    supplements = SupplementCardState(items = emptyList()),
    dashboardCards = com.burak.healthapp.domain.model.defaultDashboardCardConfig(),
)

private fun com.burak.healthapp.domain.model.SmokingEntry?.toSmokingCardState(
    goals: GoalSettings,
): SmokingCardState {
    val count = this?.count ?: 0
    val limit = goals.smokeDailyLimit

    return when {
        limit <= 0 -> SmokingCardState(
            count = count,
            limit = limit,
            progress = 0f,
            headline = "$count adet",
            supportingLabel = "Limit ayarlanmadı",
            helperLabel = "İstersen profilden günlük limit ekleyebilirsin.",
            status = SmokingStatus.PASSIVE,
        )

        count == 0 -> SmokingCardState(
            count = 0,
            limit = limit,
            progress = 0f,
            headline = "0 adet",
            supportingLabel = "Limit $limit adet",
            helperLabel = "Bugün hiç sigara içilmedi.",
            status = SmokingStatus.SAFE,
        )

        count < limit -> SmokingCardState(
            count = count,
            limit = limit,
            progress = clampProgress(count.toFloat(), limit.toFloat()),
            headline = "$count adet",
            supportingLabel = "Limit $limit adet",
            helperLabel = "Limitin içindesin.",
            status = SmokingStatus.WARNING,
        )

        else -> SmokingCardState(
            count = count,
            limit = limit,
            progress = clampProgress(count.toFloat(), limit.toFloat()),
            headline = "$count adet",
            supportingLabel = "Limit $limit adet",
            helperLabel = "Günlük limit aşıldı.",
            status = SmokingStatus.DANGER,
        )
    }
}
