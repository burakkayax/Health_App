package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.ExerciseEntryEntity
import com.burak.healthapp.domain.model.ExerciseEntry
import com.burak.healthapp.domain.model.ExerciseIntensity
import com.burak.healthapp.domain.model.ExerciseType

fun ExerciseEntryEntity.toDomain(): ExerciseEntry = ExerciseEntry(
    id = id,
    date = date,
    type = ExerciseType.valueOf(type),
    durationMinutes = durationMinutes,
    intensity = ExerciseIntensity.valueOf(intensity),
)

fun ExerciseEntry.toEntity(): ExerciseEntryEntity = ExerciseEntryEntity(
    id = id,
    date = date,
    type = type.name,
    durationMinutes = durationMinutes,
    intensity = intensity.name,
)
