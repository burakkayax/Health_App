package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.BodyMeasurementEntity
import com.burak.healthapp.domain.model.BodyMeasurementEntry

fun BodyMeasurementEntity.toDomain(): BodyMeasurementEntry = BodyMeasurementEntry(
    id = id,
    date = date,
    weightKg = weightKg,
    shoulderCm = shoulderCm,
    waistCm = waistCm,
    hipCm = hipCm,
    recordedAt = recordedAt,
)

fun BodyMeasurementEntry.toEntity(): BodyMeasurementEntity = BodyMeasurementEntity(
    id = id,
    date = date,
    weightKg = weightKg,
    shoulderCm = shoulderCm,
    waistCm = waistCm,
    hipCm = hipCm,
    recordedAt = recordedAt,
)
