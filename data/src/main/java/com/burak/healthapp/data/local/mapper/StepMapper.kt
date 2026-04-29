package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.StepEntryEntity
import com.burak.healthapp.domain.model.StepEntry

fun StepEntryEntity.toDomain(): StepEntry = StepEntry(
    id = id,
    date = date,
    steps = steps,
    sensorBaseline = sensorBaseline,
    lastSensorValue = lastSensorValue,
    updatedAt = updatedAt,
)
