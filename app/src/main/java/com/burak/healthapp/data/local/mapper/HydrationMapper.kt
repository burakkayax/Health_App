package com.burak.healthapp.data.local.mapper

import com.burak.healthapp.data.local.entity.HydrationEntryEntity
import com.burak.healthapp.domain.model.HydrationEntry

fun HydrationEntryEntity.toDomain(): HydrationEntry = HydrationEntry(
    id = id,
    date = date,
    amountMl = amountMl,
    createdAt = createdAt,
)
