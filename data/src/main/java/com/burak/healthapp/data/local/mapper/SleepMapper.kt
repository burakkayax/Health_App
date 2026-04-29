package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.SleepSessionEntity
import com.burak.healthapp.domain.model.SleepSession

fun SleepSessionEntity.toDomain(): SleepSession = SleepSession(
    id = id,
    startTime = startTime,
    endTime = endTime,
)

fun SleepSession.toEntity(): SleepSessionEntity = SleepSessionEntity(
    id = id,
    sessionDate = sessionDate,
    startTime = startTime,
    endTime = endTime,
)
