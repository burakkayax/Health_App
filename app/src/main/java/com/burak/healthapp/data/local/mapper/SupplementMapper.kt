package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.data.local.entity.MealEntryEntity
import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.data.local.entity.SmokingEntryEntity
import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.data.local.entity.SupplementDoseEntryEntity
import com.burak.healthapp.data.local.entity.SupplementTemplateEntity
import com.burak.healthapp.domain.model.BodyMeasurementEntry
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType
import com.burak.healthapp.domain.model.HydrationEntry
import com.burak.healthapp.domain.model.MealEntry
import com.burak.healthapp.domain.model.MealType
import com.burak.healthapp.domain.model.SleepSession
import com.burak.healthapp.domain.model.SmokingEntry
import com.burak.healthapp.domain.model.StepEntry
import com.burak.healthapp.domain.model.SupplementDoseEntry
import com.burak.healthapp.domain.model.SupplementTemplate
import java.time.LocalTime

fun SupplementTemplateEntity.toDomain(): SupplementTemplate {
    return SupplementTemplate(
        id = id,
        name = name,
        targetAmount = targetAmount,
        unitLabel = unitLabel,
        isActive = isActive,
        sortOrder = sortOrder,
    )
}

fun SupplementDoseEntryEntity.toDomain(): SupplementDoseEntry {
    return SupplementDoseEntry(
        id = id,
        templateId = templateId,
        date = date,
        amount = amount,
        loggedAt = loggedAt,
    )
}

fun SupplementDoseEntry.toEntity(): SupplementDoseEntryEntity {
    return SupplementDoseEntryEntity(
        id = id,
        templateId = templateId,
        date = date,
        amount = amount,
        loggedAt = loggedAt,
    )
}

internal val DEFAULT_SUPPLEMENT_NAMES = listOf(
    "B12",
    "D3 Vitamini",
    "Demir",
    "Omega 3",
    "Magnezyum",
)

fun createSupplementTemplatesFromNames(names: List<String>): List<SupplementTemplate> {
    return names.mapIndexed { index, name ->
        val trimmedName = name.trim()
        val preset = DEFAULT_SUPPLEMENT_PRESETS[trimmedName.lowercase()]
        SupplementTemplate(
            name = trimmedName,
            targetAmount = preset?.first ?: 1f,
            unitLabel = preset?.second ?: "doz",
            sortOrder = index,
        )
    }
}

internal val DEFAULT_SUPPLEMENT_PRESETS = mapOf(
    "b12" to (500f to "mcg"),
    "d3 vitamini" to (25f to "mcg"),
    "demir" to (18f to "mg"),
    "omega 3" to (1000f to "mg"),
    "magnezyum" to (200f to "mg"),
)
